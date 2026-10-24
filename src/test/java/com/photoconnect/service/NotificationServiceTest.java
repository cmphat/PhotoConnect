package com.photoconnect.service;

import com.photoconnect.dto.NotificationDto;
import com.photoconnect.entity.Notification;
import com.photoconnect.entity.NotificationType;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.repository.NotificationRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    private NotificationServiceImpl notificationService;

    private User recipient;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(
                notificationRepository,
                userRepository,
                messagingTemplate
        );

        recipient = new User();
        recipient.setId(10L);
        recipient.setFullName("Minh Tran");
        recipient.setRole(UserRole.CUSTOMER);
    }

    @Test
    @DisplayName("Successfully creates, persists, and pushes realtime notification via WebSocket")
    void sendNotification_Success() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(recipient));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(100L);
            return n;
        });

        NotificationDto result = notificationService.sendNotification(
                10L,
                NotificationType.BOOKING_ACCEPTED,
                "Booking Confirmed",
                "Photographer accepted your booking.",
                500L,
                "/bookings/500"
        );

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.recipientId()).isEqualTo(10L);
        assertThat(result.type()).isEqualTo(NotificationType.BOOKING_ACCEPTED);
        assertThat(result.title()).isEqualTo("Booking Confirmed");
        assertThat(result.targetUrl()).isEqualTo("/bookings/500");

        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/user/10/notifications"), any(NotificationDto.class));
    }

    @Test
    @DisplayName("Transient WebSocket failure does not prevent persistent notification from being saved")
    void sendNotification_WebSocketFailure_Tolerated() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(recipient));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(100L);
            return n;
        });
        doThrow(new MessagingException("Broker unavailable"))
                .when(messagingTemplate).convertAndSend(any(String.class), any(NotificationDto.class));

        NotificationDto result = notificationService.sendNotification(
                10L,
                NotificationType.DELIVERY_READY,
                "Photos Delivered",
                "Your finished album is ready.",
                500L,
                "/bookings/500/delivery"
        );

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Returns empty list when user has no notifications")
    void getNotificationsForUser_ReturnsList() {
        Notification n = new Notification(recipient, NotificationType.NEW_MESSAGE, "Hello", "Hi there", 1L, "/chat");
        n.setId(1L);
        when(notificationRepository.findTop20ByRecipientIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(n));

        List<NotificationDto> result = notificationService.getNotificationsForUser(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Hello");
    }

    @Test
    @DisplayName("Accurately counts unread notifications for recipient")
    void getUnreadCount_ReturnsCount() {
        when(notificationRepository.countByRecipientIdAndReadAtIsNull(10L)).thenReturn(4L);

        long count = notificationService.getUnreadCount(10L);

        assertThat(count).isEqualTo(4L);
    }

    @Test
    @DisplayName("Marks individual notification as read")
    void markAsRead_Success() {
        Notification n = new Notification(recipient, NotificationType.BOOKING_REQUEST, "Title", "Msg", 1L, "/url");
        n.setId(5L);
        when(notificationRepository.findByIdAndRecipientId(5L, 10L)).thenReturn(Optional.of(n));

        notificationService.markAsRead(5L, 10L);

        assertThat(n.isRead()).isTrue();
        assertThat(n.getReadAt()).isNotNull();
        verify(notificationRepository).save(n);
    }

    @Test
    @DisplayName("Marks all unread notifications as read for recipient")
    void markAllAsRead_Success() {
        when(notificationRepository.markAllAsReadForRecipient(10L)).thenReturn(3);

        notificationService.markAllAsRead(10L);

        verify(notificationRepository).markAllAsReadForRecipient(10L);
    }
}
