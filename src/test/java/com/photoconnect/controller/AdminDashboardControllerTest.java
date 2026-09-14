package com.photoconnect.controller;

import com.photoconnect.dto.AdminDashboardStatsDto;
import com.photoconnect.service.AdminDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminDashboardController.class)
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminDashboardService adminDashboardService;

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
    void unauthenticatedDashboard_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void unauthenticatedAdminRoot_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void customerAccess_shouldRedirectToHome() throws Exception {
        mockMvc.perform(get("/admin/dashboard").session(customerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void photographerAccess_shouldRedirectToHome() throws Exception {
        mockMvc.perform(get("/admin/dashboard").session(photographerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void adminRoot_shouldRedirectToDashboard() throws Exception {
        mockMvc.perform(get("/admin").session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    void adminDashboard_shouldReturn200AndDashboardViewWithStats() throws Exception {
        AdminDashboardStatsDto stats = new AdminDashboardStatsDto(
                100L, 75L, 24L, 4L, 20L, 50L, 10L, 30L, 25L, 18L, new BigDecimal("9000000.00")
        );
        when(adminDashboardService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/admin/dashboard").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-dashboard"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attribute("activeTab", "dashboard"));

        verify(adminDashboardService).getDashboardStats();
    }
}
