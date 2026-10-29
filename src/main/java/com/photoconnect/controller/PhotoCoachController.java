package com.photoconnect.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PhotoCoachController {

    @GetMapping("/photo-coach")
    public String showPhotoCoach(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Object sessionUid = session != null ? session.getAttribute("userId") : null;
        if (sessionUid == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please sign in to access the AI Photo Coach.");
            return "redirect:/login";
        }

        model.addAttribute("activeTab", "coach");
        return "photo-coach";
    }
}
