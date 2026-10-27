package com.photoconnect.controller;

import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.exception.InvalidStatusTransitionException;
import com.photoconnect.service.AdminPhotographerService;
import com.photoconnect.util.AdminSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/photographers")
public class AdminPhotographerController {

    private final AdminPhotographerService adminPhotographerService;

    public AdminPhotographerController(AdminPhotographerService adminPhotographerService) {
        this.adminPhotographerService = adminPhotographerService;
    }

    // ── GET /admin/photographers ──────────────────────────────────────────
    @GetMapping
    public String listPhotographers(@org.springframework.web.bind.annotation.RequestParam(value = "status", required = false) String statusStr,
                                    HttpSession session, Model model) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) return redirect;

        List<PhotographerProfile> profiles;
        String currentStatus;

        if (statusStr == null || statusStr.isBlank() || "PENDING".equalsIgnoreCase(statusStr)) {
            profiles = adminPhotographerService.listPendingApplications();
            currentStatus = "PENDING";
        } else if ("ALL".equalsIgnoreCase(statusStr)) {
            profiles = adminPhotographerService.listAllPhotographers();
            currentStatus = "ALL";
        } else {
            try {
                PhotographerVerificationStatus verStatus = PhotographerVerificationStatus.valueOf(statusStr.trim().toUpperCase());
                profiles = adminPhotographerService.listPhotographersByStatus(verStatus);
                currentStatus = verStatus.name();
            } catch (IllegalArgumentException e) {
                profiles = adminPhotographerService.listPendingApplications();
                currentStatus = "PENDING";
            }
        }

        model.addAttribute("applications", profiles);
        model.addAttribute("photographers", profiles);
        model.addAttribute("currentStatus", currentStatus);
        model.addAttribute("activeTab", "photographers");
        return "admin-photographers";
    }

    // ── GET /admin/photographers/{id} ─────────────────────────────────────
    @GetMapping("/{id}")
    public String viewApplicationDetail(@PathVariable Long id, HttpSession session, Model model) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) return redirect;

        try {
            PhotographerProfile profile = adminPhotographerService.getApplicationById(id);
            model.addAttribute("profile", profile);
            return "admin-photographer-detail";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/photographers";
        }
    }

    // ── POST /admin/photographers/{id}/approve ────────────────────────────
    @PostMapping("/{id}/approve")
    public String approveApplication(@PathVariable Long id,
                                     @org.springframework.web.bind.annotation.RequestParam(value = "adminNotes", required = false) String adminNotes,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) return redirect;

        Long adminId = (Long) session.getAttribute("userId");
        try {
            if (adminNotes != null && !adminNotes.isBlank()) {
                adminPhotographerService.approve(id, adminId, adminNotes);
            } else {
                adminPhotographerService.approve(id);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Application approved successfully.");
        } catch (InvalidStatusTransitionException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Application not found.");
        }
        return "redirect:/admin/photographers/" + id;
    }

    // ── POST /admin/photographers/{id}/reject ─────────────────────────────
    @PostMapping("/{id}/reject")
    public String rejectApplication(@PathVariable Long id,
                                    @org.springframework.web.bind.annotation.RequestParam(value = "rejectionReason", required = false) String rejectionReason,
                                    @org.springframework.web.bind.annotation.RequestParam(value = "adminNotes", required = false) String adminNotes,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) return redirect;

        Long adminId = (Long) session.getAttribute("userId");
        try {
            if ((rejectionReason != null && !rejectionReason.isBlank()) || (adminNotes != null && !adminNotes.isBlank())) {
                adminPhotographerService.reject(id, adminId, rejectionReason, adminNotes);
            } else {
                adminPhotographerService.reject(id);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Application rejected.");
        } catch (InvalidStatusTransitionException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Application not found.");
        }
        return "redirect:/admin/photographers/" + id;
    }

    // ── POST /admin/photographers/{id}/suspend ───────────────────────────
    @PostMapping("/{id}/suspend")
    public String suspendPhotographer(@PathVariable Long id,
                                      @org.springframework.web.bind.annotation.RequestParam("suspensionReason") String suspensionReason,
                                      @org.springframework.web.bind.annotation.RequestParam(value = "adminNotes", required = false) String adminNotes,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) return redirect;

        Long adminId = (Long) session.getAttribute("userId");
        try {
            adminPhotographerService.suspend(id, adminId, suspensionReason, adminNotes);
            redirectAttributes.addFlashAttribute("successMessage", "Photographer suspended successfully.");
        } catch (InvalidStatusTransitionException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Photographer not found.");
        }
        return "redirect:/admin/photographers/" + id;
    }
}

