package com.photoconnect.controller;

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
 * WHY POST for delete (not GET /photographer/portfolio/{id}/delete):
 *   GET requests must be idempotent and safe. A delete-via-GET would allow:
 *   - Accidental deletion from browser pre-fetch
 *   - CSRF via embedded image tags: <img src="/photographer/portfolio/42/delete">
 *   POST with a form submit is the correct HTTP semantic for a destructive action.
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
     * Shows current portfolio images and the upload form.
     */
    @GetMapping
    public String portfolioManagementPage(HttpSession session, Model model) {
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        List<PortfolioImage> images = portfolioService.getPortfolioForUser(userId);
        model.addAttribute("images", images);
        return "photographer-portfolio";
    }

    // ── POST /photographer/portfolio/upload ────────────────────────────────────

    /**
     * Handles portfolio image upload.
     * Requires the authenticated user to be an APPROVED photographer.
     *
     * Uses POST-Redirect-GET (PRG) pattern:
     *   On success → redirect to portfolio page (flash success message)
     *   On failure → redirect to portfolio page (flash error message)
     *
     * WHY PRG:
     *   Without redirect, pressing F5 on the result page would re-submit the upload.
     *   PRG makes the final response a GET, so refresh is safe.
     */
    @PostMapping("/upload")
    public String uploadImage(@RequestParam("imageFile") MultipartFile imageFile,
                              @RequestParam(value = "caption", required = false) String caption,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        try {
            portfolioService.addPortfolioImage(userId, imageFile, caption);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Image uploaded successfully to your portfolio!");
        } catch (IllegalStateException e) {
            // Profile not found or not APPROVED
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            // File validation failure
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (RuntimeException e) {
            // Cloudinary or DB failure
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Upload failed. Please try again.");
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
