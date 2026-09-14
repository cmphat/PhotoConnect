package com.photoconnect.controller;

import com.photoconnect.entity.Review;
import com.photoconnect.service.AdminReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    void adminGetReviews_shouldReturnReviewsView() throws Exception {
        Review review = new Review();
        review.setId(10L);
        when(adminReviewService.listReviews()).thenReturn(List.of(review));

        mockMvc.perform(get("/admin/reviews").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-reviews"))
                .andExpect(model().attributeExists("reviews"))
                .andExpect(model().attribute("activeTab", "reviews"));

        verify(adminReviewService).listReviews();
    }

    @Test
    void postReviewEndpoint_shouldNotBeAllowed_readOnlyEnforcement() throws Exception {
        mockMvc.perform(post("/admin/reviews").session(adminSession()))
                .andExpect(status().isMethodNotAllowed());
    }
}
