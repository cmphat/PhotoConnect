package com.photoconnect.controller;

import com.photoconnect.dto.NotificationDto;
import com.photoconnect.service.NotificationService;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String viewNotificationCenter(HttpSession session, Model model) {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        List<NotificationDto> notifications = notificationService.getNotificationsForUser(userId);
        Object roleObj = session.getAttribute("userRole");
        if (roleObj != null && "MANAGER".equalsIgnoreCase(roleObj.toString())) {
            notifications = notifications.stream()
                    .filter(n -> n.type() != null && !n.type().name().startsWith("BOOKING_")
                            && !n.type().name().startsWith("DELIVERY_")
                            && !n.type().name().startsWith("DEPOSIT_"))
                    .toList();
        }
        long unreadCount = notifications.stream().filter(n -> !n.read()).count();

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        return "notifications";
    }

    @PostMapping("/{id}/read")
    public String markNotificationRead(@PathVariable Long id,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            notificationService.markAsRead(id, userId);
        } catch (Exception e) {
            // ignore
        }
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String markAllNotificationsRead(HttpSession session,
                                           RedirectAttributes redirectAttributes) {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        notificationService.markAllAsRead(userId);
        redirectAttributes.addFlashAttribute("successMessage", "All notifications marked as read.");
        return "redirect:/notifications";
    }
}
