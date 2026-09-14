package com.photoconnect.controller;

import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.service.PhotographerProfileService;
import com.photoconnect.service.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PhotographerScheduleController.class)
class PhotographerScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private PhotographerProfileService photographerProfileService;

    private PhotographerProfile profile;

    @BeforeEach
    void setUp() {
        profile = new PhotographerProfile();
        profile.setId(10L);
    }

    private MockHttpSession photographerSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        // Note: userRole is explicitly stored as String matching LoginController contract
        session.setAttribute("userRole", "PHOTOGRAPHER");
        session.setAttribute("userFullName", "Photographer Jane");
        return session;
    }

    private MockHttpSession customerSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 2L);
        session.setAttribute("userRole", "CUSTOMER");
        session.setAttribute("userFullName", "Customer Bob");
        return session;
    }

    private MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 3L);
        session.setAttribute("userRole", "ADMIN");
        session.setAttribute("userFullName", "Admin Alex");
        return session;
    }

    // ── GET /photographer/schedule ──────────────────────────────────────────

    @Test
    void viewSchedule_photographerSessionWithStringRole_shouldSucceedWithoutClassCastException() throws Exception {
        when(photographerProfileService.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(scheduleService.getUnavailableDates(10L)).thenReturn(List.of());

        mockMvc.perform(get("/photographer/schedule").session(photographerSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("photographer-schedule"))
                .andExpect(model().attributeExists("unavailableDates"))
                .andExpect(model().attributeExists("minDate"));

        verify(scheduleService).getUnavailableDates(10L);
    }

    @Test
    void viewSchedule_unauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/photographer/schedule"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(scheduleService, never()).getUnavailableDates(any());
    }

    @Test
    void viewSchedule_missingUserId_shouldRedirectToLogin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userRole", "PHOTOGRAPHER");

        mockMvc.perform(get("/photographer/schedule").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(scheduleService, never()).getUnavailableDates(any());
    }

    @Test
    void viewSchedule_customerRole_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/photographer/schedule").session(customerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(scheduleService, never()).getUnavailableDates(any());
    }

    @Test
    void viewSchedule_adminRole_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/photographer/schedule").session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(scheduleService, never()).getUnavailableDates(any());
    }

    @Test
    void viewSchedule_photographerWithoutProfile_shouldRedirectToLogin() throws Exception {
        when(photographerProfileService.findByUserId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/photographer/schedule").session(photographerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(scheduleService, never()).getUnavailableDates(any());
    }

    // ── POST /photographer/schedule/add ─────────────────────────────────────

    @Test
    void addUnavailableDate_photographerSessionWithStringRole_shouldSucceed() throws Exception {
        when(photographerProfileService.findByUserId(1L)).thenReturn(Optional.of(profile));

        mockMvc.perform(post("/photographer/schedule/add")
                        .session(photographerSession())
                        .param("date", "2026-09-15")
                        .param("reason", "Studio Renovations"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/schedule"))
                .andExpect(flash().attribute("successMessage", "Date marked as unavailable."));

        verify(scheduleService).addUnavailableDate(10L, LocalDate.of(2026, 9, 15), "Studio Renovations");
    }

    @Test
    void addUnavailableDate_unauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/photographer/schedule/add")
                        .param("date", "2026-09-15")
                        .param("reason", "Vacation"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(scheduleService, never()).addUnavailableDate(any(), any(), any());
    }

    @Test
    void addUnavailableDate_serviceThrowsIllegalArgumentException_shouldFlashErrorMessage() throws Exception {
        when(photographerProfileService.findByUserId(1L)).thenReturn(Optional.of(profile));
        doThrow(new IllegalArgumentException("Cannot block dates in the past"))
                .when(scheduleService).addUnavailableDate(eq(10L), any(LocalDate.class), any());

        mockMvc.perform(post("/photographer/schedule/add")
                        .session(photographerSession())
                        .param("date", "2026-09-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/schedule"))
                .andExpect(flash().attribute("errorMessage", "Cannot block dates in the past"));
    }

    // ── POST /photographer/schedule/remove/{id} ─────────────────────────────

    @Test
    void removeUnavailableDate_photographerSessionWithStringRole_shouldSucceed() throws Exception {
        when(photographerProfileService.findByUserId(1L)).thenReturn(Optional.of(profile));

        mockMvc.perform(post("/photographer/schedule/remove/5").session(photographerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/schedule"))
                .andExpect(flash().attribute("successMessage", "Date is now available again."));

        verify(scheduleService).removeUnavailableDate(10L, 5L);
    }

    @Test
    void removeUnavailableDate_unauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/photographer/schedule/remove/5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(scheduleService, never()).removeUnavailableDate(any(), any());
    }
}
