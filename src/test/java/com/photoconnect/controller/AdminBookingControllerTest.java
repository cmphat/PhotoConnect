package com.photoconnect.controller;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.exception.ErrorCode;
import com.photoconnect.exception.GlobalExceptionHandler;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.service.AdminBookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminBookingController.class)
@Import(GlobalExceptionHandler.class)
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

    @Test
    void adminGetBookingDetail_shouldReturnDetailView() throws Exception {
        Booking booking = new Booking();
        booking.setId(10L);
        when(adminBookingService.getBookingDetails(10L)).thenReturn(booking);
        when(adminBookingService.getBookingAuditLogs(10L)).thenReturn(List.of());

        mockMvc.perform(get("/admin/bookings/10").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-booking-detail"))
                .andExpect(model().attribute("booking", booking))
                .andExpect(model().attributeExists("auditLogs"));

        verify(adminBookingService).getBookingDetails(10L);
    }

    @Test
    void adminResolveDispute_shouldCallServiceAndRedirect() throws Exception {
        mockMvc.perform(post("/admin/bookings/10/resolve-dispute")
                        .param("resolution", "REFUND_CUSTOMER")
                        .param("adminNotes", "Client was unable to attend due to emergency")
                        .session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/bookings/10"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(adminBookingService).resolveDispute(10L, "REFUND_CUSTOMER", "Client was unable to attend due to emergency", 99L);
    }

    @Test
    void adminResolveDispute_whenServiceThrowsInvalidStatus_webRequest_redirectsWithErrorMessage() throws Exception {
        doThrow(new InvalidBookingException(ErrorCode.BOOKING_003_INVALID_STATUS, "Only DISPUTED bookings can be resolved by admin. Current status is CONFIRMED."))
                .when(adminBookingService).resolveDispute(10L, "REFUND_CUSTOMER", "Client requested refund", 99L);

        mockMvc.perform(post("/admin/bookings/10/resolve-dispute")
                        .param("resolution", "REFUND_CUSTOMER")
                        .param("adminNotes", "Client requested refund")
                        .session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/bookings/10"))
                .andExpect(flash().attribute("errorMessage", containsString("Only DISPUTED bookings can be resolved")));
    }

    @Test
    void adminResolveDispute_whenBookingInConfirmedStatus_jsonRequest_returns409Conflict() throws Exception {
        doThrow(new InvalidBookingException(ErrorCode.BOOKING_003_INVALID_STATUS, "Only DISPUTED bookings can be resolved by admin. Current status is CONFIRMED."))
                .when(adminBookingService).resolveDispute(10L, "REFUND_CUSTOMER", "Client requested refund", 99L);

        mockMvc.perform(post("/admin/bookings/10/resolve-dispute")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("resolution", "REFUND_CUSTOMER")
                        .param("adminNotes", "Client requested refund")
                        .session(adminSession()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("BOOKING_003_INVALID_STATUS"));
    }

    @Test
    void adminResolveDispute_whenBookingInPendingStatus_jsonRequest_returns409Conflict() throws Exception {
        doThrow(new InvalidBookingException(ErrorCode.BOOKING_003_INVALID_STATUS, "Only DISPUTED bookings can be resolved by admin. Current status is PENDING."))
                .when(adminBookingService).resolveDispute(10L, "REFUND_CUSTOMER", "Client requested refund", 99L);

        mockMvc.perform(post("/admin/bookings/10/resolve-dispute")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("resolution", "REFUND_CUSTOMER")
                        .param("adminNotes", "Client requested refund")
                        .session(adminSession()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("BOOKING_003_INVALID_STATUS"));
    }

    @Test
    void adminResolveDispute_whenBookingInCompletedStatus_jsonRequest_returns409Conflict() throws Exception {
        doThrow(new InvalidBookingException(ErrorCode.BOOKING_003_INVALID_STATUS, "Only DISPUTED bookings can be resolved by admin. Current status is COMPLETED."))
                .when(adminBookingService).resolveDispute(10L, "PAY_PHOTOGRAPHER", "Admin decision", 99L);

        mockMvc.perform(post("/admin/bookings/10/resolve-dispute")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("resolution", "PAY_PHOTOGRAPHER")
                        .param("adminNotes", "Admin decision")
                        .session(adminSession()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("BOOKING_003_INVALID_STATUS"));
    }

    @Test
    void adminResolveDispute_whenDisputedStatus_jsonRequest_succeedsWithOk() throws Exception {
        mockMvc.perform(post("/admin/bookings/10/resolve-dispute")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("resolution", "REFUND_CUSTOMER")
                        .param("adminNotes", "Legitimate client grievance")
                        .session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(adminBookingService).resolveDispute(10L, "REFUND_CUSTOMER", "Legitimate client grievance", 99L);
    }
}
