package com.photoconnect.service;

import com.photoconnect.dto.TimeSlotDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.CalendarEvent;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerUnavailableDate;
import com.photoconnect.entity.User;
import com.photoconnect.exception.PhotographerUnavailableException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.CalendarEventRepository;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.PhotographerUnavailableDateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {

    @Mock
    private PhotographerUnavailableDateRepository scheduleRepository;

    @Mock
    private PhotographerProfileRepository profileRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CalendarEventRepository calendarEventRepository;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private PhotographerProfile profile;
    private User photographerUser;

    @BeforeEach
    void setUp() {
        photographerUser = new User();
        photographerUser.setId(20L);

        profile = new PhotographerProfile();
        profile.setId(1L);
        profile.setUser(photographerUser);
    }

    @Test
    void addUnavailableDate_ValidDate_Success() {
        LocalDate date = LocalDate.now().plusDays(1);
        
        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, date)).thenReturn(false);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));

        scheduleService.addUnavailableDate(1L, date, "Vacation");

        verify(scheduleRepository, times(1)).save(any(PhotographerUnavailableDate.class));
    }

    @Test
    void addUnavailableDate_PastDate_ThrowsException() {
        LocalDate date = LocalDate.now().minusDays(1);
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            scheduleService.addUnavailableDate(1L, date, "Vacation");
        });
        
        assertEquals("Cannot block dates in the past", ex.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void addUnavailableDate_DuplicateDate_ThrowsException() {
        LocalDate date = LocalDate.now().plusDays(1);
        
        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, date)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            scheduleService.addUnavailableDate(1L, date, "Vacation");
        });
        
        assertEquals("Date is already marked as unavailable", ex.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void addUnavailableDate_NullDate_ThrowsValidationException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.addUnavailableDate(1L, null, "Vacation"));

        assertEquals("Photographer profile and date are required", ex.getMessage());
        verifyNoInteractions(scheduleRepository, profileRepository);
    }

    @Test
    void addUnavailableDate_OversizedReason_ThrowsValidationException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.addUnavailableDate(1L, LocalDate.now().plusDays(1), "x".repeat(256)));

        assertEquals("Reason cannot exceed 255 characters", ex.getMessage());
        verifyNoInteractions(scheduleRepository, profileRepository);
    }

    @Test
    void isDateAvailable_Available_ReturnsTrue() {
        LocalDate date = LocalDate.now().plusDays(1);
        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, date)).thenReturn(false);

        boolean available = scheduleService.isDateAvailable(1L, date);

        assertTrue(available);
    }

    @Test
    void isDateAvailable_Unavailable_ReturnsFalse() {
        LocalDate date = LocalDate.now().plusDays(1);
        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, date)).thenReturn(true);

        boolean available = scheduleService.isDateAvailable(1L, date);

        assertFalse(available);
    }

    @Test
    void isDateAvailable_NullInput_ReturnsFalseWithoutQuery() {
        assertFalse(scheduleService.isDateAvailable(1L, null));
        verifyNoInteractions(scheduleRepository);
    }

    // ── Phase 2 Slot Conflict & Availability Engine Tests ────────────────

    @Test
    void validateSlotAvailability_ValidSlot_Success() {
        LocalDate shootDate = LocalDate.now().plusDays(3);
        LocalTime shootTime = LocalTime.of(14, 0);

        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, shootDate)).thenReturn(false);
        when(bookingRepository.findActiveBookingsOnDate(eq(1L), eq(shootDate), any(), isNull()))
                .thenReturn(Collections.emptyList());
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(calendarEventRepository.findConflictingEvents(eq(20L), any(), any()))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() ->
                scheduleService.validateSlotAvailability(1L, shootDate, shootTime, 120, null));
        assertTrue(scheduleService.isSlotAvailable(1L, shootDate, shootTime, 120));
    }

    @Test
    void validateSlotAvailability_PastDate_ThrowsException() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        LocalTime shootTime = LocalTime.of(10, 0);

        assertThrows(PhotographerUnavailableException.class, () ->
                scheduleService.validateSlotAvailability(1L, pastDate, shootTime, 120, null));
        assertFalse(scheduleService.isSlotAvailable(1L, pastDate, shootTime, 120));
    }

    @Test
    void validateSlotAvailability_WholeDayBlocked_ThrowsException() {
        LocalDate shootDate = LocalDate.now().plusDays(2);
        LocalTime shootTime = LocalTime.of(10, 0);

        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, shootDate)).thenReturn(true);

        assertThrows(PhotographerUnavailableException.class, () ->
                scheduleService.validateSlotAvailability(1L, shootDate, shootTime, 120, null));
    }

    @Test
    void validateSlotAvailability_ExactCollisionWithExistingAcceptedBooking_ThrowsException() {
        LocalDate shootDate = LocalDate.now().plusDays(4);
        LocalTime shootTime = LocalTime.of(10, 0);

        Booking existing = new Booking();
        existing.setId(50L);
        existing.setBookingDate(shootDate);
        existing.setBookingTime(LocalTime.of(10, 0));
        existing.setDurationMinutes(120); // 10:00 to 12:00
        existing.setStatus(BookingStatus.ACCEPTED);

        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, shootDate)).thenReturn(false);
        when(bookingRepository.findActiveBookingsOnDate(eq(1L), eq(shootDate), any(), isNull()))
                .thenReturn(List.of(existing));

        // Proposed: 10:00 to 12:00 (exact collision)
        assertThrows(PhotographerUnavailableException.class, () ->
                scheduleService.validateSlotAvailability(1L, shootDate, shootTime, 120, null));
    }

    @Test
    void validateSlotAvailability_PartialCollisionStart_ThrowsException() {
        LocalDate shootDate = LocalDate.now().plusDays(4);

        Booking existing = new Booking();
        existing.setId(50L);
        existing.setBookingDate(shootDate);
        existing.setBookingTime(LocalTime.of(10, 0));
        existing.setDurationMinutes(120); // 10:00 to 12:00
        existing.setStatus(BookingStatus.ACCEPTED);

        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, shootDate)).thenReturn(false);
        when(bookingRepository.findActiveBookingsOnDate(eq(1L), eq(shootDate), any(), isNull()))
                .thenReturn(List.of(existing));

        // Proposed: 11:00 to 13:00 (starts inside existing 10:00-12:00 window)
        assertThrows(PhotographerUnavailableException.class, () ->
                scheduleService.validateSlotAvailability(1L, shootDate, LocalTime.of(11, 0), 120, null));
    }

    @Test
    void validateSlotAvailability_PartialCollisionEnd_ThrowsException() {
        LocalDate shootDate = LocalDate.now().plusDays(4);

        Booking existing = new Booking();
        existing.setId(50L);
        existing.setBookingDate(shootDate);
        existing.setBookingTime(LocalTime.of(10, 0));
        existing.setDurationMinutes(120); // 10:00 to 12:00
        existing.setStatus(BookingStatus.ACCEPTED);

        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, shootDate)).thenReturn(false);
        when(bookingRepository.findActiveBookingsOnDate(eq(1L), eq(shootDate), any(), isNull()))
                .thenReturn(List.of(existing));

        // Proposed: 09:00 to 11:00 (ends inside existing 10:00-12:00 window)
        assertThrows(PhotographerUnavailableException.class, () ->
                scheduleService.validateSlotAvailability(1L, shootDate, LocalTime.of(9, 0), 120, null));
    }

    @Test
    void validateSlotAvailability_AdjacentSlots_DoNotCollide() {
        LocalDate shootDate = LocalDate.now().plusDays(4);

        Booking existing = new Booking();
        existing.setId(50L);
        existing.setBookingDate(shootDate);
        existing.setBookingTime(LocalTime.of(10, 0));
        existing.setDurationMinutes(120); // 10:00 to 12:00
        existing.setStatus(BookingStatus.ACCEPTED);

        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, shootDate)).thenReturn(false);
        when(bookingRepository.findActiveBookingsOnDate(eq(1L), eq(shootDate), any(), isNull()))
                .thenReturn(List.of(existing));
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(calendarEventRepository.findConflictingEvents(eq(20L), any(), any()))
                .thenReturn(Collections.emptyList());

        // Slot immediately before: 08:00 to 10:00 (adjacent at 10:00, no overlap)
        assertDoesNotThrow(() ->
                scheduleService.validateSlotAvailability(1L, shootDate, LocalTime.of(8, 0), 120, null));

        // Slot immediately after: 12:00 to 14:00 (adjacent at 12:00, no overlap)
        assertDoesNotThrow(() ->
                scheduleService.validateSlotAvailability(1L, shootDate, LocalTime.of(12, 0), 120, null));
    }

    @Test
    void validateSlotAvailability_CalendarBlockedEvent_ThrowsException() {
        LocalDate shootDate = LocalDate.now().plusDays(5);
        LocalTime shootTime = LocalTime.of(14, 0);

        CalendarEvent blocked = new CalendarEvent();
        blocked.setId(99L);
        blocked.setUserId(20L);
        blocked.setEventType("BLOCKED");
        blocked.setStartAt(LocalDateTime.of(shootDate, LocalTime.of(13, 0)));
        blocked.setEndAt(LocalDateTime.of(shootDate, LocalTime.of(16, 0)));

        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, shootDate)).thenReturn(false);
        when(bookingRepository.findActiveBookingsOnDate(eq(1L), eq(shootDate), any(), isNull()))
                .thenReturn(Collections.emptyList());
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(calendarEventRepository.findConflictingEvents(eq(20L), any(), any()))
                .thenReturn(List.of(blocked));

        assertThrows(PhotographerUnavailableException.class, () ->
                scheduleService.validateSlotAvailability(1L, shootDate, shootTime, 120, null));
    }

    @Test
    void getAvailableTimeSlots_ReturnsSlotList() {
        LocalDate shootDate = LocalDate.now().plusDays(5);

        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, shootDate)).thenReturn(false);
        when(bookingRepository.findActiveBookingsOnDate(eq(1L), eq(shootDate), any(), isNull()))
                .thenReturn(Collections.emptyList());
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(calendarEventRepository.findConflictingEvents(eq(20L), any(), any()))
                .thenReturn(Collections.emptyList());

        List<TimeSlotDto> slots = scheduleService.getAvailableTimeSlots(1L, shootDate, 120);

        assertNotNull(slots);
        assertFalse(slots.isEmpty());
        assertTrue(slots.stream().allMatch(TimeSlotDto::isAvailable));
    }

    @Test
    void validateSlotAvailability_eachTimeReservingStatus_blocksOverlappingBooking() {
        LocalDate shootDate = LocalDate.now().plusDays(4);
        LocalTime shootTime = LocalTime.of(10, 0);

        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, shootDate)).thenReturn(false);

        for (BookingStatus status : BookingStatus.TIME_RESERVING_STATUSES) {
            Booking existing = new Booking();
            existing.setId(50L);
            existing.setBookingDate(shootDate);
            existing.setBookingTime(LocalTime.of(10, 0));
            existing.setDurationMinutes(120);
            existing.setStatus(status);

            when(bookingRepository.findActiveBookingsOnDate(eq(1L), eq(shootDate), eq(BookingStatus.TIME_RESERVING_STATUSES_LIST), isNull()))
                    .thenReturn(List.of(existing));

            PhotographerUnavailableException ex = assertThrows(PhotographerUnavailableException.class, () ->
                    scheduleService.validateSlotAvailability(1L, shootDate, shootTime, 120, null),
                    "Status " + status + " should block overlapping bookings"
            );
            assertTrue(ex.getMessage().contains("conflicts with an existing confirmed booking"));
        }
    }

    @Test
    void validateSlotAvailability_queriesWithAuthoritativeTimeReservingStatuses() {
        LocalDate shootDate = LocalDate.now().plusDays(4);
        LocalTime shootTime = LocalTime.of(14, 0);

        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, shootDate)).thenReturn(false);
        when(bookingRepository.findActiveBookingsOnDate(eq(1L), eq(shootDate), eq(BookingStatus.TIME_RESERVING_STATUSES_LIST), isNull()))
                .thenReturn(Collections.emptyList());
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(calendarEventRepository.findConflictingEvents(eq(20L), any(), any()))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() ->
                scheduleService.validateSlotAvailability(1L, shootDate, shootTime, 120, null));

        verify(bookingRepository).findActiveBookingsOnDate(eq(1L), eq(shootDate), eq(BookingStatus.TIME_RESERVING_STATUSES_LIST), isNull());
    }
}
