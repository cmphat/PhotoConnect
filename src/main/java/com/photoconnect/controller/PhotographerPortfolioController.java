package com.photoconnect.controller;

import com.photoconnect.entity.PortfolioCategory;
import com.photoconnect.entity.PortfolioImage;
import com.photoconnect.entity.UserRole;
import com.photoconnect.service.PortfolioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for the authenticated photographer portfolio management page.
 *
 * WHY a separate controller (not merged with PhotographerOnboardingController):
 *   Portfolio management is a distinct domain concern from onboarding.
 *   Keeping them separate maintains single-responsibility and allows them to evolve
 *   independently (e.g., adding pagination or ordering to portfolio without
 *   touching onboarding logic).
 *
 * OWNERSHIP RULE:
 *   userId is always read from the server-side session, NEVER from form inputs or
 *   path variables that the user controls. A malicious user cannot supply another
 *   photographer's userId by modifying form data.
 *
 * WHY POST for delete and cover actions:
 *   GET requests must be idempotent and safe. Destructive or state-changing actions
 *   via GET would allow CSRF and accidental browser pre-fetch triggers.
 *   POST with a form submit is the correct HTTP semantic.
 */
@Controller
@RequestMapping("/photographer/portfolio")
public class PhotographerPortfolioController {

    private final PortfolioService portfolioService;

    public PhotographerPortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    // ── GET /photographer/portfolio ────────────────────────────────────────────

    /**
     * Portfolio management page for the authenticated photographer.
     * Shows current portfolio images, categories, current cover, and the upload form.
     */
    @GetMapping
    public String portfolioManagementPage(HttpSession session, Model model) {
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        List<PortfolioImage> images = portfolioService.getPortfolioForUser(userId);
        PortfolioImage currentCover = images.stream()
                .filter(PortfolioImage::isCover)
                .findFirst()
                .orElse(null);

        model.addAttribute("images", images);
        model.addAttribute("currentCover", currentCover);
        model.addAttribute("categories", PortfolioCategory.values());
        return "photographer-portfolio";
    }

    // ── POST /photographer/portfolio/upload ────────────────────────────────────

    /**
     * Handles portfolio image upload with category.
     * Requires the authenticated user to be an APPROVED photographer.
     *
     * Uses POST-Redirect-GET (PRG) pattern:
     *   On success → redirect to portfolio page (flash success message)
     *   On failure → redirect to portfolio page (flash error message)
     */
    @PostMapping("/upload")
    public String uploadImage(@RequestParam("imageFile") MultipartFile imageFile,
                              @RequestParam(value = "category", required = false) String categoryStr,
                              @RequestParam(value = "caption", required = false) String caption,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        try {
            if (categoryStr != null) {
                PortfolioCategory category = PortfolioCategory.fromFormValue(categoryStr);
                portfolioService.addPortfolioImage(userId, imageFile, caption, category);
            } else {
                portfolioService.addPortfolioImage(userId, imageFile, caption);
            }
            redirectAttributes.addFlashAttribute("successMessage",
                    "Image uploaded successfully to your portfolio!");
        } catch (IllegalStateException e) {
            // Profile not found or not APPROVED
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            // File or category validation failure
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (RuntimeException e) {
            // Cloudinary or DB failure
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Upload failed. Please try again.");
        }

        return "redirect:/photographer/portfolio";
    }

    // ── POST /photographer/portfolio/{id}/cover ───────────────────────────────

    /**
     * Sets a portfolio image as the portfolio cover for the authenticated photographer.
     * Guaranteed single-cover invariant enforced server-side.
     */
    @PostMapping("/{id}/cover")
    public String setCoverImage(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        try {
            portfolioService.setCoverImage(userId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Cover photograph updated successfully.");
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "You do not have permission to modify this image.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to update cover photograph. Please try again.");
        }

        return "redirect:/photographer/portfolio";
    }

    // ── POST /photographer/portfolio/{id}/delete ──────────────────────────────

    /**
     * Deletes a portfolio image.
     * Ownership is verified by PortfolioService using the session userId.
     * A photographer cannot delete another photographer's images.
     */
    @PostMapping("/{id}/delete")
    public String deleteImage(@PathVariable Long id,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        try {
            portfolioService.deletePortfolioImage(userId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Image deleted successfully.");
        } catch (SecurityException e) {
            // Ownership mismatch — this user does not own the image
            redirectAttributes.addFlashAttribute("errorMessage",
                    "You do not have permission to delete this image.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (RuntimeException e) {
            // Cloudinary deletion failure
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Delete failed. Please try again.");
        }

        return "redirect:/photographer/portfolio";
    }
}
