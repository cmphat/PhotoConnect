package com.photoconnect.security;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.ChatAccessDeniedException;
import com.photoconnect.repository.UserRepository;
import com.photoconnect.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StompSecurityChannelInterceptorTest {

    private JwtTokenService jwtTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatService chatService;

    @Mock
    private MessageChannel messageChannel;

    private StompSecurityChannelInterceptor interceptor;

    private static final String TEST_SECRET = "dGhpcy1pcy1hLXZlcnktc2VjdXJlLWtleS13aXRoLW1vcmUtdGhhbi0zMi1ieXRlcy1mb3ItdGVzdGluZw==";

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService(TEST_SECRET, java.time.Duration.ofHours(1));
        interceptor = new StompSecurityChannelInterceptor(jwtTokenService, userRepository, chatService);
    }

    private Message<?> createStompMessage(StompCommand command, Map<String, Object> sessionAttributes,
                                          String destination, String authHeader) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setLeaveMutable(true);
        if (sessionAttributes != null) {
            accessor.setSessionAttributes(sessionAttributes);
        }
        if (destination != null) {
            accessor.setDestination(destination);
        }
        if (authHeader != null) {
            accessor.setNativeHeader("Authorization", authHeader);
        }
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    @DisplayName("CONNECT: Anonymous connection without session or token throws AccessDeniedException")
    void connect_anonymous_throwsAccessDenied() {
        Message<?> message = createStompMessage(StompCommand.CONNECT, new HashMap<>(), null, null);

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Unauthenticated STOMP connection");
    }

    @Test
    @DisplayName("CONNECT: Valid session attributes authenticates user and sets StompPrincipal")
    void connect_withValidSession_succeeds() {
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", 42L);
        sessionAttrs.put("userRole", UserRole.CUSTOMER);

        User activeUser = new User();
        activeUser.setId(42L);
        activeUser.setStatus(UserStatus.ACTIVE);
        activeUser.setRole(UserRole.CUSTOMER);
        when(userRepository.findById(42L)).thenReturn(Optional.of(activeUser));

        Message<?> message = createStompMessage(StompCommand.CONNECT, sessionAttrs, null, null);
        Message<?> result = interceptor.preSend(message, messageChannel);

        assertThat(result).isNotNull();
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
        assertThat(accessor.getUser()).isInstanceOf(StompPrincipal.class);
        StompPrincipal principal = (StompPrincipal) accessor.getUser();
        assertThat(principal.getUserId()).isEqualTo(42L);
        assertThat(principal.getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    @DisplayName("CONNECT: Inactive or suspended user throws AccessDeniedException")
    void connect_withInactiveUser_throwsAccessDenied() {
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", 42L);
        sessionAttrs.put("userRole", UserRole.CUSTOMER);

        User inactiveUser = new User();
        inactiveUser.setId(42L);
        inactiveUser.setStatus(UserStatus.INACTIVE);
        inactiveUser.setRole(UserRole.CUSTOMER);
        when(userRepository.findById(42L)).thenReturn(Optional.of(inactiveUser));

        Message<?> message = createStompMessage(StompCommand.CONNECT, sessionAttrs, null, null);

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("inactive or not found");
    }

    @Test
    @DisplayName("CONNECT: Valid JWT token header authenticates when session is missing")
    void connect_withJwtToken_succeeds() {
        User user = new User();
        user.setId(88L);
        user.setRole(UserRole.PHOTOGRAPHER);
        user.setStatus(UserStatus.ACTIVE);
        String token = jwtTokenService.createToken(user);

        when(userRepository.findById(88L)).thenReturn(Optional.of(user));

        Message<?> message = createStompMessage(StompCommand.CONNECT, new HashMap<>(), null, "Bearer " + token);
        Message<?> result = interceptor.preSend(message, messageChannel);

        assertThat(result).isNotNull();
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
        assertThat(accessor.getUser()).isInstanceOf(StompPrincipal.class);
        StompPrincipal principal = (StompPrincipal) accessor.getUser();
        assertThat(principal.getUserId()).isEqualTo(88L);
        assertThat(principal.getRole()).isEqualTo(UserRole.PHOTOGRAPHER);
    }

    @Test
    @DisplayName("SUBSCRIBE: User subscribing to their own notifications destination is permitted")
    void subscribe_ownNotification_succeeds() {
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", 15L);
        sessionAttrs.put("userRole", UserRole.CUSTOMER);

        Message<?> message = createStompMessage(StompCommand.SUBSCRIBE, sessionAttrs,
                "/topic/user/15/notifications", null);

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("SUBSCRIBE: User subscribing to another user's notifications is rejected with AccessDeniedException")
    void subscribe_otherUserNotification_throwsAccessDenied() {
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", 15L);
        sessionAttrs.put("userRole", UserRole.CUSTOMER);

        Message<?> message = createStompMessage(StompCommand.SUBSCRIBE, sessionAttrs,
                "/topic/user/99/notifications", null);

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized to subscribe to another user's notifications");
    }

    @Test
    @DisplayName("SUBSCRIBE: Admin subscribing to any user's notifications is permitted")
    void subscribe_adminNotification_succeeds() {
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", 1L);
        sessionAttrs.put("userRole", UserRole.ADMIN);

        Message<?> message = createStompMessage(StompCommand.SUBSCRIBE, sessionAttrs,
                "/topic/user/99/notifications", null);

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("SUBSCRIBE: Authorized booking participant can subscribe to booking chat")
    void subscribe_bookingChat_authorizedParticipant_succeeds() {
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", 20L);
        sessionAttrs.put("userRole", UserRole.CUSTOMER);

        Booking booking = new Booking();
        booking.setId(500L);
        when(chatService.getBookingForParticipant(500L, 20L)).thenReturn(booking);

        Message<?> message = createStompMessage(StompCommand.SUBSCRIBE, sessionAttrs,
                "/topic/booking/500/chat", null);

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertThat(result).isNotNull();
        verify(chatService).getBookingForParticipant(500L, 20L);
    }

    @Test
    @DisplayName("SUBSCRIBE: Non-participant subscribing to booking chat is rejected with AccessDeniedException")
    void subscribe_bookingChat_nonParticipant_throwsAccessDenied() {
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", 999L);
        sessionAttrs.put("userRole", UserRole.CUSTOMER);

        when(chatService.getBookingForParticipant(500L, 999L))
                .thenThrow(new ChatAccessDeniedException("User not authorized"));

        Message<?> message = createStompMessage(StompCommand.SUBSCRIBE, sessionAttrs,
                "/topic/booking/500/chat", null);

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized to subscribe to this booking chat");
    }

    @Test
    @DisplayName("SUBSCRIBE: Admin can subscribe to any booking chat without participant check")
    void subscribe_bookingChat_admin_succeeds() {
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", 1L);
        sessionAttrs.put("userRole", UserRole.ADMIN);

        Message<?> message = createStompMessage(StompCommand.SUBSCRIBE, sessionAttrs,
                "/topic/booking/500/chat", null);

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("SUBSCRIBE: Arbitrary unauthorized topic throws AccessDeniedException")
    void subscribe_arbitraryTopic_throwsAccessDenied() {
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", 15L);
        sessionAttrs.put("userRole", UserRole.CUSTOMER);

        Message<?> message = createStompMessage(StompCommand.SUBSCRIBE, sessionAttrs,
                "/topic/confidential-internal-stream", null);

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("unauthorized subscription destination");
    }

    @Test
    @DisplayName("SEND: Unauthenticated send message throws AccessDeniedException")
    void send_unauthenticated_throwsAccessDenied() {
        Message<?> message = createStompMessage(StompCommand.SEND, new HashMap<>(), "/app/chat", null);

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Unauthenticated STOMP message rejected");
    }

    @Test
    @DisplayName("SEND: Authenticated user can send message")
    void send_authenticated_succeeds() {
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", 15L);
        sessionAttrs.put("userRole", UserRole.CUSTOMER);

        Message<?> message = createStompMessage(StompCommand.SEND, sessionAttrs, "/app/chat", null);
        Message<?> result = interceptor.preSend(message, messageChannel);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("SUBSCRIBE: When ChatService is null, non-admin subscription to booking chat fails closed")
    void subscribe_chatServiceNull_failsClosed() {
        StompSecurityChannelInterceptor nullChatInterceptor =
                new StompSecurityChannelInterceptor(jwtTokenService, userRepository, null);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", 15L);
        sessionAttrs.put("userRole", UserRole.CUSTOMER);

        Message<?> message = createStompMessage(StompCommand.SUBSCRIBE, sessionAttrs,
                "/topic/booking/500/chat", null);

        assertThatThrownBy(() -> nullChatInterceptor.preSend(message, messageChannel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chat service is unavailable");
    }
}
