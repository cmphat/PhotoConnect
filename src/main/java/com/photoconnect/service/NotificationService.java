package com.photoconnect.service;

import com.photoconnect.dto.NotificationDto;
import com.photoconnect.entity.NotificationType;

import java.util.List;

public interface NotificationService {

    /**
     * Persists a notification record and pushes a realtime message to the recipient's private WebSocket channel.
     */
    NotificationDto sendNotification(Long recipientUserId, NotificationType type, String title, String message, Long referenceId, String targetUrl);

    /**
     * Retrieves recent notifications for an authenticated user.
     */
    List<NotificationDto> getNotificationsForUser(Long userId);

    /**
     * Counts unread notifications for an authenticated user.
     */
    long getUnreadCount(Long userId);

    /**
     * Marks an individual notification as read, enforcing recipient ownership.
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * Marks all unread notifications for the authenticated user as read.
     */
    void markAllAsRead(Long userId);
}
