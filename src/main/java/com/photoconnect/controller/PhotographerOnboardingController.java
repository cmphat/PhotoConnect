package com.photoconnect.controller;

import com.photoconnect.dto.PhotographerProfileRequest;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.PhotographerProfileAlreadyExistsException;
import com.photoconnect.service.PhotographerProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;

@Controller
public class PhotographerOnboardingController {

    private final PhotographerProfileService photographerProfileService;

    public PhotographerOnboardingController(PhotographerProfileService photographerProfileService) {
        this.photographerProfileService = photographerProfileService;
    }

    @GetMapping("/become-photographer")
    public String showOnboardingForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        if (photographerProfileService.findByUserId(userId).isPresent()) {
            return "redirect:/photographer/onboarding-status";
        }

        model.addAttribute("profileRequest", new PhotographerProfileRequest());
        return "photographer-onboarding";
    }

    @PostMapping("/become-photographer")
    public String processOnboarding(@Valid @ModelAttribute("profileRequest") PhotographerProfileRequest profileRequest,
                                    BindingResult bindingResult,
                                    HttpSession session,
                                    Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "photographer-onboarding";
        }

        try {
            photographerProfileService.createProfile(userId, profileRequest);
            // Update session role so the UI reflects the change immediately without re-login
            session.setAttribute("userRole", UserRole.PHOTOGRAPHER.name());
            return "redirect:/photographer/onboarding-status";
        } catch (PhotographerProfileAlreadyExistsException e) {
            return "redirect:/photographer/onboarding-status";
        } catch (Exception e) {
            model.addAttribute("onboardingError", "An error occurred. Please try again.");
            return "photographer-onboarding";
        }
    }

    @GetMapping("/photographer/onboarding-status")
    public String showOnboardingStatus(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        photographerProfileService.findByUserId(userId).ifPresent(profile -> {
            model.addAttribute("profile", profile);
            model.addAttribute("verificationStatus", profile.getVerificationStatus().name());
        });

        return "photographer-onboarding-status";
    }
}
