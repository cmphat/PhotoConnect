package com.photoconnect.controller;

import com.photoconnect.entity.Review;
import com.photoconnect.entity.ReviewStatus;
import com.photoconnect.exception.ReviewNotFoundException;
import com.photoconnect.service.AdminReviewService;
import com.photoconnect.util.AdminSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    public AdminReviewController(AdminReviewService adminReviewService) {
        this.adminReviewService = adminReviewService;
    }

    @GetMapping
    public String listReviews(@RequestParam(value = "status", required = false) String statusParam,
                              HttpSession session,
                              Model model) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        String activeStatus = "ALL";
        List<Review> reviews;

        if ("VISIBLE".equalsIgnoreCase(statusParam)) {
            activeStatus = "VISIBLE";
            reviews = adminReviewService.listReviewsByStatus(ReviewStatus.VISIBLE);
        } else if ("HIDDEN".equalsIgnoreCase(statusParam)) {
            activeStatus = "HIDDEN";
            reviews = adminReviewService.listReviewsByStatus(ReviewStatus.HIDDEN);
        } else {
            reviews = adminReviewService.listReviews();
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("activeStatus", activeStatus);
        model.addAttribute("activeTab", "reviews");
        model.addAttribute("allCount", adminReviewService.countAll());
        model.addAttribute("visibleCount", adminReviewService.countByStatus(ReviewStatus.VISIBLE));
        model.addAttribute("hiddenCount", adminReviewService.countByStatus(ReviewStatus.HIDDEN));

        return "admin-reviews";
    }

    @PostMapping("/{id}/hide")
    public String hideReview(@PathVariable Long id,
                             @RequestParam(value = "currentStatus", required = false) String currentStatus,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            adminReviewService.hideReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Review #" + id + " has been hidden from public display.");
        } catch (ReviewNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to hide review. Please try again.");
        }

        if (isSupportedStatusFilter(currentStatus)) {
            return "redirect:/admin/reviews?status=" + currentStatus.toUpperCase();
        }
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/unhide")
    public String unhideReview(@PathVariable Long id,
                               @RequestParam(value = "currentStatus", required = false) String currentStatus,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            adminReviewService.unhideReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Review #" + id + " has been restored to public display.");
        } catch (ReviewNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to restore review. Please try again.");
        }

        if (isSupportedStatusFilter(currentStatus)) {
            return "redirect:/admin/reviews?status=" + currentStatus.toUpperCase();
        }
        return "redirect:/admin/reviews";
    }

    private boolean isSupportedStatusFilter(String value) {
        return "VISIBLE".equalsIgnoreCase(value) || "HIDDEN".equalsIgnoreCase(value);
    }
}
