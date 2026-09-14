package com.photoconnect.controller;

import com.photoconnect.entity.Review;
import com.photoconnect.service.AdminReviewService;
import com.photoconnect.util.AdminSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    public AdminReviewController(AdminReviewService adminReviewService) {
        this.adminReviewService = adminReviewService;
    }

    @GetMapping
    public String listReviews(HttpSession session, Model model) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        List<Review> reviews = adminReviewService.listReviews();

        model.addAttribute("reviews", reviews);
        model.addAttribute("activeTab", "reviews");

        return "admin-reviews";
    }
}
