package com.photoconnect.controller;

import com.photoconnect.dto.ServicePackageDto;
import com.photoconnect.dto.ServicePackageRequest;
import com.photoconnect.entity.PortfolioCategory;
import com.photoconnect.entity.UserRole;
import com.photoconnect.service.ServicePackageService;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/photographer/packages")
public class PhotographerPackageController {

    private static final Logger log = LoggerFactory.getLogger(PhotographerPackageController.class);

    private final ServicePackageService servicePackageService;

    public PhotographerPackageController(ServicePackageService servicePackageService) {
        this.servicePackageService = servicePackageService;
    }

    @GetMapping
    public String viewPackages(HttpSession session, Model model) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);
        List<ServicePackageDto> packages = servicePackageService.getAllPackagesForStudio(userId);
        long activeCount = packages.stream().filter(ServicePackageDto::isActive).count();

        model.addAttribute("packages", packages);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("categories", PortfolioCategory.values());
        return "photographer-packages";
    }

    @PostMapping
    public String createPackage(@ModelAttribute ServicePackageRequest request,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);
        try {
            servicePackageService.createPackage(userId, request);
            redirectAttributes.addFlashAttribute("successMessage", "Service package created successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to create service package for user [{}]: {}", userId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create service package. Please check your inputs.");
        }
        return "redirect:/photographer/packages";
    }

    @PostMapping("/{id}/edit")
    public String updatePackage(@PathVariable("id") Long id,
                                @ModelAttribute ServicePackageRequest request,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);
        try {
            servicePackageService.updatePackage(userId, id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Service package updated successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to update service package [{}] for user [{}]: {}", id, userId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update service package.");
        }
        return "redirect:/photographer/packages";
    }

    @PostMapping("/{id}/toggle")
    public String togglePackage(@PathVariable("id") Long id,
                                @RequestParam("active") boolean active,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);
        try {
            servicePackageService.togglePackageActive(userId, id, active);
            redirectAttributes.addFlashAttribute("successMessage",
                    active ? "Package activated." : "Package deactivated.");
        } catch (Exception e) {
            log.error("Failed to toggle service package [{}] for user [{}]: {}", id, userId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to change package status.");
        }
        return "redirect:/photographer/packages";
    }

    @PostMapping("/{id}/delete")
    public String deletePackage(@PathVariable("id") Long id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);
        try {
            servicePackageService.deletePackage(userId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Package deleted successfully.");
        } catch (Exception e) {
            log.error("Failed to delete service package [{}] for user [{}]: {}", id, userId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete package.");
        }
        return "redirect:/photographer/packages";
    }
}
