package com.photoconnect.service;

import com.photoconnect.dto.RescheduleRequestDto;
import com.photoconnect.entity.*;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingHistoryRepository;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.RescheduleRequestRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RescheduleServiceTest {

    @Mock
    private RescheduleRequestRepository rescheduleRequestRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private BookingHistoryRepository bookingHistoryRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RescheduleServiceImpl rescheduleService;

    private User customer;
    private User photogUser;
    private PhotographerProfile profile;
    private Booking booking;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(10L);
        customer.setFullName("Alice Customer");
        customer.setRole(UserRole.CUSTOMER);

        photogUser = new User();
        photogUser.setId(20L);
        photogUser.setFullName("Bob Photographer");
        photogUser.setRole(UserRole.PHOTOGRAPHER);

        profile = new PhotographerProfile();
        profile.setId(100L);
        profile.setUser(photogUser);
        profile.setDisplayName("Bob Studio");

        booking = new Booking();
        booking.setId(500L);
        booking.setCustomer(customer);
        booking.setPhotographerProfile(profile);
        booking.setBookingDate(LocalDate.now().plusDays(5));
        booking.setBookingTime(LocalTime.of(10, 0));
        booking.setDurationMinutes(120);
        booking.setAgreedPrice(BigDecimal.valueOf(1500000));
        booking.setStatus(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("createRescheduleRequest: success when initiated by customer for confirmed booking")
    void createRescheduleRequest_CustomerSuccess() {
        LocalDate newDate = LocalDate.now().plusDays(7);
        LocalTime newTime = LocalTime.of(14, 0);

        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(rescheduleRequestRepository.existsByBookingIdAndStatus(500L, RescheduleStatus.PENDING)).thenReturn(false);
        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(rescheduleRequestRepository.save(any(RescheduleRequest.class))).thenAnswer(inv -> {
            RescheduleRequest req = inv.getArgument(0);
            req.setId(1L);
            req.setCreatedAt(LocalDateTime.now());
            return req;
        });

        RescheduleRequestDto dto = rescheduleService.createRescheduleRequest(500L, 10L, newDate, newTime, "Weather conflict");

        assertThat(dto).isNotNull();
        assertThat(dto.getBookingId()).isEqualTo(500L);
        assertThat(dto.getProposedDate()).isEqualTo(newDate);
        assertThat(dto.getProposedTime()).isEqualTo(newTime);
        assertThat(dto.getStatus()).isEqualTo(RescheduleStatus.PENDING);

        verify(scheduleService).validateSlotAvailability(100L, newDate, newTime, 120, 500L);
        verify(notificationService).sendNotification(eq(20L), eq(NotificationType.SCHEDULE_CHANGED), anyString(), anyString(), eq(500L), anyString());
    }

    @Test
    @DisplayName("createRescheduleRequest: throws UnauthorizedException when caller is not booking participant")
    void createRescheduleRequest_UnauthorizedCaller() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> rescheduleService.createRescheduleRequest(500L, 999L, LocalDate.now().plusDays(6), LocalTime.of(10, 0), "conflict"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("createRescheduleRequest: throws when booking is completed or cancelled")
    void createRescheduleRequest_InvalidBookingState() {
        booking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> rescheduleService.createRescheduleRequest(500L, 10L, LocalDate.now().plusDays(6), LocalTime.of(10, 0), "test"))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Rescheduling is only permitted for active sessions");
    }

    @Test
    @DisplayName("createRescheduleRequest: throws when active pending reschedule already exists")
    void createRescheduleRequest_DuplicatePending() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(rescheduleRequestRepository.existsByBookingIdAndStatus(500L, RescheduleStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> rescheduleService.createRescheduleRequest(500L, 10L, LocalDate.now().plusDays(6), LocalTime.of(10, 0), "test"))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("A pending reschedule request already exists");
    }

    @Test
    @DisplayName("acceptReschedule: photographer accepts customer proposal, schedule updates")
    void acceptReschedule_Success() {
        LocalDate newDate = LocalDate.now().plusDays(8);
        LocalTime newTime = LocalTime.of(15, 0);

        RescheduleRequest req = new RescheduleRequest(booking, customer, newDate, newTime, "Schedule adjust");
        req.setId(101L);
        req.setStatus(RescheduleStatus.PENDING);

        when(rescheduleRequestRepository.findByIdWithDetails(101L)).thenReturn(Optional.of(req));
        when(userRepository.findById(20L)).thenReturn(Optional.of(photogUser));

        rescheduleService.acceptReschedule(101L, 20L, "Agreed on new time");

        assertThat(booking.getBookingDate()).isEqualTo(newDate);
        assertThat(booking.getBookingTime()).isEqualTo(newTime);
        assertThat(req.getStatus()).isEqualTo(RescheduleStatus.ACCEPTED);
        assertThat(req.getResponseNote()).isEqualTo("Agreed on new time");

        verify(bookingRepository).save(booking);
        verify(rescheduleRequestRepository).save(req);
        verify(bookingHistoryRepository).save(any(BookingHistory.class));
        verify(notificationService).sendNotification(eq(10L), eq(NotificationType.SCHEDULE_CHANGED), anyString(), anyString(), eq(500L), anyString());
    }

    @Test
    @DisplayName("acceptReschedule: requester cannot accept their own request")
    void acceptReschedule_CannotAcceptOwn() {
        RescheduleRequest req = new RescheduleRequest(booking, customer, LocalDate.now().plusDays(8), LocalTime.of(15, 0), "adjust");
        req.setId(101L);
        req.setStatus(RescheduleStatus.PENDING);

        when(rescheduleRequestRepository.findByIdWithDetails(101L)).thenReturn(Optional.of(req));

        assertThatThrownBy(() -> rescheduleService.acceptReschedule(101L, 10L, "Accept"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("cannot accept your own reschedule request");
    }

    @Test
    @DisplayName("rejectReschedule: opposing party declines request")
    void rejectReschedule_Success() {
        RescheduleRequest req = new RescheduleRequest(booking, customer, LocalDate.now().plusDays(8), LocalTime.of(15, 0), "adjust");
        req.setId(101L);
        req.setStatus(RescheduleStatus.PENDING);

        when(rescheduleRequestRepository.findByIdWithDetails(101L)).thenReturn(Optional.of(req));
        when(userRepository.findById(20L)).thenReturn(Optional.of(photogUser));

        rescheduleService.rejectReschedule(101L, 20L, "Fully booked that afternoon");

        assertThat(req.getStatus()).isEqualTo(RescheduleStatus.REJECTED);
        assertThat(req.getResponseNote()).isEqualTo("Fully booked that afternoon");
        verify(rescheduleRequestRepository).save(req);
        verify(notificationService).sendNotification(eq(10L), eq(NotificationType.SCHEDULE_CHANGED), anyString(), anyString(), eq(500L), anyString());
    }

    @Test
    @DisplayName("cancelReschedule: requester withdraws own request")
    void cancelReschedule_Success() {
        RescheduleRequest req = new RescheduleRequest(booking, customer, LocalDate.now().plusDays(8), LocalTime.of(15, 0), "adjust");
        req.setId(101L);
        req.setStatus(RescheduleStatus.PENDING);

        when(rescheduleRequestRepository.findByIdWithDetails(101L)).thenReturn(Optional.of(req));

        rescheduleService.cancelReschedule(101L, 10L);

        assertThat(req.getStatus()).isEqualTo(RescheduleStatus.CANCELLED);
        verify(rescheduleRequestRepository).save(req);
    }
}
