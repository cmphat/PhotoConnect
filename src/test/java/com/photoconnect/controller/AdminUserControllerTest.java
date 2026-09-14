package com.photoconnect.controller;

import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserService adminUserService;

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

    @Test
    void unauthenticatedGetUsers_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void customerGetUsers_shouldRedirectToHome() throws Exception {
        mockMvc.perform(get("/admin/users").session(customerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void adminGetUsers_shouldReturnUsersView() throws Exception {
        User user = new User("user@example.com", "hash", "Test User", "0123456789", UserRole.CUSTOMER, UserStatus.ACTIVE);
        when(adminUserService.searchUsers(null, null, null)).thenReturn(List.of(user));

        mockMvc.perform(get("/admin/users").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("activeTab", "users"));

        verify(adminUserService).searchUsers(null, null, null);
    }

    @Test
    void adminGetUsers_withSearchAndFilters_shouldPassParsedEnums() throws Exception {
        when(adminUserService.searchUsers("Jane", UserRole.PHOTOGRAPHER, UserStatus.ACTIVE)).thenReturn(List.of());

        mockMvc.perform(get("/admin/users")
                        .param("search", "Jane")
                        .param("role", "PHOTOGRAPHER")
                        .param("status", "ACTIVE")
                        .session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedRole", "PHOTOGRAPHER"))
                .andExpect(model().attribute("selectedStatus", "ACTIVE"));

        verify(adminUserService).searchUsers("Jane", UserRole.PHOTOGRAPHER, UserStatus.ACTIVE);
    }

    @Test
    void adminPostUpdateStatus_shouldCallServiceAndRedirect() throws Exception {
        mockMvc.perform(post("/admin/users/5/status")
                        .param("status", "BANNED")
                        .session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(adminUserService).updateUserStatus(5L, UserStatus.BANNED, 99L);
    }

    @Test
    void adminPostUpdateStatus_invalidStatus_shouldRedirectWithError() throws Exception {
        mockMvc.perform(post("/admin/users/5/status")
                        .param("status", "NOT_A_STATUS")
                        .session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(adminUserService, never()).updateUserStatus(any(), any(), any());
    }

    @Test
    void adminPostUpdateStatus_selfModification_shouldRedirectWithError() throws Exception {
        doThrow(new IllegalStateException("Administrators cannot modify their own account status."))
                .when(adminUserService).updateUserStatus(eq(99L), eq(UserStatus.INACTIVE), eq(99L));

        mockMvc.perform(post("/admin/users/99/status")
                        .param("status", "INACTIVE")
                        .session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(adminUserService).updateUserStatus(99L, UserStatus.INACTIVE, 99L);
    }
}
