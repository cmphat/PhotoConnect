package com.photoconnect.controller;

import com.photoconnect.dto.CustomerDashboardView;
import com.photoconnect.entity.UserRole;
import com.photoconnect.service.CustomerDashboardService;
import com.photoconnect.security.AuthenticatedUser;
import com.photoconnect.security.CurrentUserContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(CustomerDashboardController.class)
class CustomerDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerDashboardService customerDashboardService;

    @MockBean
    private CurrentUserContext currentUserContext;

    @Test
    void guestIsRedirectedToLogin() throws Exception {
        when(currentUserContext.current(anyRequest())).thenReturn(Optional.empty());
        mockMvc.perform(get("/customer/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(customerDashboardService, never()).getDashboard(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void photographerIsForbidden() throws Exception {
        authenticate(20L, UserRole.PHOTOGRAPHER);
        mockMvc.perform(get("/customer/dashboard").session(session(20L, UserRole.PHOTOGRAPHER)))
                .andExpect(status().isForbidden());

        verify(customerDashboardService, never()).getDashboard(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void adminIsForbidden() throws Exception {
        authenticate(30L, UserRole.ADMIN);
        mockMvc.perform(get("/customer/dashboard").session(session(30L, UserRole.ADMIN)))
                .andExpect(status().isForbidden());

        verify(customerDashboardService, never()).getDashboard(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void customerDashboardUsesValidatedIdentityAndIgnoresRequestIdentity() throws Exception {
        CustomerDashboardView dashboard = emptyDashboard("Mai Nguyen");
        authenticate(100L, UserRole.CUSTOMER);
        when(customerDashboardService.getDashboard(100L)).thenReturn(dashboard);

        mockMvc.perform(get("/customer/dashboard")
                        .param("customerId", "999")
                        .session(session(100L, UserRole.CUSTOMER)))
                .andExpect(status().isOk())
                .andExpect(view().name("customer-dashboard"))
                .andExpect(model().attribute("dashboard", dashboard));

        verify(customerDashboardService).getDashboard(100L);
        verify(customerDashboardService, never()).getDashboard(999L);
    }

    @Test
    void emptyDashboardStillRendersWorkspace() throws Exception {
        CustomerDashboardView dashboard = emptyDashboard("Mai Nguyen");
        authenticate(100L, UserRole.CUSTOMER);
        when(customerDashboardService.getDashboard(100L)).thenReturn(dashboard);

        mockMvc.perform(get("/customer/dashboard").session(session(100L, UserRole.CUSTOMER)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("dashboard"));
    }

    private MockHttpSession session(Long userId, UserRole role) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userId);
        session.setAttribute("userRole", role.name());
        return session;
    }

    private void authenticate(Long userId, UserRole role) {
        when(currentUserContext.current(anyRequest()))
                .thenReturn(Optional.of(new AuthenticatedUser(userId, role, "Test User")));
    }

    private jakarta.servlet.http.HttpServletRequest anyRequest() {
        return org.mockito.ArgumentMatchers.any(jakarta.servlet.http.HttpServletRequest.class);
    }

    private CustomerDashboardView emptyDashboard(String name) {
        return new CustomerDashboardView(
                name,
                new CustomerDashboardView.Summary(0, 0, 0, 0),
                List.of(), List.of(), List.of());
    }
}
