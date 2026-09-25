package com.photoconnect.controller;

import com.photoconnect.dto.PhotographerProfileEditRequest;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerSpecialty;
import com.photoconnect.entity.PortfolioImage;
import com.photoconnect.entity.UserRole;
import com.photoconnect.service.PhotographerProfileService;
import com.photoconnect.service.PortfolioService;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/photographer/profile")
public class PhotographerProfileController {

    private final PhotographerProfileService photographerProfileService;
    private final PortfolioService portfolioService;

    public PhotographerProfileController(PhotographerProfileService photographerProfileService,
                                         PortfolioService portfolioService) {
        this.photographerProfileService = photographerProfileService;
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public String profileRoot() {
        return "redirect:/photographer/profile/edit";
    }

    @GetMapping("/edit")
    public String showEditProfileForm(HttpSession session, Model model) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) {
            return redirect;
        }

        Long userId = SessionSecurityUtils.userId(session);
        Optional<PhotographerProfile> profileOpt = photographerProfileService.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            return "redirect:/photographer/onboarding-status";
        }

        PhotographerProfile profile = profileOpt.get();

        // Calculate actual profile completeness from portfolio & profile state
        List<PortfolioImage> portfolioImages = portfolioService.getPortfolioForUser(userId);
        int portfolioCount = portfolioImages.size();
        boolean hasCover = portfolioImages.stream().anyMatch(PortfolioImage::isCover);

        int completenessScore = photographerProfileService.calculateProfileCompleteness(profile, portfolioCount, hasCover);
        List<String> recommendations = photographerProfileService.getCompletenessRecommendations(profile, portfolioCount, hasCover);

        // Pre-populate form DTO if not already added by validation errors redirect
        if (!model.containsAttribute("editRequest")) {
            PhotographerProfileEditRequest editRequest = new PhotographerProfileEditRequest();
            editRequest.setDisplayName(profile.getDisplayName());
            editRequest.setHeadline(profile.getHeadline());
            editRequest.setBio(profile.getBio());
            editRequest.setCity(profile.getCity());
            editRequest.setCountry(profile.getCountry());
            editRequest.setExperienceYears(profile.getExperienceYears());
            editRequest.setPriceFrom(profile.getPriceFrom());
            if (profile.getSpecialtiesList() != null) {
                editRequest.setSpecialties(profile.getSpecialtiesList().stream().map(Enum::name).toList());
            }
            editRequest.setWebsiteUrl(profile.getWebsiteUrl());
            editRequest.setInstagramUrl(profile.getInstagramUrl());
            editRequest.setFacebookUrl(profile.getFacebookUrl());
            editRequest.setEquipmentSummary(profile.getEquipmentSummary());
            editRequest.setLanguages(profile.getLanguages());
            editRequest.setTravelAvailable(profile.isTravelAvailable());
            model.addAttribute("editRequest", editRequest);
        }

        model.addAttribute("profile", profile);
        model.addAttribute("allSpecialties", PhotographerSpecialty.values());
        model.addAttribute("completenessScore", completenessScore);
        model.addAttribute("completenessRecommendations", recommendations);

        return "photographer-profile-edit";
    }

    @PostMapping("/edit")
    public String processProfileUpdate(@Valid @ModelAttribute("editRequest") PhotographerProfileEditRequest editRequest,
                                       BindingResult bindingResult,
                                       HttpSession session,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) {
            return redirect;
        }

        Long userId = SessionSecurityUtils.userId(session);
        Optional<PhotographerProfile> profileOpt = photographerProfileService.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            return "redirect:/photographer/onboarding-status";
        }

        PhotographerProfile profile = profileOpt.get();

        if (bindingResult.hasErrors()) {
            populateModelAttributes(userId, profile, model);
            return "photographer-profile-edit";
        }

        try {
            photographerProfileService.updateProfile(userId, editRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Your professional profile has been successfully updated.");
            return "redirect:/photographer/profile/edit";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateModelAttributes(userId, profile, model);
            return "photographer-profile-edit";
        }
    }

    private void populateModelAttributes(Long userId, PhotographerProfile profile, Model model) {
        List<PortfolioImage> portfolioImages = portfolioService.getPortfolioForUser(userId);
        int portfolioCount = portfolioImages.size();
        boolean hasCover = portfolioImages.stream().anyMatch(PortfolioImage::isCover);

        int completenessScore = photographerProfileService.calculateProfileCompleteness(profile, portfolioCount, hasCover);
        List<String> recommendations = photographerProfileService.getCompletenessRecommendations(profile, portfolioCount, hasCover);

        model.addAttribute("profile", profile);
        model.addAttribute("allSpecialties", PhotographerSpecialty.values());
        model.addAttribute("completenessScore", completenessScore);
        model.addAttribute("completenessRecommendations", recommendations);
    }
}
