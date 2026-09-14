package com.photoconnect.controller;

import com.photoconnect.dto.AdminDashboardStatsDto;
import com.photoconnect.service.AdminDashboardService;
import com.photoconnect.util.AdminSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping
    public String adminRoot(HttpSession session) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            return redirect;
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        AdminDashboardStatsDto stats = adminDashboardService.getDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("activeTab", "dashboard");
        return "admin-dashboard";
    }
}
