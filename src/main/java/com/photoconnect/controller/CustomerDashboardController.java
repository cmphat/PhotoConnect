package com.photoconnect.controller;

import com.photoconnect.entity.UserRole;
import com.photoconnect.service.CustomerDashboardService;
import com.photoconnect.security.AuthenticatedUser;
import com.photoconnect.security.CurrentUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class CustomerDashboardController {

    private final CustomerDashboardService customerDashboardService;
    private final CurrentUserContext currentUserContext;

    public CustomerDashboardController(CustomerDashboardService customerDashboardService,
                                       CurrentUserContext currentUserContext) {
        this.customerDashboardService = customerDashboardService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping("/customer/dashboard")
    public String dashboard(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        AuthenticatedUser currentUser = currentUserContext.current(request).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (currentUser.role() != UserRole.CUSTOMER) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Only customers can access the customer workspace.");
            return null;
        }

        model.addAttribute("dashboard", customerDashboardService.getDashboard(currentUser.userId()));
        return "customer-dashboard";
    }
}
