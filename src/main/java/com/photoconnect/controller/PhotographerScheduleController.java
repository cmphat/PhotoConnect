package com.photoconnect.controller;

import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerUnavailableDate;
import com.photoconnect.entity.UserRole;
import com.photoconnect.service.PhotographerProfileService;
import com.photoconnect.service.ScheduleService;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/photographer/schedule")
public class PhotographerScheduleController {

    private final ScheduleService scheduleService;
    private final PhotographerProfileService photographerProfileService;

    public PhotographerScheduleController(ScheduleService scheduleService, PhotographerProfileService photographerProfileService) {
        this.scheduleService = scheduleService;
        this.photographerProfileService = photographerProfileService;
    }

    private Long getPhotographerProfileId(HttpSession session) {
        if (session == null) {
            return null;
        }

        if (!SessionSecurityUtils.hasRole(session, UserRole.PHOTOGRAPHER)) {
            return null;
        }

        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return null;
        }
        Optional<PhotographerProfile> profileOpt = photographerProfileService.findByUserId(userId);
        return profileOpt.map(PhotographerProfile::getId).orElse(null);
    }

    @GetMapping
    public String viewSchedule(HttpSession session, Model model) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) {
            return redirect;
        }
        Long profileId = getPhotographerProfileId(session);
        if (profileId == null) {
            return "redirect:/photographer/onboarding-status";
        }

        List<PhotographerUnavailableDate> unavailableDates = scheduleService.getUnavailableDates(profileId);
        model.addAttribute("unavailableDates", unavailableDates);
        model.addAttribute("minDate", LocalDate.now().toString());

        return "photographer-schedule";
    }

    @PostMapping("/add")
    public String addUnavailableDate(@RequestParam("date") LocalDate date,
                                     @RequestParam(value = "reason", required = false) String reason,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) {
            return redirect;
        }
        Long profileId = getPhotographerProfileId(session);
        if (profileId == null) {
            return "redirect:/photographer/onboarding-status";
        }

        try {
            scheduleService.addUnavailableDate(profileId, date, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Date marked as unavailable.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/photographer/schedule";
    }

    @PostMapping("/remove/{id}")
    public String removeUnavailableDate(@PathVariable("id") Long dateId,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) {
            return redirect;
        }
        Long profileId = getPhotographerProfileId(session);
        if (profileId == null) {
            return "redirect:/photographer/onboarding-status";
        }

        scheduleService.removeUnavailableDate(profileId, dateId);
        redirectAttributes.addFlashAttribute("successMessage", "Date is now available again.");

        return "redirect:/photographer/schedule";
    }
}
