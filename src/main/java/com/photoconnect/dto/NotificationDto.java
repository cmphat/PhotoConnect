package com.photoconnect.dto;

import com.photoconnect.entity.Notification;
import com.photoconnect.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        Long recipientId,
        NotificationType type,
        String typeName,
        String iconClass,
        String badgeClass,
        String title,
        String message,
        Long referenceId,
        String targetUrl,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    public static NotificationDto fromEntity(Notification entity) {
        if (entity == null) {
            return null;
        }
        NotificationType t = entity.getType();
        return new NotificationDto(
                entity.getId(),
                entity.getRecipient() != null ? entity.getRecipient().getId() : null,
                t,
                t != null ? t.getDisplayName() : "Notification",
                t != null ? t.getIconClass() : "bi-bell",
                t != null ? t.getBadgeClass() : "pc-badge-secondary",
                entity.getTitle(),
                entity.getMessage(),
                entity.getReferenceId(),
                entity.getTargetUrl(),
                entity.isRead(),
                entity.getReadAt(),
                entity.getCreatedAt()
        );
    }
}
