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
        if (UserRole.PHOTOGRAPHER.name().equals(userRole)) {
            return "redirect:/photographer/dashboard";
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
        if (UserRole.PHOTOGRAPHER.name().equals(userRole)) {
            return "redirect:/photographer/dashboard";
        }

        if (bindingResult.hasErrors()) {
            return "photographer-onboarding";
        }

        try {
            PhotographerProfile profile = photographerProfileService.createProfile(userId, profileRequest);
            // User retains CUSTOMER role while application is PENDING.
            session.setAttribute("userRole", UserRole.CUSTOMER.name());
            return "redirect:/photographer/onboarding-status";
        } catch (PhotographerProfileAlreadyExistsException e) {
            return "redirect:/photographer/onboarding-status";
        } catch (Exception e) {
            model.addAttribute("onboardingError", "An error occurred. Please try again.");
            return "photographer-onboarding";
        }
    }

    @GetMapping("/photographer/onboarding-status")
    public String showOnboardingStatus(@org.springframework.web.bind.annotation.RequestParam(value = "resubmitted", required = false) Boolean resubmitted,
                                       HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        String userRole = (String) session.getAttribute("userRole");
        if (UserRole.ADMIN.name().equals(userRole)) {
            return "redirect:/admin/dashboard";
        }

        if (Boolean.TRUE.equals(resubmitted)) {
            model.addAttribute("resubmitSuccess", "Hồ sơ của bạn đã được gửi lại để xét duyệt thành công!");
        }

        photographerProfileService.findByUserId(userId).ifPresent(profile -> {
            model.addAttribute("profile", profile);
            model.addAttribute("verificationStatus", profile.getVerificationStatus().name());
            model.addAttribute("rejectionReason", profile.getRejectionReason());
        });

        return "photographer-onboarding-status";
    }

    @GetMapping("/photographer/onboarding/reapply")
    public String showReapplyForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        var profileOpt = photographerProfileService.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            return "redirect:/become-photographer";
        }

        var profile = profileOpt.get();
        if (profile.getVerificationStatus() != com.photoconnect.entity.PhotographerVerificationStatus.REJECTED) {
            return "redirect:/photographer/onboarding-status";
        }

        PhotographerProfileRequest request = new PhotographerProfileRequest();
        request.setDisplayName(profile.getDisplayName());
        request.setBio(profile.getBio());
        request.setCity(profile.getCity());
        request.setExperienceYears(profile.getExperienceYears());
        request.setPriceFrom(profile.getPriceFrom());

        model.addAttribute("profileRequest", request);
        model.addAttribute("isReapply", true);
        model.addAttribute("rejectionReason", profile.getRejectionReason());
        return "photographer-onboarding";
    }

    @PostMapping("/photographer/onboarding/reapply")
    public String processReapply(@Valid @ModelAttribute("profileRequest") PhotographerProfileRequest profileRequest,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("isReapply", true);
            return "photographer-onboarding";
        }

        try {
            photographerProfileService.reapply(userId, profileRequest);
            return "redirect:/photographer/onboarding-status?resubmitted=true";
        } catch (Exception e) {
            model.addAttribute("onboardingError", e.getMessage());
            model.addAttribute("isReapply", true);
            return "photographer-onboarding";
        }
    }
}
