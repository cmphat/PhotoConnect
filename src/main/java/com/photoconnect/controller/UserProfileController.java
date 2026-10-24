package com.photoconnect.controller;

import com.photoconnect.dto.UserProfileDto;
import com.photoconnect.dto.UserProfileUpdateRequest;
import com.photoconnect.service.UserProfileService;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserProfileController {

    private static final Logger log = LoggerFactory.getLogger(UserProfileController.class);

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    public String profileRoot(HttpSession session) {
        Long currentUserId = SessionSecurityUtils.userId(session);
        if (currentUserId == null) {
            return "redirect:/login";
        }
        return "redirect:/profile/me";
    }

    @GetMapping("/profile/me")
    public String myProfile(HttpSession session, Model model) {
        Long currentUserId = SessionSecurityUtils.userId(session);
        if (currentUserId == null) {
            return "redirect:/login";
        }
        return renderProfile(currentUserId, currentUserId, model);
    }

    @GetMapping("/profile/{idOrUsername}")
    public String profileByIdOrUsername(@PathVariable("idOrUsername") String idOrUsername,
                                        HttpSession session,
                                        Model model) {
        Long currentUserId = SessionSecurityUtils.userId(session);
        try {
            // Check if argument is a numeric ID
            Long targetUserId = Long.parseLong(idOrUsername);
            return renderProfile(targetUserId, currentUserId, model);
        } catch (NumberFormatException e) {
            // Otherwise lookup by username
            try {
                UserProfileDto dto = userProfileService.getProfileByUsername(idOrUsername, currentUserId);
                model.addAttribute("profile", dto);
                model.addAttribute("isOwner", dto.isOwner());
                return "user-profile";
            } catch (IllegalArgumentException ex) {
                return "redirect:/?error=UserNotFound";
            }
        }
    }

    @GetMapping("/users/{id}")
    public String userProfileById(@PathVariable("id") Long id,
                                  HttpSession session,
                                  Model model) {
        Long currentUserId = SessionSecurityUtils.userId(session);
        return renderProfile(id, currentUserId, model);
    }

    private String renderProfile(Long targetUserId, Long currentUserId, Model model) {
        try {
            UserProfileDto dto = userProfileService.getProfile(targetUserId, currentUserId);
            model.addAttribute("profile", dto);
            model.addAttribute("isOwner", dto.isOwner());
            return "user-profile";
        } catch (IllegalArgumentException e) {
            return "redirect:/?error=UserNotFound";
        }
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute("updateRequest") UserProfileUpdateRequest request,
                                BindingResult bindingResult,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long currentUserId = SessionSecurityUtils.userId(session);
        if (currentUserId == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/profile/me";
        }

        try {
            userProfileService.updateProfile(currentUserId, request);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile/me";
    }

    @PostMapping("/profile/avatar")
    public String uploadAvatar(@RequestParam("avatarFile") MultipartFile avatarFile,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long currentUserId = SessionSecurityUtils.userId(session);
        if (currentUserId == null) {
            return "redirect:/login";
        }

        try {
            userProfileService.updateAvatar(currentUserId, avatarFile);
            redirectAttributes.addFlashAttribute("successMessage", "Profile avatar updated successfully.");
        } catch (Exception e) {
            log.error("Failed to upload avatar: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile/me";
    }

    @PostMapping("/profile/avatar/remove")
    public String removeAvatar(HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long currentUserId = SessionSecurityUtils.userId(session);
        if (currentUserId == null) {
            return "redirect:/login";
        }

        try {
            userProfileService.removeAvatar(currentUserId);
            redirectAttributes.addFlashAttribute("successMessage", "Avatar removed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile/me";
    }
}
