package com.photoconnect.controller;

import com.photoconnect.dto.PhotographerProfileRequest;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.exception.PhotographerProfileAlreadyExistsException;
import com.photoconnect.service.PhotographerProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PhotographerOnboardingController.class)
class PhotographerOnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhotographerProfileService photographerProfileService;

    // --- GET /become-photographer ---

    @Test
    void unauthenticatedGet_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/become-photographer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void authenticatedGet_noProfile_shouldShowForm() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);

        when(photographerProfileService.findByUserId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/become-photographer").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("photographer-onboarding"))
                .andExpect(model().attributeExists("profileRequest"));
    }

    @Test
    void authenticatedGet_profileExists_shouldRedirectToStatus() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);

        PhotographerProfile existingProfile = new PhotographerProfile();
        when(photographerProfileService.findByUserId(1L)).thenReturn(Optional.of(existingProfile));

        mockMvc.perform(get("/become-photographer").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/onboarding-status"));
    }

    // --- POST /become-photographer ---

    @Test
    void unauthenticatedPost_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/become-photographer")
                        .param("displayName", "Studio")
                        .param("bio", "A bio that is long enough to pass the 20 char minimum")
                        .param("city", "Hanoi")
                        .param("experienceYears", "3")
                        .param("priceFrom", "1000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void invalidPost_shouldReturnFormWithErrors() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);

        // displayName is blank → validation error
        mockMvc.perform(post("/become-photographer").session(session)
                        .param("displayName", "")
                        .param("bio", "Short bio")
                        .param("city", "")
                        .param("experienceYears", "")
                        .param("priceFrom", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("photographer-onboarding"));
    }

    @Test
    void validPost_shouldCallServiceAndRedirect() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("userRole", "CUSTOMER");

        User user = new User();
        user.setId(1L);
        user.setRole(UserRole.PHOTOGRAPHER);

        PhotographerProfile profile = new PhotographerProfile();
        profile.setUser(user);
        profile.setDisplayName("My Studio");
        profile.setVerificationStatus(PhotographerVerificationStatus.PENDING);

        when(photographerProfileService.createProfile(eq(1L), any(PhotographerProfileRequest.class)))
                .thenReturn(profile);

        mockMvc.perform(post("/become-photographer").session(session)
                        .param("displayName", "My Studio")
                        .param("bio", "A bio that is long enough to pass the 20 char minimum validation rule")
                        .param("city", "Ho Chi Minh City")
                        .param("experienceYears", "5")
                        .param("priceFrom", "1500000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/onboarding-status"));

        // Session should be updated to PHOTOGRAPHER
        org.junit.jupiter.api.Assertions.assertEquals("PHOTOGRAPHER", session.getAttribute("userRole"));
    }

    @Test
    void duplicateProfile_shouldRedirectToStatus() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);

        when(photographerProfileService.createProfile(eq(1L), any(PhotographerProfileRequest.class)))
                .thenThrow(new PhotographerProfileAlreadyExistsException("Already exists"));

        mockMvc.perform(post("/become-photographer").session(session)
                        .param("displayName", "My Studio")
                        .param("bio", "A bio that is long enough to pass the 20 char minimum validation rule")
                        .param("city", "Ho Chi Minh City")
                        .param("experienceYears", "5")
                        .param("priceFrom", "1500000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/onboarding-status"));
    }

    // --- GET /photographer/onboarding-status ---

    @Test
    void unauthenticatedStatusGet_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/photographer/onboarding-status"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void authenticatedStatusGet_withProfile_shouldShowStatus() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);

        User user = new User();
        user.setId(1L);
        PhotographerProfile profile = new PhotographerProfile();
        profile.setUser(user);
        profile.setDisplayName("My Studio");
        profile.setVerificationStatus(PhotographerVerificationStatus.PENDING);

        when(photographerProfileService.findByUserId(1L)).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/photographer/onboarding-status").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("photographer-onboarding-status"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().attribute("verificationStatus", "PENDING"));
    }
}
