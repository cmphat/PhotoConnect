package com.photoconnect.service;

import com.photoconnect.dto.CalendarEventDto;
import com.photoconnect.dto.CalendarEventRequest;
import com.photoconnect.entity.*;
import com.photoconnect.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CalendarServiceImpl implements CalendarService {

    private static final Logger log = LoggerFactory.getLogger(CalendarServiceImpl.class);
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final CalendarEventRepository calendarEventRepository;
    private final BookingRepository bookingRepository;
    private final PhotographerProfileRepository photographerProfileRepository;
    private final PhotographerUnavailableDateRepository unavailableDateRepository;
    private final UserRepository userRepository;

    public CalendarServiceImpl(CalendarEventRepository calendarEventRepository,
                               BookingRepository bookingRepository,
                               PhotographerProfileRepository photographerProfileRepository,
                               PhotographerUnavailableDateRepository unavailableDateRepository,
                               UserRepository userRepository) {
        this.calendarEventRepository = calendarEventRepository;
        this.bookingRepository = bookingRepository;
        this.photographerProfileRepository = photographerProfileRepository;
        this.unavailableDateRepository = unavailableDateRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventDto> getCalendarEvents(Long targetUserId, Long currentUserId) {
        List<CalendarEventDto> events = new ArrayList<>();
        boolean isOwner = currentUserId != null && currentUserId.equals(targetUserId);

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + targetUserId));

        // 1. Manual Calendar Events
        List<CalendarEvent> manualEvents = isOwner
                ? calendarEventRepository.findByUserIdOrderByStartAtAsc(targetUserId)
                : calendarEventRepository.findByUserIdAndVisibilityOrderByStartAtAsc(targetUserId, "PUBLIC");

        for (CalendarEvent me : manualEvents) {
            CalendarEventDto dto = new CalendarEventDto();
            dto.setId("manual-" + me.getId());
            dto.setStart(me.getStartAt().format(ISO_FMT));
            dto.setEnd(me.getEndAt().format(ISO_FMT));
            dto.setAllDay(false);
            dto.addExtendedProp("isBooking", false);
            dto.addExtendedProp("manualEventId", me.getId());
            dto.addExtendedProp("eventType", me.getEventType());
            dto.addExtendedProp("visibility", me.getVisibility());
            dto.addExtendedProp("editable", isOwner);

            if (isOwner) {
                dto.setTitle(me.getTitle());
                dto.setDescription(me.getDescription());
                dto.addExtendedProp("rawTitle", me.getTitle());
                dto.addExtendedProp("rawDescription", me.getDescription());
            } else {
                if ("AVAILABLE".equalsIgnoreCase(me.getEventType())) {
                    dto.setTitle("Available Slot");
                } else {
                    dto.setTitle("Busy");
                }
                dto.setDescription(null);
            }

            // Colors based on event type
            switch (me.getEventType() != null ? me.getEventType().toUpperCase() : "PERSONAL") {
                case "AVAILABLE" -> {
                    dto.setBackgroundColor("#10B981");
                    dto.setBorderColor("#059669");
                }
                case "BLOCKED" -> {
                    dto.setBackgroundColor("#64748B");
                    dto.setBorderColor("#475569");
                }
                case "SESSION" -> {
                    dto.setBackgroundColor("#2563EB");
                    dto.setBorderColor("#1D4ED8");
                }
                default -> { // PERSONAL
                    dto.setBackgroundColor("#6366F1");
                    dto.setBorderColor("#4F46E5");
                }
            }
            events.add(dto);
        }

        // 2. Real Booking Events
        if (targetUser.getRole() == UserRole.PHOTOGRAPHER) {
            Optional<PhotographerProfile> profileOpt = photographerProfileRepository.findByUserId(targetUserId);
            if (profileOpt.isPresent()) {
                PhotographerProfile profile = profileOpt.get();
                List<Booking> bookings = bookingRepository.findByPhotographerProfileIdOrderByBookingDateAscBookingTimeAsc(profile.getId());

                for (Booking b : bookings) {
                    if (b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.REJECTED) {
                        // Don't clutter calendar with cancelled or declined bookings
                        continue;
                    }

                    LocalDate bDate = b.getBookingDate();
                    LocalTime bTime = b.getBookingTime() != null ? b.getBookingTime() : LocalTime.of(9, 0);
                    LocalDateTime startDt = LocalDateTime.of(bDate, bTime);
                    int duration = (b.getDurationMinutes() != null && b.getDurationMinutes() > 0) ? b.getDurationMinutes() : 120;
                    LocalDateTime endDt = startDt.plusMinutes(duration);

                    CalendarEventDto dto = new CalendarEventDto();
                    dto.setId("booking-" + b.getId());
                    dto.setStart(startDt.format(ISO_FMT));
                    dto.setEnd(endDt.format(ISO_FMT));
                    dto.setAllDay(false);
                    dto.addExtendedProp("isBooking", true);
                    dto.addExtendedProp("bookingId", b.getId());
                    dto.addExtendedProp("bookingStatus", b.getStatus().name());
                    dto.addExtendedProp("editable", false);

                    if (isOwner) {
                        String custName = b.getCustomer() != null ? b.getCustomer().getFullName() : "Client";
                        dto.setTitle("Shoot: " + custName + " (" + b.getStatus() + ")");
                        dto.setDescription("Location: " + (b.getLocation() != null ? b.getLocation() : "TBD") +
                                (b.getNotes() != null ? " • Notes: " + b.getNotes() : ""));
                        dto.addExtendedProp("location", b.getLocation());
                        dto.addExtendedProp("clientName", custName);
                    } else {
                        dto.setTitle("Reserved Shoot");
                        dto.setDescription(null);
                    }

                    if (b.getStatus() == BookingStatus.COMPLETED) {
                        dto.setBackgroundColor("#0F172A"); // Midnight deep
                        dto.setBorderColor("#1E293B");
                    } else if (b.getStatus() == BookingStatus.PENDING) {
                        dto.setBackgroundColor("#D97706"); // Warning amber
                        dto.setBorderColor("#B45309");
                    } else {
                        dto.setBackgroundColor("#2563EB"); // Royal blue
                        dto.setBorderColor("#1D4ED8");
                    }
                    events.add(dto);
                }

                // 3. Photographer Unavailable Dates
                List<PhotographerUnavailableDate> unavailables = unavailableDateRepository.findByPhotographerProfileIdOrderByDateAsc(profile.getId());
                for (PhotographerUnavailableDate ud : unavailables) {
                    LocalDate uDate = ud.getDate();
                    LocalDateTime startDt = uDate.atStartOfDay();
                    LocalDateTime endDt = uDate.atTime(23, 59, 59);

                    CalendarEventDto dto = new CalendarEventDto();
                    dto.setId("unavail-" + ud.getId());
                    dto.setStart(startDt.format(ISO_FMT));
                    dto.setEnd(endDt.format(ISO_FMT));
                    dto.setAllDay(true);
                    dto.addExtendedProp("isBooking", false);
                    dto.addExtendedProp("editable", false);
                    dto.setBackgroundColor("#94A3B8");
                    dto.setBorderColor("#64748B");

                    if (isOwner) {
                        dto.setTitle("Blocked: " + (ud.getReason() != null && !ud.getReason().isBlank() ? ud.getReason() : "Day Off"));
                        dto.setDescription(ud.getReason());
                    } else {
                        dto.setTitle("Unavailable");
                        dto.setDescription(null);
                    }
                    events.add(dto);
                }
            }
        } else if (isOwner) {
            // Customer viewing their own calendar: display sessions they have booked
            List<Booking> myBookings = bookingRepository.findByCustomerIdOrderByBookingDateDesc(targetUserId);
            for (Booking b : myBookings) {
                if (b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.REJECTED) {
                    continue;
                }

                LocalDate bDate = b.getBookingDate();
                LocalTime bTime = b.getBookingTime() != null ? b.getBookingTime() : LocalTime.of(9, 0);
                LocalDateTime startDt = LocalDateTime.of(bDate, bTime);
                int duration = (b.getDurationMinutes() != null && b.getDurationMinutes() > 0) ? b.getDurationMinutes() : 120;
                LocalDateTime endDt = startDt.plusMinutes(duration);

                CalendarEventDto dto = new CalendarEventDto();
                dto.setId("booking-" + b.getId());
                dto.setStart(startDt.format(ISO_FMT));
                dto.setEnd(endDt.format(ISO_FMT));
                dto.setAllDay(false);
                dto.addExtendedProp("isBooking", true);
                dto.addExtendedProp("bookingId", b.getId());
                dto.addExtendedProp("bookingStatus", b.getStatus().name());
                dto.addExtendedProp("editable", false);

                String photoName = b.getPhotographerProfile() != null ? b.getPhotographerProfile().getDisplayName() : "Photographer";
                dto.setTitle("Session w/ " + photoName);
                dto.setDescription("Location: " + (b.getLocation() != null ? b.getLocation() : "TBD"));
                dto.setBackgroundColor("#2563EB");
                dto.setBorderColor("#1D4ED8");
                events.add(dto);
            }
        }

        return events;
    }

    @Override
    @Transactional
    public CalendarEventDto createEvent(Long currentUserId, CalendarEventRequest request) {
        if (!request.isValidRange()) {
            throw new IllegalArgumentException("End date and time must be strictly after the start date and time.");
        }

        CalendarEvent event = new CalendarEvent(
                currentUserId,
                request.getTitle().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.getStartAt(),
                request.getEndAt(),
                request.getEventType(),
                request.getVisibility()
        );

        CalendarEvent saved = calendarEventRepository.save(event);
        log.info("Created calendar event [{}] for user [{}]", saved.getId(), currentUserId);

        CalendarEventDto dto = new CalendarEventDto();
        dto.setId("manual-" + saved.getId());
        dto.setTitle(saved.getTitle());
        dto.setDescription(saved.getDescription());
        dto.setStart(saved.getStartAt().format(ISO_FMT));
        dto.setEnd(saved.getEndAt().format(ISO_FMT));
        dto.setAllDay(false);
        dto.addExtendedProp("isBooking", false);
        dto.addExtendedProp("manualEventId", saved.getId());
        dto.addExtendedProp("eventType", saved.getEventType());
        dto.addExtendedProp("visibility", saved.getVisibility());
        dto.addExtendedProp("editable", true);
        return dto;
    }

    @Override
    @Transactional
    public CalendarEventDto updateEvent(Long currentUserId, Long eventId, CalendarEventRequest request) {
        CalendarEvent event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Calendar event not found with id: " + eventId));

        if (!event.getUserId().equals(currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to modify this event.");
        }

        if (!request.isValidRange()) {
            throw new IllegalArgumentException("End date and time must be strictly after the start date and time.");
        }

        event.setTitle(request.getTitle().trim());
        event.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        event.setStartAt(request.getStartAt());
        event.setEndAt(request.getEndAt());
        event.setEventType(request.getEventType());
        event.setVisibility(request.getVisibility());

        CalendarEvent saved = calendarEventRepository.save(event);
        log.info("Updated calendar event [{}] for user [{}]", saved.getId(), currentUserId);

        CalendarEventDto dto = new CalendarEventDto();
        dto.setId("manual-" + saved.getId());
        dto.setTitle(saved.getTitle());
        dto.setDescription(saved.getDescription());
        dto.setStart(saved.getStartAt().format(ISO_FMT));
        dto.setEnd(saved.getEndAt().format(ISO_FMT));
        dto.setAllDay(false);
        dto.addExtendedProp("isBooking", false);
        dto.addExtendedProp("manualEventId", saved.getId());
        dto.addExtendedProp("eventType", saved.getEventType());
        dto.addExtendedProp("visibility", saved.getVisibility());
        dto.addExtendedProp("editable", true);
        return dto;
    }

    @Override
    @Transactional
    public void deleteEvent(Long currentUserId, Long eventId) {
        CalendarEvent event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Calendar event not found with id: " + eventId));

        if (!event.getUserId().equals(currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to delete this event.");
        }

        calendarEventRepository.delete(event);
        log.info("Deleted calendar event [{}] by user [{}]", eventId, currentUserId);
    }
}
