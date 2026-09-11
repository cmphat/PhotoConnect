package com.photoconnect.controller;

import com.photoconnect.dto.PhotographerPublicDto;
import com.photoconnect.dto.PhotographerSearchRequest;
import com.photoconnect.dto.PortfolioImagePublicDto;
import com.photoconnect.service.PortfolioService;
import com.photoconnect.service.PublicPhotographerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for public photographer marketplace routes.
 *
 * Key invariants tested:
 * - GET /photographers requires NO login (guest access permitted)
 * - Search query parameters are passed to service
 * - Invalid search parameters produce clean error message
 * - GET /photographers/{id} requires NO login
 * - Non-approved profiles redirect, never render
 * - Model attributes are populated correctly for approved profiles
 */
@WebMvcTest(PhotographerController.class)
class PhotographerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicPhotographerService publicPhotographerService;

    @MockBean
    private PortfolioService portfolioService;

    @MockBean
    private com.photoconnect.service.ReviewService reviewService;

    private PhotographerPublicDto sampleDto() {
        com.photoconnect.entity.User user = new com.photoconnect.entity.User();
        user.setId(10L);
        user.setEmail("studio@example.com");
        user.setPassword("$2a$10$hashed");
        user.setFullName("Tran Thi B");
        user.setRole(com.photoconnect.entity.UserRole.PHOTOGRAPHER);
        user.setStatus(com.photoconnect.entity.UserStatus.ACTIVE);

        com.photoconnect.entity.PhotographerProfile profile =
                new com.photoconnect.entity.PhotographerProfile();
        profile.setUser(user);
        profile.setDisplayName("Ánh Sáng Studio");
        profile.setBio("Capturing life's most precious moments.");
        profile.setCity("Ho Chi Minh City");
        profile.setExperienceYears(7);
        profile.setPriceFrom(new BigDecimal("5000000"));
        profile.setVerificationStatus(
                com.photoconnect.entity.PhotographerVerificationStatus.APPROVED);

        return PhotographerPublicDto.from(profile);
    }

    // ── GET /photographers ──────────────────────────────────────────────────

    /**
     * Guest (no session) must be able to access the public listing.
     */
    @Test
    void getPhotographers_guestNoSession_shouldReturn200() throws Exception {
        when(publicPhotographerService.searchPhotographers(any())).thenReturn(List.of());

        mockMvc.perform(get("/photographers"))
                .andExpect(status().isOk())
                .andExpect(view().name("photographers"));
    }

    @Test
    void getPhotographers_shouldReturn200AndCorrectView() throws Exception {
        when(publicPhotographerService.searchPhotographers(any())).thenReturn(List.of());

        mockMvc.perform(get("/photographers"))
                .andExpect(status().isOk())
                .andExpect(view().name("photographers"))
                .andExpect(model().attributeExists("photographers"))
                .andExpect(model().attributeExists("resultCount"))
                .andExpect(model().attributeExists("hasFilters"));
    }

    @Test
    void getPhotographers_withApprovedProfiles_shouldExposeToModel() throws Exception {
        PhotographerPublicDto dto = sampleDto();
        when(publicPhotographerService.searchPhotographers(any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/photographers"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("photographers", List.of(dto)))
                .andExpect(model().attribute("resultCount", 1))
                .andExpect(model().attribute("hasFilters", false));
    }

    @Test
    void getPhotographers_withSearchFilters_shouldPassRequestToService() throws Exception {
        PhotographerPublicDto dto = sampleDto();
        when(publicPhotographerService.searchPhotographers(any(PhotographerSearchRequest.class)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/photographers")
                        .param("keyword", "portrait")
                        .param("city", "Ho Chi Minh City")
                        .param("minPrice", "1000000")
                        .param("maxPrice", "8000000")
                        .param("minExperience", "3"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("photographers", List.of(dto)))
                .andExpect(model().attribute("hasFilters", true))
                .andExpect(model().attribute("resultCount", 1));

        verify(publicPhotographerService).searchPhotographers(argThat(req ->
                "portrait".equals(req.getKeyword()) &&
                "Ho Chi Minh City".equals(req.getCity()) &&
                new BigDecimal("1000000").compareTo(req.getMinPrice()) == 0 &&
                new BigDecimal("8000000").compareTo(req.getMaxPrice()) == 0 &&
                Integer.valueOf(3).equals(req.getMinExperience())
        ));
    }

    @Test
    void getPhotographers_withInvalidPriceRange_shouldShowErrorMessage() throws Exception {
        PhotographerPublicDto dto = sampleDto();
        when(publicPhotographerService.listApprovedPhotographers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/photographers")
                        .param("minPrice", "10000000")
                        .param("maxPrice", "5000000"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("photographers", List.of(dto)));

        verify(publicPhotographerService).listApprovedPhotographers();
        verify(publicPhotographerService, never()).searchPhotographers(argThat(r -> !r.isValid()));
    }

    /**
     * Logged-in users of any role should also be able to access the marketplace.
     */
    @Test
    void getPhotographers_loggedInUser_shouldAlsoReturn200() throws Exception {
        when(publicPhotographerService.searchPhotographers(any())).thenReturn(List.of());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("userRole", "CUSTOMER");

        mockMvc.perform(get("/photographers").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("photographers"));
    }

    // ── GET /photographers/{id} ─────────────────────────────────────────────

    /**
     * Guest access to an approved detail page must succeed.
     */
    @Test
    void getPhotographerDetail_guestWithApprovedId_shouldReturn200() throws Exception {
        PhotographerPublicDto dto = sampleDto();
        when(publicPhotographerService.getApprovedPhotographerById(1L)).thenReturn(dto);
        when(portfolioService.getPublicPortfolioForProfile(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/photographers/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("photographer-detail"))
                .andExpect(model().attributeExists("photographer"))
                .andExpect(model().attributeExists("portfolioImages"))
                .andExpect(model().attributeExists("reviews"));
    }

    @Test
    void getPhotographerDetail_approvedId_shouldPopulateModel() throws Exception {
        PhotographerPublicDto dto = sampleDto();
        when(publicPhotographerService.getApprovedPhotographerById(1L)).thenReturn(dto);
        when(portfolioService.getPublicPortfolioForProfile(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/photographers/1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("photographer", dto))
                .andExpect(model().attributeExists("portfolioImages"))
                .andExpect(model().attributeExists("reviews"));
    }

    /**
     * A non-approved profile (PENDING/REJECTED/SUSPENDED) must NOT render.
     */
    @Test
    void getPhotographerDetail_nonApprovedId_shouldRedirectToListing() throws Exception {
        when(publicPhotographerService.getApprovedPhotographerById(99L))
                .thenThrow(new IllegalArgumentException("Approved photographer not found"));

        mockMvc.perform(get("/photographers/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographers"));
    }

    @Test
    void getPhotographerDetail_nonExistentId_shouldRedirectToListing() throws Exception {
        when(publicPhotographerService.getApprovedPhotographerById(999L))
                .thenThrow(new IllegalArgumentException("Approved photographer not found"));

        mockMvc.perform(get("/photographers/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographers"));
    }
}
