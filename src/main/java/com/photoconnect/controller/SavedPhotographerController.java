package com.photoconnect.controller;

import com.photoconnect.dto.SavedPhotographerDto;
import com.photoconnect.entity.UserRole;
import com.photoconnect.service.SavedPhotographerService;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
public class SavedPhotographerController {

    private final SavedPhotographerService savedPhotographerService;

    public SavedPhotographerController(SavedPhotographerService savedPhotographerService) {
        this.savedPhotographerService = savedPhotographerService;
    }

    /**
     * Customer view for saved/favorited photographers.
     */
    @GetMapping("/customer/saved-photographers")
    public String viewSavedPhotographers(HttpSession session,
                                         HttpServletResponse response,
                                         Model model) throws IOException {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        if (!SessionSecurityUtils.hasRole(session, UserRole.CUSTOMER)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only customers can access saved photographers.");
            return null;
        }

        List<SavedPhotographerDto> savedList = savedPhotographerService.getSavedPhotographers(userId);
        model.addAttribute("savedPhotographers", savedList);
        model.addAttribute("savedCount", savedList.size());
        return "saved-photographers";
    }

    /**
     * Saves an approved photographer to customer favorites.
     */
    @PostMapping("/photographers/{id}/save")
    public String savePhotographer(@PathVariable("id") Long id,
                                   @RequestParam(value = "redirect", required = false) String redirectUrl,
                                   HttpSession session,
                                   HttpServletResponse response,
                                   RedirectAttributes redirectAttributes) throws IOException {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        if (!SessionSecurityUtils.hasRole(session, UserRole.CUSTOMER)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only customers can save photographers.");
            return null;
        }

        try {
            savedPhotographerService.savePhotographer(userId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Photographer saved to your favorites.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return resolveRedirect(redirectUrl, id);
    }

    /**
     * Removes a photographer from customer favorites.
     */
    @PostMapping("/photographers/{id}/unsave")
    public String unsavePhotographer(@PathVariable("id") Long id,
                                     @RequestParam(value = "redirect", required = false) String redirectUrl,
                                     HttpSession session,
                                     HttpServletResponse response,
                                     RedirectAttributes redirectAttributes) throws IOException {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        if (!SessionSecurityUtils.hasRole(session, UserRole.CUSTOMER)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only customers can unsave photographers.");
            return null;
        }

        savedPhotographerService.removeSavedPhotographer(userId, id);
        redirectAttributes.addFlashAttribute("successMessage", "Photographer removed from your favorites.");

        return resolveRedirect(redirectUrl, id);
    }

    private String resolveRedirect(String redirectUrl, Long photographerId) {
        if (redirectUrl != null && redirectUrl.startsWith("/") && !redirectUrl.startsWith("//")) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/photographers/" + photographerId;
    }
}
