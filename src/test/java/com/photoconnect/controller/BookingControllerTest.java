package com.photoconnect.controller;

import com.photoconnect.dto.BookingRequest;
import com.photoconnect.dto.PhotographerPublicDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.User;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.SelfBookingNotAllowedException;
import com.photoconnect.service.BookingService;
import com.photoconnect.service.PublicPhotographerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private PublicPhotographerService publicPhotographerService;

    @MockBean
    private com.photoconnect.service.DepositService depositService;

    private PhotographerPublicDto samplePhotographer;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        PhotographerProfile profile = new PhotographerProfile();
        profile.setId(10L);
        profile.setDisplayName("Bob's Studio");
        profile.setBio("Cinematic photography");
        profile.setCity("Ho Chi Minh City");
        profile.setExperienceYears(5);
        profile.setPriceFrom(new BigDecimal("3000000.00"));

        samplePhotographer = PhotographerPublicDto.from(profile, "https://res.cloudinary.com/demo/sample.jpg");

        session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("userRole", "CUSTOMER");
        session.setAttribute("userFullName", "Customer Jane");
    }

    // ── GET /photographers/{id}/book ─────────────────────────────────────────

    @Test
    void getBookingForm_unauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/photographers/10/book"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void getBookingForm_authenticated_photographerFound_shouldRenderForm() throws Exception {
        when(publicPhotographerService.getApprovedPhotographerById(10L))
                .thenReturn(samplePhotographer);

        mockMvc.perform(get("/photographers/10/book").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("photographer"))
                .andExpect(model().attributeExists("bookingRequest"))
                .andExpect(forwardedUrl("/WEB-INF/views/booking-form.jsp"));
    }

    @Test
    void getBookingForm_photographerNotFound_shouldRedirectToMarketplace() throws Exception {
        when(publicPhotographerService.getApprovedPhotographerById(999L))
                .thenThrow(new IllegalArgumentException("Photographer not found"));

        mockMvc.perform(get("/photographers/999/book").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographers"));
    }

    // ── POST /photographers/{id}/book ────────────────────────────────────────

    @Test
    void postBooking_unauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/photographers/10/book")
                        .param("bookingDate", LocalDate.now().plusDays(3).toString())
                        .param("bookingTime", "14:00")
                        .param("location", "Studio A"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(bookingService, never()).createBooking(any(), any(), any());
    }

    @Test
    void postBooking_validRequest_shouldCreateBookingAndRedirectToSuccess() throws Exception {
        LocalDate date = LocalDate.now().plusDays(3);
        LocalTime time = LocalTime.of(14, 0);

        User customer = new User();
        customer.setId(1L);
        customer.setFullName("Customer Jane");

        PhotographerProfile profile = new PhotographerProfile();
        profile.setId(10L);
        profile.setDisplayName("Bob's Studio");

        Booking createdBooking = new Booking(customer, profile, date, time, "District 1", "Portrait", new BigDecimal("3000000"));
        createdBooking.setId(101L);

        when(publicPhotographerService.getApprovedPhotographerById(10L))
                .thenReturn(samplePhotographer);
        when(bookingService.createBooking(eq(1L), eq(10L), any(BookingRequest.class)))
                .thenReturn(createdBooking);

        mockMvc.perform(post("/photographers/10/book")
                        .session(session)
                        .param("bookingDate", date.toString())
                        .param("bookingTime", "14:00")
                        .param("location", "District 1")
                        .param("notes", "Portrait"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/101/success"));

        verify(bookingService).createBooking(eq(1L), eq(10L), any(BookingRequest.class));
    }

    @Test
    void postBooking_validationFailure_shouldReturnFormWithErrors() throws Exception {
        when(publicPhotographerService.getApprovedPhotographerById(10L))
                .thenReturn(samplePhotographer);

        // Missing location and past date
        mockMvc.perform(post("/photographers/10/book")
                        .session(session)
                        .param("bookingDate", LocalDate.now().minusDays(1).toString())
                        .param("bookingTime", "14:00")
                        .param("location", ""))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("photographer"))
                .andExpect(forwardedUrl("/WEB-INF/views/booking-form.jsp"));

        verify(bookingService, never()).createBooking(any(), any(), any());
    }

    @Test
    void postBooking_selfBooking_shouldReturnFormWithErrorMessage() throws Exception {
        when(publicPhotographerService.getApprovedPhotographerById(10L))
                .thenReturn(samplePhotographer);
        when(bookingService.createBooking(eq(1L), eq(10L), any(BookingRequest.class)))
                .thenThrow(new SelfBookingNotAllowedException("You cannot book your own photographer profile."));

        mockMvc.perform(post("/photographers/10/book")
                        .session(session)
                        .param("bookingDate", LocalDate.now().plusDays(2).toString())
                        .param("bookingTime", "10:00")
                        .param("location", "Somewhere"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errorMessage", "You cannot book your own photographer profile."))
                .andExpect(forwardedUrl("/WEB-INF/views/booking-form.jsp"));
    }

    // ── GET /bookings/{id}/success ───────────────────────────────────────────

    @Test
    void getBookingSuccess_unauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/bookings/101/success"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void getBookingSuccess_authorizedCustomer_shouldRenderSuccessPage() throws Exception {
        User customer = new User();
        customer.setId(1L);
        customer.setFullName("Customer Jane");

        PhotographerProfile profile = new PhotographerProfile();
        profile.setId(10L);
        profile.setDisplayName("Bob's Studio");
        profile.setCity("Ho Chi Minh City");

        Booking booking = new Booking(customer, profile, LocalDate.now().plusDays(3), LocalTime.of(14, 0), "District 1", "Notes", new BigDecimal("3000000"));
        booking.setId(101L);

        when(bookingService.getBookingForCustomer(101L, 1L)).thenReturn(booking);

        mockMvc.perform(get("/bookings/101/success").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("booking"))
                .andExpect(forwardedUrl("/WEB-INF/views/booking-success.jsp"));
    }

    @Test
    void getBookingSuccess_unauthorizedCustomer_shouldRedirectToMarketplace() throws Exception {
        when(bookingService.getBookingForCustomer(101L, 1L))
                .thenThrow(new InvalidBookingException("You are not authorized to view this booking."));

        mockMvc.perform(get("/bookings/101/success").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographers"));
    }
}
