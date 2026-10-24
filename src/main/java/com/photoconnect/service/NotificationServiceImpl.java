package com.photoconnect.service;

import com.photoconnect.dto.NotificationDto;
import com.photoconnect.entity.Notification;
import com.photoconnect.entity.NotificationType;
import com.photoconnect.entity.User;
import com.photoconnect.exception.ResourceNotFoundException;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.NotificationRepository;
import com.photoconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository,
                                   SimpMessageSendingOperations messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public NotificationDto sendNotification(Long recipientUserId, NotificationType type, String title, String message, Long referenceId, String targetUrl) {
        if (recipientUserId == null) {
            log.warn("Cannot send notification without a valid recipient ID.");
            return null;
        }

        User recipient = userRepository.findById(recipientUserId)
                .orElse(null);
        if (recipient == null) {
            log.warn("Recipient user [{}] not found for notification: {}", recipientUserId, title);
            return null;
        }

        Notification notification = new Notification(recipient, type, title, message, referenceId, targetUrl);
        Notification saved = notificationRepository.save(notification);
        NotificationDto dto = NotificationDto.fromEntity(saved);

        // Realtime push to recipient's private topic
        try {
            messagingTemplate.convertAndSend("/topic/user/" + recipientUserId + "/notifications", dto);
        } catch (Exception e) {
            log.warn("Failed to push realtime notification over WebSocket to user [{}]: {}",
                    recipientUserId, e.getMessage());
        }

        log.info("Sent notification [{}] type [{}] to user [{}]", saved.getId(), type, recipientUserId);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsForUser(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return notificationRepository.findTop20ByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        return notificationRepository.countByRecipientIdAndReadAtIsNull(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        if (notificationId == null || userId == null) {
            return;
        }
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found or access denied: " + notificationId));

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        if (userId == null) {
            return;
        }
        notificationRepository.markAllAsReadForRecipient(userId);
    }
}
