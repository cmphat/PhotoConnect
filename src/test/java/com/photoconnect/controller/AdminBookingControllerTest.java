package com.photoconnect.controller;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.service.AdminBookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminBookingController.class)
class AdminBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminBookingService adminBookingService;

    private MockHttpSession adminSession() {
        MockHttpSession s = new MockHttpSession();
        s.setAttribute("userId", 99L);
        s.setAttribute("userRole", "ADMIN");
        s.setAttribute("userFullName", "Admin User");
        return s;
    }

    private MockHttpSession customerSession() {
        MockHttpSession s = new MockHttpSession();
        s.setAttribute("userId", 1L);
        s.setAttribute("userRole", "CUSTOMER");
        return s;
    }

    private MockHttpSession photographerSession() {
        MockHttpSession s = new MockHttpSession();
        s.setAttribute("userId", 2L);
        s.setAttribute("userRole", "PHOTOGRAPHER");
        return s;
    }

    @Test
    void unauthenticatedGetBookings_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin/bookings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void customerGetBookings_shouldRedirectToHome() throws Exception {
        mockMvc.perform(get("/admin/bookings").session(customerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void photographerGetBookings_shouldRedirectToHome() throws Exception {
        mockMvc.perform(get("/admin/bookings").session(photographerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void adminGetBookings_shouldReturnBookingsView() throws Exception {
        Booking booking = new Booking();
        booking.setId(10L);
        when(adminBookingService.listBookings(null)).thenReturn(List.of(booking));

        mockMvc.perform(get("/admin/bookings").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-bookings"))
                .andExpect(model().attributeExists("bookings"))
                .andExpect(model().attribute("selectedStatus", "ALL"))
                .andExpect(model().attribute("activeTab", "bookings"));

        verify(adminBookingService).listBookings(null);
    }

    @Test
    void adminGetBookings_withStatusFilter_shouldPassEnumToService() throws Exception {
        when(adminBookingService.listBookings(BookingStatus.COMPLETED)).thenReturn(List.of());

        mockMvc.perform(get("/admin/bookings")
                        .param("status", "COMPLETED")
                        .session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedStatus", "COMPLETED"));

        verify(adminBookingService).listBookings(BookingStatus.COMPLETED);
    }

    @Test
    void postBookingEndpoint_shouldNotBeAllowed_readOnlyEnforcement() throws Exception {
        mockMvc.perform(post("/admin/bookings").session(adminSession()))
                .andExpect(status().isMethodNotAllowed());
    }
}
