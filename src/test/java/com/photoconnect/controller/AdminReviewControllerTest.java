package com.photoconnect.controller;

import com.photoconnect.entity.Review;
import com.photoconnect.entity.ReviewStatus;
import com.photoconnect.exception.ReviewNotFoundException;
import com.photoconnect.service.AdminReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminReviewController.class)
class AdminReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminReviewService adminReviewService;

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
    void unauthenticatedGetReviews_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin/reviews"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void customerGetReviews_shouldRedirectToHome() throws Exception {
        mockMvc.perform(get("/admin/reviews").session(customerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void photographerGetReviews_shouldRedirectToHome() throws Exception {
        mockMvc.perform(get("/admin/reviews").session(photographerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void adminGetReviews_shouldReturnReviewsViewWithCounts() throws Exception {
        Review review = new Review();
        review.setId(10L);
        when(adminReviewService.listReviews()).thenReturn(List.of(review));
        when(adminReviewService.countAll()).thenReturn(5L);
        when(adminReviewService.countByStatus(ReviewStatus.VISIBLE)).thenReturn(4L);
        when(adminReviewService.countByStatus(ReviewStatus.HIDDEN)).thenReturn(1L);

        mockMvc.perform(get("/admin/reviews").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-reviews"))
                .andExpect(model().attributeExists("reviews"))
                .andExpect(model().attribute("activeStatus", "ALL"))
                .andExpect(model().attribute("activeTab", "reviews"))
                .andExpect(model().attribute("allCount", 5L))
                .andExpect(model().attribute("visibleCount", 4L))
                .andExpect(model().attribute("hiddenCount", 1L));

        verify(adminReviewService).listReviews();
    }

    @Test
    void adminGetReviews_withStatusVisible_shouldFilterVisibleReviews() throws Exception {
        Review review = new Review();
        review.setId(11L);
        when(adminReviewService.listReviewsByStatus(ReviewStatus.VISIBLE)).thenReturn(List.of(review));

        mockMvc.perform(get("/admin/reviews?status=VISIBLE").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-reviews"))
                .andExpect(model().attribute("activeStatus", "VISIBLE"))
                .andExpect(model().attributeExists("reviews"));

        verify(adminReviewService).listReviewsByStatus(ReviewStatus.VISIBLE);
    }

    @Test
    void adminGetReviews_withStatusHidden_shouldFilterHiddenReviews() throws Exception {
        Review review = new Review();
        review.setId(12L);
        when(adminReviewService.listReviewsByStatus(ReviewStatus.HIDDEN)).thenReturn(List.of(review));

        mockMvc.perform(get("/admin/reviews?status=HIDDEN").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-reviews"))
                .andExpect(model().attribute("activeStatus", "HIDDEN"))
                .andExpect(model().attributeExists("reviews"));

        verify(adminReviewService).listReviewsByStatus(ReviewStatus.HIDDEN);
    }

    @Test
    void unauthenticatedHideReview_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/admin/reviews/10/hide"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(adminReviewService, never()).hideReview(anyLong());
    }

    @Test
    void customerHideReview_shouldRedirectToHome() throws Exception {
        mockMvc.perform(post("/admin/reviews/10/hide").session(customerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(adminReviewService, never()).hideReview(anyLong());
    }

    @Test
    void adminHideReview_shouldCallServiceAndRedirect() throws Exception {
        when(adminReviewService.hideReview(10L)).thenReturn(new Review());

        mockMvc.perform(post("/admin/reviews/10/hide").session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reviews"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(adminReviewService).hideReview(10L);
    }

    @Test
    void adminHideReview_withCurrentStatus_shouldRedirectPreservingStatus() throws Exception {
        when(adminReviewService.hideReview(10L)).thenReturn(new Review());

        mockMvc.perform(post("/admin/reviews/10/hide")
                        .param("currentStatus", "VISIBLE")
                        .session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reviews?status=VISIBLE"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(adminReviewService).hideReview(10L);
    }

    @Test
    void adminHideReview_whenServiceThrowsException_shouldSetErrorMessageAndRedirect() throws Exception {
        when(adminReviewService.hideReview(999L)).thenThrow(new ReviewNotFoundException("Review not found with ID: 999"));

        mockMvc.perform(post("/admin/reviews/999/hide").session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reviews"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(adminReviewService).hideReview(999L);
    }

    @Test
    void unauthenticatedUnhideReview_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/admin/reviews/10/unhide"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(adminReviewService, never()).unhideReview(anyLong());
    }

    @Test
    void customerUnhideReview_shouldRedirectToHome() throws Exception {
        mockMvc.perform(post("/admin/reviews/10/unhide").session(customerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(adminReviewService, never()).unhideReview(anyLong());
    }

    @Test
    void adminUnhideReview_shouldCallServiceAndRedirect() throws Exception {
        when(adminReviewService.unhideReview(10L)).thenReturn(new Review());

        mockMvc.perform(post("/admin/reviews/10/unhide").session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reviews"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(adminReviewService).unhideReview(10L);
    }

    @Test
    void adminUnhideReview_withCurrentStatus_shouldRedirectPreservingStatus() throws Exception {
        when(adminReviewService.unhideReview(10L)).thenReturn(new Review());

        mockMvc.perform(post("/admin/reviews/10/unhide")
                        .param("currentStatus", "HIDDEN")
                        .session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reviews?status=HIDDEN"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(adminReviewService).unhideReview(10L);
    }

    @Test
    void postReviewEndpoint_shouldNotBeAllowed_readOnlyEnforcement() throws Exception {
        mockMvc.perform(post("/admin/reviews").session(adminSession()))
                .andExpect(status().isMethodNotAllowed());
    }
}
