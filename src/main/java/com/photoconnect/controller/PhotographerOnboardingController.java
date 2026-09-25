package com.photoconnect.controller;

import com.photoconnect.dto.PhotographerProfileRequest;
import com.photoconnect.entity.UserRole;
import com.photoconnect.exception.PhotographerProfileAlreadyExistsException;
import com.photoconnect.service.PhotographerProfileService;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.security.JwtCookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final JwtCookieService jwtCookieService;

    public PhotographerOnboardingController(PhotographerProfileService photographerProfileService,
                                            JwtCookieService jwtCookieService) {
        this.photographerProfileService = photographerProfileService;
        this.jwtCookieService = jwtCookieService;
    }

    @GetMapping("/become-photographer")
    public String showOnboardingForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        String userRole = (String) session.getAttribute("userRole");
        if (UserRole.ADMIN.name().equals(userRole)) {
            return "redirect:/admin/dashboard";
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
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    HttpSession session,
                                    Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        String userRole = (String) session.getAttribute("userRole");
        if (UserRole.ADMIN.name().equals(userRole)) {
            return "redirect:/admin/dashboard";
        }

        if (bindingResult.hasErrors()) {
            return "photographer-onboarding";
        }

        try {
            PhotographerProfile profile = photographerProfileService.createProfile(userId, profileRequest);
            // The persisted role changed, so rotate the cookie immediately; the old role claim is never trusted.
            jwtCookieService.issue(request, response, profile.getUser());
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

        String userRole = (String) session.getAttribute("userRole");
        if (UserRole.ADMIN.name().equals(userRole)) {
            return "redirect:/admin/dashboard";
        }

        photographerProfileService.findByUserId(userId).ifPresent(profile -> {
            model.addAttribute("profile", profile);
            model.addAttribute("verificationStatus", profile.getVerificationStatus().name());
        });

        return "photographer-onboarding-status";
    }
}
