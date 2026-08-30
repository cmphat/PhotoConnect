package com.photoconnect.controller;

import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.InvalidStatusTransitionException;
import com.photoconnect.service.AdminPhotographerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminPhotographerController.class)
class AdminPhotographerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminPhotographerService adminPhotographerService;

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

    private PhotographerProfile buildPendingProfile(Long id) {
        User user = new User();
        user.setId(10L);
        user.setEmail("photographer@example.com");
        user.setFullName("Test Photographer");
        user.setRole(UserRole.PHOTOGRAPHER);
        user.setStatus(UserStatus.ACTIVE);

        PhotographerProfile p = new PhotographerProfile();
        p.setUser(user);
        p.setDisplayName("Studio Alpha");
        p.setBio("Experienced landscape photographer with a focus on nature.");
        p.setCity("Hanoi");
        p.setExperienceYears(5);
        p.setPriceFrom(new BigDecimal("1000000"));
        p.setVerificationStatus(PhotographerVerificationStatus.PENDING);
        return p;
    }

    // ── Authorization tests ─────────────────────────────────────────────

    @Test
    void unauthenticatedGet_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin/photographers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void customerGet_shouldRedirectHome() throws Exception {
        mockMvc.perform(get("/admin/photographers").session(customerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void photographerGet_shouldRedirectHome() throws Exception {
        mockMvc.perform(get("/admin/photographers").session(photographerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void adminGet_shouldReturn200AndListView() throws Exception {
        when(adminPhotographerService.listPendingApplications()).thenReturn(List.of());

        mockMvc.perform(get("/admin/photographers").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-photographers"))
                .andExpect(model().attributeExists("applications"));
    }

    // ── List tests ──────────────────────────────────────────────────────

    @Test
    void adminGet_withPendingApplications_shouldExposeToModel() throws Exception {
        PhotographerProfile pending = buildPendingProfile(1L);
        when(adminPhotographerService.listPendingApplications()).thenReturn(List.of(pending));

        mockMvc.perform(get("/admin/photographers").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("applications", List.of(pending)));
    }

    // ── Detail tests ────────────────────────────────────────────────────

    @Test
    void adminGetDetail_shouldReturn200AndDetailView() throws Exception {
        PhotographerProfile profile = buildPendingProfile(1L);
        when(adminPhotographerService.getApplicationById(1L)).thenReturn(profile);

        mockMvc.perform(get("/admin/photographers/1").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-photographer-detail"))
                .andExpect(model().attributeExists("profile"));
    }

    @Test
    void nonAdminGetDetail_shouldRedirectHome() throws Exception {
        mockMvc.perform(get("/admin/photographers/1").session(customerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    // ── Approve tests ───────────────────────────────────────────────────

    @Test
    void nonAdminPostApprove_shouldRedirectHome() throws Exception {
        mockMvc.perform(post("/admin/photographers/1/approve").session(customerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        verify(adminPhotographerService, never()).approve(any());
    }

    @Test
    void adminPostApprove_shouldCallServiceAndRedirect() throws Exception {
        PhotographerProfile approved = buildPendingProfile(1L);
        approved.setVerificationStatus(PhotographerVerificationStatus.APPROVED);
        when(adminPhotographerService.approve(1L)).thenReturn(approved);

        mockMvc.perform(post("/admin/photographers/1/approve").session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/photographers/1"));

        verify(adminPhotographerService).approve(1L);
    }

    @Test
    void adminPostApprove_invalidTransition_shouldRedirectWithError() throws Exception {
        when(adminPhotographerService.approve(1L))
                .thenThrow(new InvalidStatusTransitionException("Already processed"));

        mockMvc.perform(post("/admin/photographers/1/approve").session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/photographers/1"));
    }

    // ── Reject tests ────────────────────────────────────────────────────

    @Test
    void nonAdminPostReject_shouldRedirectHome() throws Exception {
        mockMvc.perform(post("/admin/photographers/1/reject").session(photographerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        verify(adminPhotographerService, never()).reject(any());
    }

    @Test
    void adminPostReject_shouldCallServiceAndRedirect() throws Exception {
        PhotographerProfile rejected = buildPendingProfile(1L);
        rejected.setVerificationStatus(PhotographerVerificationStatus.REJECTED);
        when(adminPhotographerService.reject(1L)).thenReturn(rejected);

        mockMvc.perform(post("/admin/photographers/1/reject").session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/photographers/1"));

        verify(adminPhotographerService).reject(1L);
    }
}
