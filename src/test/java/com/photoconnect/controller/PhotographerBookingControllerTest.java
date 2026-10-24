package com.photoconnect.controller;

import com.photoconnect.entity.*;
import com.photoconnect.exception.*;
import com.photoconnect.service.BookingService;
import com.photoconnect.service.BookingStateMachineService;
import com.photoconnect.service.RescheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PhotographerBookingController.class)
@Import(GlobalExceptionHandler.class)
class PhotographerBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private BookingStateMachineService bookingStateMachineService;

    @MockBean
    private RescheduleService rescheduleService;

    private MockHttpSession photographerSession;
    private Booking sampleBooking;
    private User assignedPhotographerUser;
    private PhotographerProfile assignedProfile;

    @BeforeEach
    void setUp() {
        // Authenticated as Photographer 2 (userId = 2L)
        photographerSession = new MockHttpSession();
        photographerSession.setAttribute("userId", 2L);
        photographerSession.setAttribute("userRole", "PHOTOGRAPHER");
        photographerSession.setAttribute("userFullName", "Photographer Intruder");

        // Booking belongs to Photographer 1 (userId = 1L)
        assignedPhotographerUser = new User();
        assignedPhotographerUser.setId(1L);
        assignedPhotographerUser.setFullName("Photographer Owner");
        assignedPhotographerUser.setRole(UserRole.PHOTOGRAPHER);

        assignedProfile = new PhotographerProfile();
        assignedProfile.setId(10L);
        assignedProfile.setUser(assignedPhotographerUser);

        User customer = new User();
        customer.setId(99L);
        customer.setFullName("Customer Alice");
        customer.setRole(UserRole.CUSTOMER);

        sampleBooking = new Booking(customer, assignedProfile, LocalDate.now().plusDays(3),
                LocalTime.of(10, 0), "Downtown", "Test shoot", new BigDecimal("2000000"));
        sampleBooking.setId(100L);
        sampleBooking.setStatus(BookingStatus.PENDING);
    }

    @Nested
    @DisplayName("SEC-01: Negative Authorization Tests (Cross-Photographer Modification)")
    class CrossPhotographerAuthorizationTests {

        @Test
        @DisplayName("GET /photographer/bookings/{id} by non-owner photographer should return 403 Forbidden")
        void viewBooking_byNonOwnerPhotographer_shouldReturn403() throws Exception {
            when(bookingService.getBookingForPhotographer(100L, 2L))
                    .thenThrow(new InvalidBookingException(ErrorCode.BOOKING_005_NOT_OWNER, "You are not authorized to view this booking."));

            mockMvc.perform(get("/photographer/bookings/100").session(photographerSession))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /photographer/bookings/{id}/accept by non-owner photographer should return 403 Forbidden")
        void acceptBooking_byNonOwnerPhotographer_shouldReturn403() throws Exception {
            doThrow(new InvalidBookingException(ErrorCode.BOOKING_005_NOT_OWNER, "You are not authorized to view this booking."))
                    .when(bookingService).acceptBooking(100L, 2L);

            mockMvc.perform(post("/photographer/bookings/100/accept").session(photographerSession))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /photographer/bookings/{id}/reject by non-owner photographer should return 403 Forbidden")
        void rejectBooking_byNonOwnerPhotographer_shouldReturn403() throws Exception {
            doThrow(new InvalidBookingException(ErrorCode.BOOKING_005_NOT_OWNER, "You are not authorized to view this booking."))
                    .when(bookingService).rejectBooking(100L, 2L);

            mockMvc.perform(post("/photographer/bookings/100/reject").session(photographerSession))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /photographer/bookings/{id}/complete by non-owner photographer should return 403 Forbidden")
        void completeBooking_byNonOwnerPhotographer_shouldReturn403() throws Exception {
            doThrow(new InvalidBookingException(ErrorCode.BOOKING_005_NOT_OWNER, "You are not authorized to view this booking."))
                    .when(bookingService).completeBooking(100L, 2L);

            mockMvc.perform(post("/photographer/bookings/100/complete").session(photographerSession))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /photographer/bookings/{id}/cancel by non-owner photographer should return 403 Forbidden")
        void cancelBooking_byNonOwnerPhotographer_shouldReturn403() throws Exception {
            doThrow(new InvalidBookingException(ErrorCode.BOOKING_005_NOT_OWNER, "You are not authorized to view this booking."))
                    .when(bookingService).cancelBookingByPhotographer(eq(100L), eq(2L), any());

            mockMvc.perform(post("/photographer/bookings/100/cancel").session(photographerSession))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /photographer/bookings/{id}/advance-status by non-owner photographer should return 403 Forbidden")
        void advanceStatus_byNonOwnerPhotographer_shouldReturn403() throws Exception {
            when(bookingService.getBookingForPhotographer(100L, 2L))
                    .thenThrow(new InvalidBookingException(ErrorCode.BOOKING_005_NOT_OWNER, "You are not authorized to view this booking."));

            mockMvc.perform(post("/photographer/bookings/100/advance-status")
                            .session(photographerSession)
                            .param("targetStatus", "SHOOTING"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /photographer/bookings/{id}/reschedule by non-owner photographer should return 403 Forbidden")
        void reschedule_byNonOwnerPhotographer_shouldReturn403() throws Exception {
            when(bookingService.getBookingForPhotographer(100L, 2L))
                    .thenThrow(new InvalidBookingException(ErrorCode.BOOKING_005_NOT_OWNER, "You are not authorized to view this booking."));

            mockMvc.perform(post("/photographer/bookings/100/reschedule")
                            .session(photographerSession)
                            .param("proposedDate", LocalDate.now().plusDays(4).toString())
                            .param("proposedTime", "14:00"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /photographer/bookings/{id}/reschedule/{requestId}/accept by non-owner photographer should return 403 Forbidden")
        void acceptReschedule_byNonOwnerPhotographer_shouldReturn403() throws Exception {
            when(bookingService.getBookingForPhotographer(100L, 2L))
                    .thenThrow(new InvalidBookingException(ErrorCode.BOOKING_005_NOT_OWNER, "You are not authorized to view this booking."));

            mockMvc.perform(post("/photographer/bookings/100/reschedule/1/accept").session(photographerSession))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /photographer/bookings/{id}/reschedule/{requestId}/reject by non-owner photographer should return 403 Forbidden")
        void rejectReschedule_byNonOwnerPhotographer_shouldReturn403() throws Exception {
            when(bookingService.getBookingForPhotographer(100L, 2L))
                    .thenThrow(new InvalidBookingException(ErrorCode.BOOKING_005_NOT_OWNER, "You are not authorized to view this booking."));

            mockMvc.perform(post("/photographer/bookings/100/reschedule/1/reject").session(photographerSession))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /photographer/bookings/{id}/reschedule/{requestId}/cancel by non-owner photographer should return 403 Forbidden")
        void cancelReschedule_byNonOwnerPhotographer_shouldReturn403() throws Exception {
            when(bookingService.getBookingForPhotographer(100L, 2L))
                    .thenThrow(new InvalidBookingException(ErrorCode.BOOKING_005_NOT_OWNER, "You are not authorized to view this booking."));

            mockMvc.perform(post("/photographer/bookings/100/reschedule/1/cancel").session(photographerSession))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("SEC-01: State Conflict Tests (409 Conflict)")
    class StateConflictTests {

        private MockHttpSession ownerSession;

        @BeforeEach
        void setUpOwner() {
            ownerSession = new MockHttpSession();
            ownerSession.setAttribute("userId", 1L);
            ownerSession.setAttribute("userRole", "PHOTOGRAPHER");
            ownerSession.setAttribute("userFullName", "Photographer Owner");
        }

        @Test
        @DisplayName("POST /photographer/bookings/{id}/accept on non-pending booking should return 409 Conflict")
        void acceptBooking_nonPendingState_shouldReturn409() throws Exception {
            doThrow(new InvalidBookingException(ErrorCode.BOOKING_003_INVALID_STATUS, "Only PENDING bookings can be accepted."))
                    .when(bookingService).acceptBooking(100L, 1L);

            mockMvc.perform(post("/photographer/bookings/100/accept").session(ownerSession))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("POST /photographer/bookings/{id}/advance-status to unauthorized target status CONFIRMED should return 409 Conflict")
        void advanceStatus_unauthorizedTargetStatus_shouldReturn409() throws Exception {
            when(bookingService.getBookingForPhotographer(100L, 1L)).thenReturn(sampleBooking);

            mockMvc.perform(post("/photographer/bookings/100/advance-status")
                            .session(ownerSession)
                            .param("targetStatus", "CONFIRMED"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("POST /photographer/bookings/{id}/advance-status with state transition conflict should return 409 Conflict")
        void advanceStatus_invalidTransition_shouldReturn409() throws Exception {
            when(bookingService.getBookingForPhotographer(100L, 1L)).thenReturn(sampleBooking);
            when(bookingStateMachineService.transition(eq(100L), eq(BookingStatus.SHOOTING), eq(1L), any()))
                    .thenThrow(new IllegalStateException("Invalid transition: cannot move booking from PENDING to SHOOTING."));

            mockMvc.perform(post("/photographer/bookings/100/advance-status")
                            .session(ownerSession)
                            .param("targetStatus", "SHOOTING"))
                    .andExpect(status().isConflict());
        }
    }
}
