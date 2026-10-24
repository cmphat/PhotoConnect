package com.photoconnect.service;

import com.photoconnect.dto.TimeSlotDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.CalendarEvent;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerUnavailableDate;
import com.photoconnect.exception.PhotographerUnavailableException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.CalendarEventRepository;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.PhotographerUnavailableDateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleServiceImpl.class);

    private static final List<BookingStatus> RESERVING_STATUSES = BookingStatus.TIME_RESERVING_STATUSES_LIST;

    private final PhotographerUnavailableDateRepository scheduleRepository;
    private final PhotographerProfileRepository profileRepository;
    private final BookingRepository bookingRepository;
    private final CalendarEventRepository calendarEventRepository;

    @Autowired
    public ScheduleServiceImpl(PhotographerUnavailableDateRepository scheduleRepository,
                               PhotographerProfileRepository profileRepository,
                               @Autowired(required = false) BookingRepository bookingRepository,
                               @Autowired(required = false) CalendarEventRepository calendarEventRepository) {
        this.scheduleRepository = scheduleRepository;
        this.profileRepository = profileRepository;
        this.bookingRepository = bookingRepository;
        this.calendarEventRepository = calendarEventRepository;
    }

    public ScheduleServiceImpl(PhotographerUnavailableDateRepository scheduleRepository,
                               PhotographerProfileRepository profileRepository) {
        this(scheduleRepository, profileRepository, null, null);
    }

    @Override
    @Transactional
    public void addUnavailableDate(Long profileId, LocalDate date, String reason) {
        if (profileId == null || date == null) {
            throw new IllegalArgumentException("Photographer profile and date are required");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot block dates in the past");
        }

        String normalizedReason = reason != null ? reason.trim() : null;
        if (normalizedReason != null && normalizedReason.length() > 255) {
            throw new IllegalArgumentException("Reason cannot exceed 255 characters");
        }
        if (normalizedReason != null && normalizedReason.isEmpty()) {
            normalizedReason = null;
        }

        if (scheduleRepository.existsByPhotographerProfileIdAndDate(profileId, date)) {
            throw new IllegalArgumentException("Date is already marked as unavailable");
        }

        PhotographerProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        PhotographerUnavailableDate unavailableDate = new PhotographerUnavailableDate(profile, date, normalizedReason);
        scheduleRepository.save(unavailableDate);
    }

    @Override
    @Transactional
    public void removeUnavailableDate(Long profileId, Long dateId) {
        if (profileId == null || dateId == null) {
            throw new IllegalArgumentException("Photographer profile and unavailable date are required");
        }
        scheduleRepository.deleteByIdAndPhotographerProfileId(dateId, profileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotographerUnavailableDate> getUnavailableDates(Long profileId) {
        return scheduleRepository.findByPhotographerProfileIdOrderByDateAsc(profileId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDateAvailable(Long profileId, LocalDate date) {
        if (profileId == null || date == null) {
            return false;
        }
        return !scheduleRepository.existsByPhotographerProfileIdAndDate(profileId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSlotAvailable(Long profileId, LocalDate date, LocalTime time, int durationMinutes) {
        return isSlotAvailable(profileId, date, time, durationMinutes, null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSlotAvailable(Long profileId, LocalDate date, LocalTime time, int durationMinutes, Long excludeBookingId) {
        try {
            validateSlotAvailability(profileId, date, time, durationMinutes, excludeBookingId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateSlotAvailability(Long profileId, LocalDate date, LocalTime time, int durationMinutes, Long excludeBookingId) {
        if (profileId == null || date == null || time == null) {
            throw new IllegalArgumentException("Photographer profile, date, and time are required.");
        }

        int duration = durationMinutes > 0 ? durationMinutes : 120;

        if (date.isBefore(LocalDate.now())) {
            throw new PhotographerUnavailableException("Cannot book a session in the past.");
        }

        if (date.equals(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            throw new PhotographerUnavailableException("Cannot book a time slot that has already passed.");
        }

        // 1. Check whole-day unavailable block
        if (scheduleRepository.existsByPhotographerProfileIdAndDate(profileId, date)) {
            throw new PhotographerUnavailableException("The photographer is marked unavailable on this date.");
        }

        LocalDateTime proposedStart = LocalDateTime.of(date, time);
        LocalDateTime proposedEnd = proposedStart.plusMinutes(duration);

        // 2. Check overlap with confirmed/accepted bookings (newStart < existingEnd AND newEnd > existingStart)
        if (bookingRepository != null) {
            List<Booking> activeBookings = bookingRepository.findActiveBookingsOnDate(profileId, date, RESERVING_STATUSES, excludeBookingId);
            for (Booking existing : activeBookings) {
                LocalTime existingTime = existing.getBookingTime() != null ? existing.getBookingTime() : LocalTime.of(9, 0);
                LocalDateTime existingStart = LocalDateTime.of(existing.getBookingDate(), existingTime);
                int existingDuration = (existing.getDurationMinutes() != null && existing.getDurationMinutes() > 0)
                        ? existing.getDurationMinutes()
                        : 120;
                LocalDateTime existingEnd = existingStart.plusMinutes(existingDuration);

                // Overlap condition: proposedStart < existingEnd && proposedEnd > existingStart
                if (proposedStart.isBefore(existingEnd) && proposedEnd.isAfter(existingStart)) {
                    log.warn("Booking collision detected for photographer [{}]: proposed [{} - {}] overlaps with existing booking [{}] [{} - {}]",
                            profileId, proposedStart, proposedEnd, existing.getId(), existingStart, existingEnd);
                    throw new PhotographerUnavailableException(
                            String.format("The requested time slot (%s for %d mins) conflicts with an existing confirmed booking.",
                                    time.toString(), duration)
                    );
                }
            }
        }

        // 3. Check overlap with photographer's personal/blocked calendar events
        if (calendarEventRepository != null) {
            PhotographerProfile profile = profileRepository.findById(profileId).orElse(null);
            if (profile != null && profile.getUser() != null) {
                Long userId = profile.getUser().getId();
                List<CalendarEvent> conflicts = calendarEventRepository.findConflictingEvents(userId, proposedStart, proposedEnd);
                if (!conflicts.isEmpty()) {
                    log.warn("Calendar event collision detected for photographer user [{}]: proposed [{} - {}] conflicts with {} manual events",
                            userId, proposedStart, proposedEnd, conflicts.size());
                    throw new PhotographerUnavailableException("The requested time slot conflicts with the photographer's personal blocked schedule.");
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotDto> getAvailableTimeSlots(Long profileId, LocalDate date, int durationMinutes) {
        List<TimeSlotDto> slots = new ArrayList<>();
        if (profileId == null || date == null) {
            return slots;
        }

        int duration = durationMinutes > 0 ? durationMinutes : 120;

        // Working window: 08:00 to 20:00, checking every 60 minutes
        LocalTime startHour = LocalTime.of(8, 0);
        LocalTime endHour = LocalTime.of(20, 0);

        LocalTime current = startHour;
        while (!current.plusMinutes(duration).isAfter(endHour)) {
            LocalTime slotEnd = current.plusMinutes(duration);
            boolean available = true;
            String reason = "Available";

            try {
                validateSlotAvailability(profileId, date, current, duration, null);
            } catch (PhotographerUnavailableException e) {
                available = false;
                reason = e.getMessage();
            } catch (Exception e) {
                available = false;
                reason = "Unavailable";
            }

            slots.add(new TimeSlotDto(current, slotEnd, available, reason));
            current = current.plusHours(1);
        }

        return slots;
    }
}
