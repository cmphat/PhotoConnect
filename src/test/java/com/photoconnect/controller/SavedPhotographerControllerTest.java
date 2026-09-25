package com.photoconnect.controller;

import com.photoconnect.dto.PhotographerPublicDto;
import com.photoconnect.dto.SavedPhotographerDto;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.UserRole;
import com.photoconnect.service.SavedPhotographerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SavedPhotographerController.class)
class SavedPhotographerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SavedPhotographerService savedPhotographerService;

    private MockHttpSession customerSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 100L);
        session.setAttribute("userRole", UserRole.CUSTOMER.name());
        return session;
    }

    private MockHttpSession photographerSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 200L);
        session.setAttribute("userRole", UserRole.PHOTOGRAPHER.name());
        return session;
    }

    private MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 300L);
        session.setAttribute("userRole", UserRole.ADMIN.name());
        return session;
    }

    // ── POST /photographers/{id}/save ──────────────────────────────────────

    @Test
    @DisplayName("CUSTOMER can save an approved photographer")
    void customerCanSavePhotographer() throws Exception {
        when(savedPhotographerService.savePhotographer(100L, 10L)).thenReturn(true);

        mockMvc.perform(post("/photographers/10/save")
                        .session(customerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographers/10"))
                .andExpect(flash().attribute("successMessage", "Photographer saved to your favorites."));

        verify(savedPhotographerService).savePhotographer(100L, 10L);
    }

    @Test
    @DisplayName("CUSTOMER save with redirect parameter redirects to requested path")
    void customerSaveWithRedirectParam() throws Exception {
        when(savedPhotographerService.savePhotographer(100L, 10L)).thenReturn(true);

        mockMvc.perform(post("/photographers/10/save")
                        .param("redirect", "/photographers")
                        .session(customerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographers"));
    }

    @Test
    @DisplayName("GUEST attempting to save redirects to login")
    void guestSaveRedirectsToLogin() throws Exception {
        mockMvc.perform(post("/photographers/10/save"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(savedPhotographerService, never()).savePhotographer(any(), any());
    }

    @Test
    @DisplayName("PHOTOGRAPHER attempting to save is denied with 403")
    void photographerSaveIsForbidden() throws Exception {
        mockMvc.perform(post("/photographers/10/save")
                        .session(photographerSession()))
                .andExpect(status().isForbidden());

        verify(savedPhotographerService, never()).savePhotographer(any(), any());
    }

    @Test
    @DisplayName("ADMIN attempting to save is denied with 403")
    void adminSaveIsForbidden() throws Exception {
        mockMvc.perform(post("/photographers/10/save")
                        .session(adminSession()))
                .andExpect(status().isForbidden());

        verify(savedPhotographerService, never()).savePhotographer(any(), any());
    }

    // ── POST /photographers/{id}/unsave ────────────────────────────────────

    @Test
    @DisplayName("CUSTOMER can unsave a photographer")
    void customerCanUnsavePhotographer() throws Exception {
        when(savedPhotographerService.removeSavedPhotographer(100L, 10L)).thenReturn(true);

        mockMvc.perform(post("/photographers/10/unsave")
                        .session(customerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographers/10"))
                .andExpect(flash().attribute("successMessage", "Photographer removed from your favorites."));

        verify(savedPhotographerService).removeSavedPhotographer(100L, 10L);
    }

    @Test
    @DisplayName("CUSTOMER unsave with redirect parameter returns to specified route")
    void customerUnsaveWithRedirectParam() throws Exception {
        when(savedPhotographerService.removeSavedPhotographer(100L, 10L)).thenReturn(true);

        mockMvc.perform(post("/photographers/10/unsave")
                        .param("redirect", "/customer/saved-photographers")
                        .session(customerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/saved-photographers"));
    }

    @Test
    @DisplayName("GUEST attempting to unsave redirects to login")
    void guestUnsaveRedirectsToLogin() throws Exception {
        mockMvc.perform(post("/photographers/10/unsave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(savedPhotographerService, never()).removeSavedPhotographer(any(), any());
    }

    @Test
    @DisplayName("PHOTOGRAPHER attempting to unsave is denied with 403")
    void photographerUnsaveIsForbidden() throws Exception {
        mockMvc.perform(post("/photographers/10/unsave")
                        .session(photographerSession()))
                .andExpect(status().isForbidden());

        verify(savedPhotographerService, never()).removeSavedPhotographer(any(), any());
    }

    // ── GET /customer/saved-photographers ──────────────────────────────────

    @Test
    @DisplayName("CUSTOMER can view saved photographers list")
    void customerCanViewSavedPhotographers() throws Exception {
        PhotographerProfile profile = new PhotographerProfile();
        profile.setId(10L);
        profile.setDisplayName("Test Studio");
        PhotographerPublicDto publicDto = PhotographerPublicDto.from(profile, null, 75);
        SavedPhotographerDto dto = new SavedPhotographerDto(1L, 10L, publicDto, LocalDateTime.now());

        when(savedPhotographerService.getSavedPhotographers(100L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/customer/saved-photographers")
                        .session(customerSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("saved-photographers"))
                .andExpect(model().attributeExists("savedPhotographers"))
                .andExpect(model().attribute("savedCount", 1));
    }

    @Test
    @DisplayName("CUSTOMER viewing empty saved list renders empty state")
    void customerViewingEmptySavedList() throws Exception {
        when(savedPhotographerService.getSavedPhotographers(100L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/customer/saved-photographers")
                        .session(customerSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("saved-photographers"))
                .andExpect(model().attribute("savedCount", 0));
    }

    @Test
    @DisplayName("GUEST accessing saved photographers page redirects to login")
    void guestAccessingSavedPhotographersRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/customer/saved-photographers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("PHOTOGRAPHER accessing customer saved photographers page is denied with 403")
    void photographerAccessingSavedPhotographersIsForbidden() throws Exception {
        mockMvc.perform(get("/customer/saved-photographers")
                        .session(photographerSession()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN accessing customer saved photographers page is denied with 403")
    void adminAccessingSavedPhotographersIsForbidden() throws Exception {
        mockMvc.perform(get("/customer/saved-photographers")
                        .session(adminSession()))
                .andExpect(status().isForbidden());
    }
}
