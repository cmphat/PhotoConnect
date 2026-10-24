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
    private final com.photoconnect.service.ReportService reportService;

    public AdminReviewController(AdminReviewService adminReviewService) {
        this(adminReviewService, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AdminReviewController(AdminReviewService adminReviewService,
                                 @org.springframework.beans.factory.annotation.Autowired(required = false) com.photoconnect.service.ReportService reportService) {
        this.adminReviewService = adminReviewService;
        this.reportService = reportService;
    }

    @GetMapping
    public String listReviews(@RequestParam(value = "status", required = false) String statusParam,
                              @RequestParam(value = "tab", required = false) String tab,
                              @RequestParam(value = "reportStatus", required = false) String reportStatusParam,
                              HttpSession session,
                              Model model) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        long pendingReports = reportService != null ? reportService.countPendingReports() : 0;
        model.addAttribute("pendingReportsCount", pendingReports);
        model.addAttribute("activeTab", "reviews");

        if ("reports".equalsIgnoreCase(tab)) {
            model.addAttribute("activeTabSection", "reports");
            com.photoconnect.entity.ReportStatus statusFilter = null;
            if ("PENDING".equalsIgnoreCase(reportStatusParam)) {
                statusFilter = com.photoconnect.entity.ReportStatus.PENDING;
            } else if ("RESOLVED".equalsIgnoreCase(reportStatusParam)) {
                statusFilter = com.photoconnect.entity.ReportStatus.RESOLVED;
            } else if ("DISMISSED".equalsIgnoreCase(reportStatusParam)) {
                statusFilter = com.photoconnect.entity.ReportStatus.DISMISSED;
            }
            List<com.photoconnect.dto.ReportDto> reports = reportService != null
                    ? reportService.getReportsForAdmin(statusFilter)
                    : java.util.Collections.emptyList();
            model.addAttribute("reports", reports);
            model.addAttribute("activeReportStatus", reportStatusParam != null ? reportStatusParam.toUpperCase() : "ALL");
            return "admin-reviews";
        }

        model.addAttribute("activeTabSection", "reviews");
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

    @PostMapping("/reports/{id}/resolve")
    public String resolveReport(@PathVariable("id") Long id,
                                @RequestParam(value = "adminNotes", required = false) String adminNotes,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) return redirect;
        Long adminUserId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        if (reportService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Report service unavailable.");
            return "redirect:/admin/reviews?tab=reports";
        }

        try {
            reportService.resolveReport(id, adminUserId, adminNotes, com.photoconnect.entity.ReportStatus.RESOLVED);
            redirectAttributes.addFlashAttribute("successMessage", "Báo cáo sự cố #" + id + " đã được đánh dấu là ĐÃ XỬ LÝ (RESOLVED).");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/reviews?tab=reports";
    }

    @PostMapping("/reports/{id}/dismiss")
    public String dismissReport(@PathVariable("id") Long id,
                                @RequestParam(value = "adminNotes", required = false) String adminNotes,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) return redirect;
        Long adminUserId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        if (reportService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Report service unavailable.");
            return "redirect:/admin/reviews?tab=reports";
        }

        try {
            reportService.resolveReport(id, adminUserId, adminNotes, com.photoconnect.entity.ReportStatus.DISMISSED);
            redirectAttributes.addFlashAttribute("successMessage", "Báo cáo sự cố #" + id + " đã được BỎ QUA / TỪ CHỐI (DISMISSED).");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/reviews?tab=reports";
    }
}
