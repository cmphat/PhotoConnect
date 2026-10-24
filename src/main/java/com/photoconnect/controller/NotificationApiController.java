package com.photoconnect.controller;

import com.photoconnect.dto.NotificationDto;
import com.photoconnect.service.NotificationService;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationApiController {

    private final NotificationService notificationService;

    public NotificationApiController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(HttpSession session) {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return ResponseEntity.ok(Map.of("unreadCount", 0, "notifications", Collections.emptyList()));
        }

        List<NotificationDto> list = notificationService.getNotificationsForUser(userId);
        long unread = notificationService.getUnreadCount(userId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("unreadCount", unread);
        resp.put("notifications", list);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long id, HttpSession session) {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId != null) {
            notificationService.markAsRead(id, userId);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(HttpSession session) {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId != null) {
            notificationService.markAllAsRead(userId);
        }
        return ResponseEntity.ok().build();
    }
}
