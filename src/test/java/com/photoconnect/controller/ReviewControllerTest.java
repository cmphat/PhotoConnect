package com.photoconnect.controller;

import com.photoconnect.dto.ReviewDto;
import com.photoconnect.dto.ReviewRequest;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.ReviewAlreadyExistsException;
import com.photoconnect.service.BookingService;
import com.photoconnect.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private BookingService bookingService;

    private MockHttpSession session;
    private Booking completedBooking;
    private Booking acceptedBooking;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute("userId", 100L);
        session.setAttribute("userRole", "CUSTOMER");

        User customer = new User();
        customer.setId(100L);
        customer.setFullName("Alice Customer");
        customer.setEmail("alice@example.com");
        customer.setRole(UserRole.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);

        User photoUser = new User();
        photoUser.setId(200L);
        photoUser.setFullName("Bob Photographer");
        photoUser.setRole(UserRole.PHOTOGRAPHER);

        PhotographerProfile profile = new PhotographerProfile();
        profile.setId(10L);
        profile.setUser(photoUser);
        profile.setDisplayName("Bob's Lens");
        profile.setPriceFrom(new BigDecimal("1500000"));

        completedBooking = new Booking(customer, profile, LocalDate.now().minusDays(2), LocalTime.of(14, 0), "Downtown", "Notes", new BigDecimal("1500000"));
        completedBooking.setId(1L);
        completedBooking.setStatus(BookingStatus.COMPLETED);

        acceptedBooking = new Booking(customer, profile, LocalDate.now().plusDays(2), LocalTime.of(14, 0), "Downtown", "Notes", new BigDecimal("1500000"));
        acceptedBooking.setId(2L);
        acceptedBooking.setStatus(BookingStatus.ACCEPTED);
    }

    // ── GET /bookings/{id}/review ──────────────────────────────────────────

    @Test
    void showReviewForm_unauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/bookings/1/review"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void showReviewForm_wrongCustomerOrInvalidBooking_shouldRedirectToBookings() throws Exception {
        when(bookingService.getBookingForCustomer(99L, 100L))
                .thenThrow(new InvalidBookingException("Booking not found or not owned by customer"));

        mockMvc.perform(get("/bookings/99/review").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void showReviewForm_bookingNotCompleted_shouldRedirectToBookingDetail() throws Exception {
        when(bookingService.getBookingForCustomer(2L, 100L)).thenReturn(acceptedBooking);

        mockMvc.perform(get("/bookings/2/review").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/2"))
                .andExpect(flash().attribute("errorMessage", "Only completed bookings can be reviewed."));
    }

    @Test
    void showReviewForm_reviewAlreadyExists_shouldRedirectToBookingDetail() throws Exception {
        when(bookingService.getBookingForCustomer(1L, 100L)).thenReturn(completedBooking);
        when(reviewService.hasReviewForBooking(1L)).thenReturn(true);

        mockMvc.perform(get("/bookings/1/review").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/1"))
                .andExpect(flash().attribute("errorMessage", "A review has already been submitted for this booking."));
    }

    @Test
    void showReviewForm_validCompletedBooking_shouldRenderForm() throws Exception {
        when(bookingService.getBookingForCustomer(1L, 100L)).thenReturn(completedBooking);
        when(reviewService.hasReviewForBooking(1L)).thenReturn(false);

        mockMvc.perform(get("/bookings/1/review").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("booking"))
                .andExpect(model().attributeExists("reviewRequest"))
                .andExpect(forwardedUrl("/WEB-INF/views/review-form.jsp"));
    }

    // ── POST /bookings/{id}/review ─────────────────────────────────────────

    @Test
    void submitReview_unauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/bookings/1/review")
                        .param("rating", "5")
                        .param("comment", "Wonderful shoot!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(reviewService, never()).createReview(any(), any(), any());
    }

    @Test
    void submitReview_validRequest_shouldCreateReviewAndRedirect() throws Exception {
        ReviewDto reviewDto = new ReviewDto(10L, 1L, 10L, 5, "Wonderful shoot!", "Alice Customer", LocalDateTime.now());
        when(reviewService.createReview(eq(1L), eq(100L), any(ReviewRequest.class))).thenReturn(reviewDto);

        mockMvc.perform(post("/bookings/1/review")
                        .session(session)
                        .param("rating", "5")
                        .param("comment", "Wonderful shoot!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/1"))
                .andExpect(flash().attribute("successMessage", "Thank you! Your review has been submitted."));

        verify(reviewService).createReview(eq(1L), eq(100L), any(ReviewRequest.class));
    }

    @Test
    void submitReview_duplicateReview_shouldRedirectWithErrorMessage() throws Exception {
        when(reviewService.createReview(eq(1L), eq(100L), any(ReviewRequest.class)))
                .thenThrow(new ReviewAlreadyExistsException("A review has already been submitted for this booking."));

        mockMvc.perform(post("/bookings/1/review")
                        .session(session)
                        .param("rating", "5")
                        .param("comment", "Wonderful shoot!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/1"))
                .andExpect(flash().attribute("errorMessage", "A review has already been submitted for this booking."));
    }

    @Test
    void submitReview_validationErrors_shouldReturnReviewForm() throws Exception {
        when(bookingService.getBookingForCustomer(1L, 100L)).thenReturn(completedBooking);

        mockMvc.perform(post("/bookings/1/review")
                        .session(session)
                        .param("rating", "0") // Invalid rating (< 1)
                        .param("comment", "Invalid"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("booking"))
                .andExpect(forwardedUrl("/WEB-INF/views/review-form.jsp"));

        verify(reviewService, never()).createReview(any(), any(), any());
    }
}
