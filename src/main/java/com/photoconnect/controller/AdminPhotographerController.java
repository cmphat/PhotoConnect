package com.photoconnect.controller;

import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.InvalidStatusTransitionException;
import com.photoconnect.service.AdminPhotographerService;
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

    // ── Authorization helper ──────────────────────────────────────────────
    private boolean isAdmin(HttpSession session) {
        Object role = session.getAttribute("userRole");
        return role != null && UserRole.ADMIN.name().equals(role.toString());
    }

    private String requireAdmin(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        if (!isAdmin(session)) {
            return "redirect:/";  // logged in but not admin
        }
        return null;
    }

    // ── GET /admin/photographers ──────────────────────────────────────────
    @GetMapping
    public String listPendingApplications(HttpSession session, Model model) {
        String redirect = requireAdmin(session);
        if (redirect != null) return redirect;

        List<PhotographerProfile> pending = adminPhotographerService.listPendingApplications();
        model.addAttribute("applications", pending);
        return "admin-photographers";
    }

    // ── GET /admin/photographers/{id} ─────────────────────────────────────
    @GetMapping("/{id}")
    public String viewApplicationDetail(@PathVariable Long id, HttpSession session, Model model) {
        String redirect = requireAdmin(session);
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
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        String redirect = requireAdmin(session);
        if (redirect != null) return redirect;

        try {
            adminPhotographerService.approve(id);
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
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        String redirect = requireAdmin(session);
        if (redirect != null) return redirect;

        try {
            adminPhotographerService.reject(id);
            redirectAttributes.addFlashAttribute("successMessage", "Application rejected.");
        } catch (InvalidStatusTransitionException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Application not found.");
        }
        return "redirect:/admin/photographers/" + id;
    }
}
