package com.photoconnect.controller;

import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.service.AdminUserService;
import com.photoconnect.util.AdminSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public String listUsers(@RequestParam(value = "search", required = false) String search,
                            @RequestParam(value = "role", required = false) String roleStr,
                            @RequestParam(value = "status", required = false) String statusStr,
                            HttpSession session,
                            Model model) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        UserRole roleFilter = null;
        if (roleStr != null && !roleStr.isBlank() && !"ALL".equalsIgnoreCase(roleStr)) {
            try {
                roleFilter = UserRole.valueOf(roleStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        UserStatus statusFilter = null;
        if (statusStr != null && !statusStr.isBlank() && !"ALL".equalsIgnoreCase(statusStr)) {
            try {
                statusFilter = UserStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        List<User> users = adminUserService.searchUsers(search, roleFilter, statusFilter);

        model.addAttribute("users", users);
        model.addAttribute("search", search != null ? search.trim() : "");
        model.addAttribute("selectedRole", roleFilter != null ? roleFilter.name() : "ALL");
        model.addAttribute("selectedStatus", statusFilter != null ? statusFilter.name() : "ALL");
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("currentAdminId", session.getAttribute("userId"));
        model.addAttribute("activeTab", "users");

        return "admin-users";
    }

    @PostMapping("/{id}/status")
    public String updateUserStatus(@PathVariable Long id,
                                   @RequestParam("status") String statusStr,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        Long currentAdminId = (Long) session.getAttribute("userId");
        try {
            UserStatus newStatus = UserStatus.valueOf(statusStr.trim().toUpperCase());
            adminUserService.updateUserStatus(id, newStatus, currentAdminId);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated to " + newStatus.name() + ".");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid status value: " + statusStr);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users";
    }
}
