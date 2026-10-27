package com.photoconnect.controller;

import com.photoconnect.service.ManagerOperationsService;
import com.photoconnect.util.AdminSecurityUtils;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping({"/admin/audit", "/admin/audit-logs"})
public class AdminAuditController {
    private final ManagerOperationsService operations;

    public AdminAuditController(ManagerOperationsService operations) { this.operations = operations; }

    @GetMapping
    public String audit(HttpSession session, Model model) {
        String denied = AdminSecurityUtils.requireAdmin(session); if (denied != null) return denied;
        model.addAttribute("logs", operations.recentActivity(SessionSecurityUtils.userId(session), 200));
        model.addAttribute("activeTab", "audit");
        return "admin-audit";
    }
}
