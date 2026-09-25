package com.photoconnect.controller;

import com.photoconnect.dto.PhotographerProfileEditRequest;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.UserRole;
import com.photoconnect.service.PhotographerProfileService;
import com.photoconnect.service.PortfolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PhotographerProfileController.class)
class PhotographerProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhotographerProfileService photographerProfileService;

    @MockBean
    private PortfolioService portfolioService;

    private MockHttpSession photographerSession;
    private MockHttpSession customerSession;
    private PhotographerProfile mockProfile;

    @BeforeEach
    void setUp() {
        photographerSession = new MockHttpSession();
        photographerSession.setAttribute("userId", 10L);
        photographerSession.setAttribute("userRole", UserRole.PHOTOGRAPHER.name());

        customerSession = new MockHttpSession();
        customerSession.setAttribute("userId", 20L);
        customerSession.setAttribute("userRole", UserRole.CUSTOMER.name());

        mockProfile = new PhotographerProfile();
        mockProfile.setId(100L);
        mockProfile.setDisplayName("Elena Visuals");
        mockProfile.setHeadline("Fine Art Editorial");
        mockProfile.setBio("Detailed biography about artistic narrative and portraiture.");
        mockProfile.setCity("Ho Chi Minh City");
        mockProfile.setCountry("Vietnam");
        mockProfile.setExperienceYears(6);
        mockProfile.setPriceFrom(new BigDecimal("2500000"));
        mockProfile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);
    }

    @Test
    void getEditProfile_authenticatedPhotographer_returnsViewWithModel() throws Exception {
        when(photographerProfileService.findByUserId(10L)).thenReturn(Optional.of(mockProfile));
        when(portfolioService.getPortfolioForUser(10L)).thenReturn(Collections.emptyList());
        when(photographerProfileService.calculateProfileCompleteness(eq(mockProfile), eq(0), eq(false))).thenReturn(85);
        when(photographerProfileService.getCompletenessRecommendations(eq(mockProfile), eq(0), eq(false)))
                .thenReturn(List.of("Upload portfolio photos"));

        mockMvc.perform(get("/photographer/profile/edit").session(photographerSession))
                .andExpect(status().isOk())
                .andExpect(view().name("photographer-profile-edit"))
                .andExpect(model().attributeExists("editRequest"))
                .andExpect(model().attributeExists("allSpecialties"))
                .andExpect(model().attribute("completenessScore", 85))
                .andExpect(model().attributeExists("completenessRecommendations"));
    }

    @Test
    void getEditProfile_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/photographer/profile/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void getEditProfile_customerRole_redirectsToHome() throws Exception {
        mockMvc.perform(get("/photographer/profile/edit").session(customerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void postEditProfile_validInput_updatesAndRedirectsWithFlashSuccess() throws Exception {
        when(photographerProfileService.findByUserId(10L)).thenReturn(Optional.of(mockProfile));
        when(photographerProfileService.updateProfile(eq(10L), any(PhotographerProfileEditRequest.class)))
                .thenReturn(mockProfile);

        mockMvc.perform(post("/photographer/profile/edit")
                        .session(photographerSession)
                        .param("displayName", "Elena Visuals Studio")
                        .param("headline", "Editorial & Wedding Art")
                        .param("bio", "Updated biography capturing artistic moments across Vietnam.")
                        .param("city", "Da Nang")
                        .param("country", "Vietnam")
                        .param("experienceYears", "7")
                        .param("priceFrom", "3000000")
                        .param("specialties", "PORTRAIT", "WEDDING")
                        .param("websiteUrl", "https://elenavisuals.com")
                        .param("instagramUrl", "https://instagram.com/elenavisuals")
                        .param("facebookUrl", "https://facebook.com/elenavisuals")
                        .param("equipmentSummary", "Canon R5, 50mm f/1.2")
                        .param("languages", "English, Vietnamese")
                        .param("travelAvailable", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/profile/edit"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(photographerProfileService).updateProfile(eq(10L), any(PhotographerProfileEditRequest.class));
    }

    @Test
    void postEditProfile_validationError_returnsEditViewWithoutCallingUpdate() throws Exception {
        when(photographerProfileService.findByUserId(10L)).thenReturn(Optional.of(mockProfile));
        when(portfolioService.getPortfolioForUser(10L)).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/photographer/profile/edit")
                        .session(photographerSession)
                        .param("displayName", "") // Blank displayName violates @NotBlank
                        .param("bio", "Too short") // Violates min length
                        .param("city", "")
                        .param("experienceYears", "-1") // Negative violates @Min(0)
                        .param("priceFrom", "-500")) // Negative violates @DecimalMin
                .andExpect(status().isOk())
                .andExpect(view().name("photographer-profile-edit"))
                .andExpect(model().hasErrors());

        verify(photographerProfileService, never()).updateProfile(any(), any());
    }

    @Test
    void postEditProfile_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/photographer/profile/edit")
                        .param("displayName", "Studio")
                        .param("bio", "A very long biography that passes validation.")
                        .param("city", "Hanoi")
                        .param("experienceYears", "5")
                        .param("priceFrom", "1000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
