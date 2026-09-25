package com.photoconnect.controller;

import com.photoconnect.dto.PhotographerDashboardView;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.UserRole;
import com.photoconnect.exception.PhotographerStudioProfileNotFoundException;
import com.photoconnect.service.PhotographerDashboardService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PhotographerDashboardController.class)
class PhotographerDashboardControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PhotographerDashboardService photographerDashboardService;
    @MockBean private CurrentUserContext currentUserContext;

    @Test
    void guestIsRedirectedToLogin() throws Exception {
        when(currentUserContext.current(any())).thenReturn(Optional.empty());
        mockMvc.perform(get("/photographer/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(photographerDashboardService, never()).getDashboard(any());
    }

    @Test
    void customerIsForbidden() throws Exception {
        authenticate(2L, UserRole.CUSTOMER);
        mockMvc.perform(get("/photographer/dashboard").session(session(2L, UserRole.CUSTOMER)))
                .andExpect(status().isForbidden());
        verify(photographerDashboardService, never()).getDashboard(any());
    }

    @Test
    void adminIsForbidden() throws Exception {
        authenticate(3L, UserRole.ADMIN);
        mockMvc.perform(get("/photographer/dashboard").session(session(3L, UserRole.ADMIN)))
                .andExpect(status().isForbidden());
        verify(photographerDashboardService, never()).getDashboard(any());
    }

    @Test
    void photographerUsesValidatedIdentityAndIgnoresRequestIdentity() throws Exception {
        PhotographerDashboardView dashboard = emptyDashboard();
        authenticate(100L, UserRole.PHOTOGRAPHER);
        when(photographerDashboardService.getDashboard(100L)).thenReturn(dashboard);

        mockMvc.perform(get("/photographer/dashboard")
                        .param("userId", "999")
                        .param("photographerProfileId", "888")
                        .param("role", "ADMIN")
                        .session(session(100L, UserRole.PHOTOGRAPHER)))
                .andExpect(status().isOk())
                .andExpect(view().name("photographer-dashboard"))
                .andExpect(model().attribute("dashboard", dashboard));

        verify(photographerDashboardService).getDashboard(100L);
        verify(photographerDashboardService, never()).getDashboard(999L);
    }

    @Test
    void photographerWithoutProfileReturnsToOnboardingStatus() throws Exception {
        authenticate(100L, UserRole.PHOTOGRAPHER);
        when(photographerDashboardService.getDashboard(100L))
                .thenThrow(new PhotographerStudioProfileNotFoundException("Photographer profile not found."));

        mockMvc.perform(get("/photographer/dashboard")
                        .session(session(100L, UserRole.PHOTOGRAPHER)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/onboarding-status"));
    }

    private MockHttpSession session(Long userId, UserRole role) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userId);
        session.setAttribute("userRole", role.name());
        return session;
    }

    private void authenticate(Long userId, UserRole role) {
        when(currentUserContext.current(any()))
                .thenReturn(Optional.of(new AuthenticatedUser(userId, role, "Studio Owner")));
    }

    private PhotographerDashboardView emptyDashboard() {
        return new PhotographerDashboardView(
                10L, "North Studio", PhotographerVerificationStatus.APPROVED,
                true, 50, List.of(), List.of(), List.of(),
                new PhotographerDashboardView.PortfolioSummary(0, 0, false), List.of(),
                new PhotographerDashboardView.AvailabilitySummary(0, null),
                new PhotographerDashboardView.RatingSummary(0, 0));
    }
}
