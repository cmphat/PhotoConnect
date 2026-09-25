package com.photoconnect.controller;

import com.photoconnect.entity.PortfolioCategory;
import com.photoconnect.entity.PortfolioImage;
import com.photoconnect.service.PortfolioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-layer tests for PhotographerPortfolioController.
 *
 * Behavior verified:
 * 1. Unauthenticated GET → redirect to login (no userId in session)
 * 2. Authenticated photographer GET → 200, images in model
 * 3. Upload: service called, PRG redirect on success
 * 4. Upload: service exception → flash error, still redirects (no 500)
 * 5. Delete: service called with session userId, PRG redirect
 * 6. Delete: SecurityException → flash error, not 500
 * 7. Unauthenticated upload → redirect login (not 500)
 * 8. Unauthenticated delete → redirect login (not 500)
 */
@WebMvcTest(PhotographerPortfolioController.class)
class PhotographerPortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortfolioService portfolioService;

    private MockHttpSession photographerSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("userRole", "PHOTOGRAPHER");
        session.setAttribute("userFullName", "Test Photographer");
        return session;
    }

    // ── GET /photographer/portfolio — authentication guard ────────────────────

    /**
     * Without a session, the user must be redirected to login.
     * This is the critical access control for the private management page.
     */
    @Test
    void getPortfolio_noSession_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/photographer/portfolio"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void getPortfolio_customerRole_shouldRedirectWithoutReadingPortfolio() throws Exception {
        MockHttpSession session = photographerSession();
        session.setAttribute("userRole", "CUSTOMER");

        mockMvc.perform(get("/photographer/portfolio").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verifyNoInteractions(portfolioService);
    }

    /**
     * With a valid session, the management page should render with images in model.
     */
    @Test
    void getPortfolio_authenticatedPhotographer_shouldReturn200() throws Exception {
        when(portfolioService.getPortfolioForUser(1L)).thenReturn(List.of());

        mockMvc.perform(get("/photographer/portfolio").session(photographerSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("photographer-portfolio"))
                .andExpect(model().attributeExists("images"));
    }

    @Test
    void getPortfolio_withImages_shouldExposeToModel() throws Exception {
        PortfolioImage img = new PortfolioImage();
        img.setImageUrl("https://res.cloudinary.com/test/image1.jpg");
        img.setCaption("Test image");

        when(portfolioService.getPortfolioForUser(1L)).thenReturn(List.of(img));

        mockMvc.perform(get("/photographer/portfolio").session(photographerSession()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("images", List.of(img)));
    }

    // ── POST /photographer/portfolio/upload ───────────────────────────────────

    /**
     * Unauthenticated upload must redirect to login, not throw a 500.
     */
    @Test
    void uploadImage_noSession_shouldRedirectToLogin() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "photo.jpg", "image/jpeg", new byte[100]);

        mockMvc.perform(multipart("/photographer/portfolio/upload")
                        .file(file)
                        .param("caption", "My photo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // Service must NOT be called when not authenticated
        verify(portfolioService, never()).addPortfolioImage(any(), any(), any());
    }

    /**
     * Successful upload: service is called, then PRG redirect to management page.
     */
    @Test
    void uploadImage_authenticatedApprovedPhotographer_success_shouldRedirectWithFlash() throws Exception {
        PortfolioImage savedImage = new PortfolioImage();
        savedImage.setImageUrl("https://res.cloudinary.com/test/abc.jpg");

        when(portfolioService.addPortfolioImage(eq(1L), any(), eq("Sunset wedding")))
                .thenReturn(savedImage);

        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "photo.jpg", "image/jpeg", new byte[100]);

        mockMvc.perform(multipart("/photographer/portfolio/upload")
                        .file(file)
                        .param("caption", "Sunset wedding")
                        .session(photographerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/portfolio"));

        verify(portfolioService).addPortfolioImage(eq(1L), any(), eq("Sunset wedding"));
    }

    /**
     * Service throws IllegalStateException (PENDING photographer tries to upload).
     * Controller must redirect with error flash — not propagate a 500.
     */
    @Test
    void uploadImage_pendingPhotographerServiceThrows_shouldRedirectWithError() throws Exception {
        when(portfolioService.addPortfolioImage(any(), any(), any()))
                .thenThrow(new IllegalStateException("Only APPROVED photographers can upload."));

        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "photo.jpg", "image/jpeg", new byte[100]);

        mockMvc.perform(multipart("/photographer/portfolio/upload")
                        .file(file)
                        .session(photographerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/portfolio"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    /**
     * Invalid file type: service throws IllegalArgumentException.
     * Controller must redirect with error flash.
     */
    @Test
    void uploadImage_invalidFile_shouldRedirectWithError() throws Exception {
        when(portfolioService.addPortfolioImage(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid file type."));

        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "bad.exe", "application/octet-stream", new byte[]{0x4D, 0x5A});

        mockMvc.perform(multipart("/photographer/portfolio/upload")
                        .file(file)
                        .session(photographerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/portfolio"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ── POST /photographer/portfolio/{id}/delete ──────────────────────────────

    /**
     * Unauthenticated delete must redirect to login, not 500.
     */
    @Test
    void deleteImage_noSession_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/photographer/portfolio/5/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(portfolioService, never()).deletePortfolioImage(any(), any());
    }

    /**
     * Successful delete: service called with session userId and path imageId.
     */
    @Test
    void deleteImage_ownImage_shouldCallServiceAndRedirect() throws Exception {
        doNothing().when(portfolioService).deletePortfolioImage(1L, 5L);

        mockMvc.perform(post("/photographer/portfolio/5/delete")
                        .session(photographerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/portfolio"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(portfolioService).deletePortfolioImage(1L, 5L);
    }

    /**
     * Ownership violation: service throws SecurityException.
     * Controller must redirect with error flash — not a 403 or 500.
     */
    @Test
    void deleteImage_anotherPhotographersImage_shouldRedirectWithError() throws Exception {
        doThrow(new SecurityException("You do not own this image."))
                .when(portfolioService).deletePortfolioImage(1L, 99L);

        mockMvc.perform(post("/photographer/portfolio/99/delete")
                        .session(photographerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/portfolio"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ── TASK-B02 Enhancements: Category and Cover Actions ─────────────────────

    @Test
    void uploadImage_withCategory_shouldCallServiceAndRedirectWithFlash() throws Exception {
        PortfolioImage savedImage = new PortfolioImage();
        savedImage.setImageUrl("https://res.cloudinary.com/test/portrait.jpg");

        when(portfolioService.addPortfolioImage(eq(1L), any(), eq("Portrait series"), eq(PortfolioCategory.PORTRAIT)))
                .thenReturn(savedImage);

        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "photo.jpg", "image/jpeg", new byte[100]);

        mockMvc.perform(multipart("/photographer/portfolio/upload")
                        .file(file)
                        .param("category", "PORTRAIT")
                        .param("caption", "Portrait series")
                        .session(photographerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/portfolio"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(portfolioService).addPortfolioImage(eq(1L), any(), eq("Portrait series"), eq(PortfolioCategory.PORTRAIT));
    }

    @Test
    void uploadImage_withInvalidCategory_shouldRedirectWithErrorFlash() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "photo.jpg", "image/jpeg", new byte[100]);

        mockMvc.perform(multipart("/photographer/portfolio/upload")
                        .file(file)
                        .param("category", "INVALID_CAT")
                        .session(photographerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/portfolio"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(portfolioService, never()).addPortfolioImage(any(), any(), any(), any());
    }

    @Test
    void setCoverImage_authenticatedPhotographer_shouldCallServiceAndRedirect() throws Exception {
        doNothing().when(portfolioService).setCoverImage(1L, 10L);

        mockMvc.perform(post("/photographer/portfolio/10/cover")
                        .session(photographerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/portfolio"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(portfolioService).setCoverImage(1L, 10L);
    }

    @Test
    void setCoverImage_noSession_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/photographer/portfolio/10/cover"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(portfolioService, never()).setCoverImage(any(), any());
    }

    @Test
    void setCoverImage_customerRole_shouldRedirectToRoot() throws Exception {
        MockHttpSession session = photographerSession();
        session.setAttribute("userRole", "CUSTOMER");

        mockMvc.perform(post("/photographer/portfolio/10/cover").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(portfolioService, never()).setCoverImage(any(), any());
    }

    @Test
    void setCoverImage_unownedImage_shouldRedirectWithErrorFlash() throws Exception {
        doThrow(new SecurityException("You do not own this image."))
                .when(portfolioService).setCoverImage(1L, 999L);

        mockMvc.perform(post("/photographer/portfolio/999/cover")
                        .session(photographerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photographer/portfolio"))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}
