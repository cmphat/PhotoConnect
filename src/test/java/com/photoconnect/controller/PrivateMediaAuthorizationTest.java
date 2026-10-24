package com.photoconnect.controller;

import com.photoconnect.entity.UserRole;
import com.photoconnect.exception.GlobalExceptionHandler;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.service.DeliveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PrivateMediaAuthorizationTest {

    @Mock
    private DeliveryService deliveryService;

    private MockMvc customerMockMvc;
    private MockMvc photographerMockMvc;

    @BeforeEach
    void setUp() {
        CustomerDeliveryController customerController = new CustomerDeliveryController(deliveryService);
        PhotographerDeliveryController photographerController = new PhotographerDeliveryController(deliveryService);

        customerMockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        photographerMockMvc = MockMvcBuilders.standaloneSetup(photographerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── Anonymous Access (SEC-04) ───────────────────────────────────────────

    @Test
    @DisplayName("Anonymous user cannot download customer photo -> redirects to login")
    void anonymous_downloadCustomerPhoto_redirectsToLogin() throws Exception {
        customerMockMvc.perform(get("/bookings/500/delivery/photos/101/download"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(deliveryService);
    }

    @Test
    @DisplayName("Anonymous user cannot preview customer photo -> redirects to login")
    void anonymous_previewCustomerPhoto_redirectsToLogin() throws Exception {
        customerMockMvc.perform(get("/bookings/500/delivery/photos/101/preview"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(deliveryService);
    }

    @Test
    @DisplayName("Anonymous user cannot view customer thumbnail -> redirects to login")
    void anonymous_thumbnailCustomerPhoto_redirectsToLogin() throws Exception {
        customerMockMvc.perform(get("/bookings/500/delivery/photos/101/thumbnail"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(deliveryService);
    }

    // ── Negative Authorization (Stranger Blocked 403) ───────────────────────

    @Test
    @DisplayName("Unauthorized stranger cannot download photo -> 403 Forbidden")
    void stranger_downloadPhoto_returns403Forbidden() throws Exception {
        when(deliveryService.getAuthorizedDownloadUrl(500L, 101L, 99L))
                .thenThrow(new UnauthorizedException("You are not authorized to access photos from this booking."));

        customerMockMvc.perform(get("/bookings/500/delivery/photos/101/download")
                        .sessionAttr("userId", 99L)
                        .sessionAttr("userRole", UserRole.CUSTOMER))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthorized stranger cannot preview photo -> 403 Forbidden")
    void stranger_previewPhoto_returns403Forbidden() throws Exception {
        when(deliveryService.getAuthorizedPreviewUrl(500L, 101L, 99L))
                .thenThrow(new UnauthorizedException("You are not authorized to access photos from this booking."));

        customerMockMvc.perform(get("/bookings/500/delivery/photos/101/preview")
                        .sessionAttr("userId", 99L)
                        .sessionAttr("userRole", UserRole.CUSTOMER))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthorized stranger cannot view thumbnail -> 403 Forbidden")
    void stranger_thumbnailPhoto_returns403Forbidden() throws Exception {
        when(deliveryService.getAuthorizedThumbnailUrl(500L, 101L, 99L))
                .thenThrow(new UnauthorizedException("You are not authorized to access photos from this booking."));

        customerMockMvc.perform(get("/bookings/500/delivery/photos/101/thumbnail")
                        .sessionAttr("userId", 99L)
                        .sessionAttr("userRole", UserRole.CUSTOMER))
                .andExpect(status().isForbidden());
    }

    // ── Positive Authorization (Redirects to Short-Lived Signed URL) ────────

    @Test
    @DisplayName("Authorized customer receives short-lived signed download redirect")
    void authorizedCustomer_downloadPhoto_redirectsToSignedUrl() throws Exception {
        String signedDownloadUrl = "https://res.cloudinary.com/demo/image/upload/s--xyz--/fl_attachment/deliv_1.jpg?__cld_token__=exp=1700000";
        when(deliveryService.getAuthorizedDownloadUrl(500L, 101L, 20L))
                .thenReturn(signedDownloadUrl);

        customerMockMvc.perform(get("/bookings/500/delivery/photos/101/download")
                        .sessionAttr("userId", 20L)
                        .sessionAttr("userRole", UserRole.CUSTOMER))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(signedDownloadUrl));
    }

    @Test
    @DisplayName("Authorized customer receives short-lived signed preview redirect")
    void authorizedCustomer_previewPhoto_redirectsToSignedUrl() throws Exception {
        String signedPreviewUrl = "https://res.cloudinary.com/demo/image/upload/s--preview--/c_limit,w_1600/deliv_1.jpg?__cld_token__=exp=1700000";
        when(deliveryService.getAuthorizedPreviewUrl(500L, 101L, 20L))
                .thenReturn(signedPreviewUrl);

        customerMockMvc.perform(get("/bookings/500/delivery/photos/101/preview")
                        .sessionAttr("userId", 20L)
                        .sessionAttr("userRole", UserRole.CUSTOMER))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(signedPreviewUrl));
    }

    @Test
    @DisplayName("Authorized customer receives short-lived signed thumbnail redirect")
    void authorizedCustomer_thumbnailPhoto_redirectsToSignedUrl() throws Exception {
        String signedThumbUrl = "https://res.cloudinary.com/demo/image/upload/s--thumb--/c_fill,w_500/deliv_1.jpg?__cld_token__=exp=1700000";
        when(deliveryService.getAuthorizedThumbnailUrl(500L, 101L, 20L))
                .thenReturn(signedThumbUrl);

        customerMockMvc.perform(get("/bookings/500/delivery/photos/101/thumbnail")
                        .sessionAttr("userId", 20L)
                        .sessionAttr("userRole", UserRole.CUSTOMER))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(signedThumbUrl));
    }

    // ── Photographer Delivery Endpoint Authorization ────────────────────────

    @Test
    @DisplayName("Anonymous user accessing photographer delivery photo -> redirects to login")
    void anonymous_accessingPhotographerDelivery_redirectsToLogin() throws Exception {
        photographerMockMvc.perform(get("/photographer/bookings/500/delivery/photos/101/download"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(deliveryService);
    }

    @Test
    @DisplayName("Customer role accessing photographer delivery photo -> redirects to home")
    void customerRole_accessingPhotographerDelivery_redirectsToHome() throws Exception {
        photographerMockMvc.perform(get("/photographer/bookings/500/delivery/photos/101/download")
                        .sessionAttr("userId", 20L)
                        .sessionAttr("userRole", UserRole.CUSTOMER))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verifyNoInteractions(deliveryService);
    }

    @Test
    @DisplayName("Assigned photographer can download deliverable photo")
    void authorizedPhotographer_downloadPhoto_redirectsToSignedUrl() throws Exception {
        String signedDownload = "https://res.cloudinary.com/demo/image/authenticated/s--photog--/fl_attachment/deliv_1.jpg";
        when(deliveryService.getAuthorizedDownloadUrl(500L, 101L, 10L))
                .thenReturn(signedDownload);

        photographerMockMvc.perform(get("/photographer/bookings/500/delivery/photos/101/download")
                        .sessionAttr("userId", 10L)
                        .sessionAttr("userRole", UserRole.PHOTOGRAPHER))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(signedDownload));
    }
}
