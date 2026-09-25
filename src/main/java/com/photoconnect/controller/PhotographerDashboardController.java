package com.photoconnect.controller;

import com.photoconnect.entity.UserRole;
import com.photoconnect.exception.PhotographerStudioProfileNotFoundException;
import com.photoconnect.service.PhotographerDashboardService;
import com.photoconnect.security.AuthenticatedUser;
import com.photoconnect.security.CurrentUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class PhotographerDashboardController {

    private final PhotographerDashboardService photographerDashboardService;
    private final CurrentUserContext currentUserContext;

    public PhotographerDashboardController(PhotographerDashboardService photographerDashboardService,
                                           CurrentUserContext currentUserContext) {
        this.photographerDashboardService = photographerDashboardService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping("/photographer/dashboard")
    public String dashboard(HttpServletRequest request, HttpServletResponse response, Model model)
            throws IOException {
        AuthenticatedUser currentUser = currentUserContext.current(request).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (currentUser.role() != UserRole.PHOTOGRAPHER) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Only photographers can access Studio.");
            return null;
        }

        try {
            model.addAttribute("dashboard",
                    photographerDashboardService.getDashboard(currentUser.userId()));
            return "photographer-dashboard";
        } catch (PhotographerStudioProfileNotFoundException ex) {
            return "redirect:/photographer/onboarding-status";
        }
    }
}
