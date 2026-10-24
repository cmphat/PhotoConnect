package com.photoconnect.service;

import com.photoconnect.dto.CalendarEventDto;
import com.photoconnect.dto.CalendarEventRequest;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.CalendarEvent;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.CalendarEventRepository;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.PhotographerUnavailableDateRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private CalendarEventRepository calendarEventRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PhotographerUnavailableDateRepository unavailableDateRepository;

    @Mock
    private PhotographerProfileRepository photographerProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CalendarServiceImpl calendarService;

    private User photographer;
    private PhotographerProfile profile;
    private User customer;

    @BeforeEach
    void setUp() {
        photographer = new User();
        ReflectionTestUtils.setField(photographer, "id", 10L);
        photographer.setFullName("Sam Photo");
        photographer.setRole(UserRole.PHOTOGRAPHER);
        photographer.setStatus(UserStatus.ACTIVE);

        profile = new PhotographerProfile();
        ReflectionTestUtils.setField(profile, "id", 100L);
        profile.setUser(photographer);

        customer = new User();
        ReflectionTestUtils.setField(customer, "id", 20L);
        customer.setFullName("Emma Client");
        customer.setRole(UserRole.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);
    }

    @Test
    void getCalendarEvents_asOwner_returnsFullManualAndBookingEvents() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(photographer));

        CalendarEvent event = new CalendarEvent();
        ReflectionTestUtils.setField(event, "id", 501L);
        event.setUserId(10L);
        event.setTitle("Studio Equipment Prep");
        event.setDescription("Clean lenses and charge batteries");
        event.setStartAt(LocalDateTime.of(2026, 11, 1, 9, 0));
        event.setEndAt(LocalDateTime.of(2026, 11, 1, 11, 0));
        event.setVisibility("PRIVATE");
        event.setEventType("personal");

        when(calendarEventRepository.findByUserIdOrderByStartAtAsc(10L))
                .thenReturn(List.of(event));
        when(photographerProfileRepository.findByUserId(10L)).thenReturn(Optional.of(profile));

        Booking booking = new Booking();
        ReflectionTestUtils.setField(booking, "id", 77L);
        booking.setCustomer(customer);
        booking.setPhotographerProfile(profile);
        booking.setBookingDate(LocalDate.of(2026, 11, 2));
        booking.setBookingTime(LocalTime.of(14, 0));
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setLocation("Shinjuku Gyoen");

        when(bookingRepository.findByPhotographerProfileIdOrderByBookingDateAscBookingTimeAsc(100L))
                .thenReturn(List.of(booking));
        when(unavailableDateRepository.findByPhotographerProfileIdOrderByDateAsc(100L))
                .thenReturn(List.of());

        List<CalendarEventDto> events = calendarService.getCalendarEvents(10L, 10L);

        assertThat(events).hasSize(2);

        // Owner sees private manual event with title and description
        CalendarEventDto manualDto = events.get(0);
        assertThat(manualDto.getTitle()).isEqualTo("Studio Equipment Prep");
        assertThat(manualDto.getDescription()).isEqualTo("Clean lenses and charge batteries");

        // Owner sees client name and booking details
        CalendarEventDto bookingDto = events.get(1);
        assertThat(bookingDto.getTitle()).contains("Emma Client");
        assertThat(bookingDto.getDescription()).contains("Shinjuku Gyoen");
    }

    @Test
    void getCalendarEvents_asVisitor_masksPrivateEventTitlesAndHidesBookingDetails() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(photographer));

        CalendarEvent privateEvent = new CalendarEvent();
        ReflectionTestUtils.setField(privateEvent, "id", 502L);
        privateEvent.setUserId(10L);
        privateEvent.setTitle("Doctor Appointment");
        privateEvent.setDescription("Dental checkup");
        privateEvent.setStartAt(LocalDateTime.of(2026, 11, 5, 10, 0));
        privateEvent.setEndAt(LocalDateTime.of(2026, 11, 5, 12, 0));
        privateEvent.setVisibility("PUBLIC");
        privateEvent.setEventType("personal");

        // Visitor only gets public events via repository query
        when(calendarEventRepository.findByUserIdAndVisibilityOrderByStartAtAsc(10L, "PUBLIC"))
                .thenReturn(List.of(privateEvent));
        when(photographerProfileRepository.findByUserId(10L)).thenReturn(Optional.of(profile));

        Booking booking = new Booking();
        ReflectionTestUtils.setField(booking, "id", 88L);
        booking.setCustomer(customer);
        booking.setPhotographerProfile(profile);
        booking.setBookingDate(LocalDate.of(2026, 11, 6));
        booking.setBookingTime(LocalTime.of(15, 0));
        booking.setStatus(BookingStatus.ACCEPTED);

        when(bookingRepository.findByPhotographerProfileIdOrderByBookingDateAscBookingTimeAsc(100L))
                .thenReturn(List.of(booking));
        when(unavailableDateRepository.findByPhotographerProfileIdOrderByDateAsc(100L))
                .thenReturn(List.of());

        // Visitor user ID = 999L (not owner)
        List<CalendarEventDto> events = calendarService.getCalendarEvents(10L, 999L);

        assertThat(events).hasSize(2);

        // Booking details sanitized for visitor: no client name or internal notes
        CalendarEventDto maskedBooking = events.get(1);
        assertThat(maskedBooking.getTitle()).isEqualTo("Reserved Shoot");
        assertThat(maskedBooking.getDescription()).isNull();
    }

    @Test
    void createEvent_persistsValidEvent() {
        CalendarEventRequest req = new CalendarEventRequest();
        req.setTitle("Portfolio Shooting Day");
        req.setStartAt(LocalDateTime.of(2026, 11, 10, 8, 0));
        req.setEndAt(LocalDateTime.of(2026, 11, 10, 17, 0));
        req.setEventType("shoot");
        req.setVisibility("PUBLIC");

        when(calendarEventRepository.save(any(CalendarEvent.class))).thenAnswer(invocation -> {
            CalendarEvent saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 600L);
            return saved;
        });

        CalendarEventDto created = calendarService.createEvent(10L, req);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo("manual-600");
        assertThat(created.getTitle()).isEqualTo("Portfolio Shooting Day");
        verify(calendarEventRepository).save(any(CalendarEvent.class));
    }

    @Test
    void createEvent_rejectsInvalidTimeRange() {
        CalendarEventRequest req = new CalendarEventRequest();
        req.setTitle("Bad Event");
        req.setStartAt(LocalDateTime.of(2026, 11, 10, 18, 0));
        req.setEndAt(LocalDateTime.of(2026, 11, 10, 10, 0)); // end is before start

        assertThatThrownBy(() -> calendarService.createEvent(10L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date and time must be strictly after the start date and time");
    }

    @Test
    void deleteEvent_enforcesOwnership() {
        CalendarEvent event = new CalendarEvent();
        ReflectionTestUtils.setField(event, "id", 999L);
        event.setUserId(10L); // Owner is user 10

        when(calendarEventRepository.findById(999L)).thenReturn(Optional.of(event));

        // Attempt deletion by user 55
        assertThatThrownBy(() -> calendarService.deleteEvent(55L, 999L))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("You are not authorized to delete this event");
    }
}
