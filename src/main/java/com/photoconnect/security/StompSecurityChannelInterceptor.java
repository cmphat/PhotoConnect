package com.photoconnect.security;

import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.ChatAccessDeniedException;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.repository.UserRepository;
import com.photoconnect.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Intercepts inbound STOMP messages to enforce authentication and destination-level authorization.
 * Resolves SEC-03:
 * - Authenticates STOMP CONNECT using session attributes or JWT token.
 * - Authorizes SUBSCRIBE destinations:
 *     * /topic/user/{id}/notifications: only accessible by user {id} or ADMIN.
 *     * /topic/booking/{id}/chat: only accessible by verified booking participants or ADMIN.
 * - Rejects unauthorized subscription or send attempts with AccessDeniedException.
 */
@Component
public class StompSecurityChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(StompSecurityChannelInterceptor.class);

    private static final Pattern USER_NOTIF_PATTERN = Pattern.compile("^/topic/user/(\\d+)/notifications/?$");
    private static final Pattern BOOKING_CHAT_PATTERN = Pattern.compile("^/topic/booking/(\\d+)(?:/chat)?/?$");

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final ChatService chatService;

    @Autowired
    public StompSecurityChannelInterceptor(@Autowired(required = false) JwtTokenService jwtTokenService,
                                           @Autowired(required = false) UserRepository userRepository,
                                           @org.springframework.context.annotation.Lazy @Autowired(required = false) ChatService chatService) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.chatService = chatService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (command == StompCommand.CONNECT) {
            handleConnect(accessor);
        } else if (command == StompCommand.SUBSCRIBE) {
            handleSubscribe(accessor);
        } else if (command == StompCommand.SEND) {
            handleSend(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        Long userId = null;
        UserRole userRole = null;

        // 1. Check HTTP Session attributes copied by HttpSessionHandshakeInterceptor
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null && sessionAttributes.get("userId") != null) {
            Object idObj = sessionAttributes.get("userId");
            if (idObj instanceof Long l) {
                userId = l;
            } else if (idObj instanceof Number n) {
                userId = n.longValue();
            } else if (idObj instanceof String s) {
                try { userId = Long.parseLong(s); } catch (NumberFormatException ignored) {}
            }

            Object roleObj = sessionAttributes.get("userRole");
            if (roleObj instanceof UserRole ur) {
                userRole = ur;
            } else if (roleObj instanceof String s) {
                try { userRole = UserRole.valueOf(s); } catch (IllegalArgumentException ignored) {}
            }
        }

        // 2. Fallback to STOMP native headers (Authorization: Bearer <jwt> or token: <jwt>)
        if (userId == null && jwtTokenService != null) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token == null) {
                token = accessor.getFirstNativeHeader("token");
            }
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            if (token != null && !token.isBlank()) {
                try {
                    JwtPrincipal principal = jwtTokenService.parseToken(token.trim());
                    if (principal != null) {
                        userId = principal.userId();
                        userRole = principal.role();
                    }
                } catch (Exception ex) {
                    log.warn("STOMP connect JWT validation failed: {}", ex.getMessage());
                }
            }
        }

        if (userId == null) {
            log.warn("Rejecting unauthenticated STOMP CONNECT frame");
            throw new AccessDeniedException("Unauthenticated STOMP connection: missing session or valid token.");
        }

        final Long effectiveUserId = userId;
        // 3. Verify user status in repository if available
        if (userRepository != null) {
            User user = userRepository.findById(effectiveUserId)
                    .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                    .orElseThrow(() -> new AccessDeniedException("User account is inactive or not found: " + effectiveUserId));
            userRole = user.getRole();
        }

        if (userRole == null) {
            userRole = UserRole.CUSTOMER;
        }

        StompPrincipal principal = new StompPrincipal(effectiveUserId, userRole);
        if (accessor.isMutable()) {
            accessor.setUser(principal);
        }

        if (sessionAttributes != null) {
            sessionAttributes.put("userId", userId);
            sessionAttributes.put("userRole", userRole);
        }

        log.debug("Authenticated STOMP connection for user [{}] with role [{}]", userId, userRole);
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        StompPrincipal principal = resolvePrincipal(accessor);
        if (principal == null) {
            log.warn("Rejecting unauthenticated STOMP SUBSCRIBE frame");
            throw new AccessDeniedException("Unauthenticated STOMP subscription rejected.");
        }

        String destination = accessor.getDestination();
        if (destination == null || destination.isBlank()) {
            throw new AccessDeniedException("Subscription destination cannot be empty.");
        }

        Long authenticatedUserId = principal.getUserId();
        UserRole authenticatedRole = principal.getRole();

        // Rule A: Notifications destination (/topic/user/{targetUserId}/notifications)
        Matcher notifMatcher = USER_NOTIF_PATTERN.matcher(destination);
        if (notifMatcher.matches()) {
            Long targetUserId = Long.parseLong(notifMatcher.group(1));
            if (!targetUserId.equals(authenticatedUserId) && authenticatedRole != UserRole.ADMIN) {
                log.warn("User [{}] attempted unauthorized subscription to user [{}] notifications",
                        authenticatedUserId, targetUserId);
                throw new AccessDeniedException("You are not authorized to subscribe to another user's notifications.");
            }
            return;
        }

        // Rule B: Booking Chat destination (/topic/booking/{bookingId}/chat)
        Matcher chatMatcher = BOOKING_CHAT_PATTERN.matcher(destination);
        if (chatMatcher.matches()) {
            if (authenticatedRole == UserRole.ADMIN) {
                return;
            }
            if (chatService == null) {
                log.error("ChatService is unavailable for STOMP authorization. Failing closed.");
                throw new AccessDeniedException("Access denied: chat service is unavailable to verify participant authorization.");
            }
            Long bookingId = Long.parseLong(chatMatcher.group(1));
            try {
                chatService.getBookingForParticipant(bookingId, authenticatedUserId);
            } catch (ChatAccessDeniedException | InvalidBookingException ex) {
                log.warn("User [{}] attempted unauthorized subscription to booking [{}] chat: {}",
                        authenticatedUserId, bookingId, ex.getMessage());
                throw new AccessDeniedException("You are not authorized to subscribe to this booking chat.");
            }
            return;
        }

        // Rule C: User personal queues (/user/queue/**)
        if (destination.startsWith("/user/")) {
            return;
        }

        // Rule D: Public destinations (/topic/public/**)
        if (destination.startsWith("/topic/public")) {
            return;
        }

        // Any other destination is rejected by default
        log.warn("User [{}] attempted subscription to unauthorized destination [{}]",
                authenticatedUserId, destination);
        throw new AccessDeniedException("Access denied: unauthorized subscription destination " + destination);
    }

    private void handleSend(StompHeaderAccessor accessor) {
        StompPrincipal principal = resolvePrincipal(accessor);
        if (principal == null) {
            log.warn("Rejecting unauthenticated STOMP SEND frame");
            throw new AccessDeniedException("Unauthenticated STOMP message rejected.");
        }
    }

    private StompPrincipal resolvePrincipal(StompHeaderAccessor accessor) {
        Principal p = accessor.getUser();
        if (p instanceof StompPrincipal sp) {
            return sp;
        }

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null && sessionAttributes.get("userId") != null) {
            Long userId = null;
            Object idObj = sessionAttributes.get("userId");
            if (idObj instanceof Long l) userId = l;
            else if (idObj instanceof Number n) userId = n.longValue();
            else if (idObj instanceof String s) {
                try { userId = Long.parseLong(s); } catch (NumberFormatException ignored) {}
            }

            UserRole role = UserRole.CUSTOMER;
            Object r = sessionAttributes.get("userRole");
            if (r instanceof UserRole ur) role = ur;
            else if (r instanceof String s) {
                try { role = UserRole.valueOf(s); } catch (IllegalArgumentException ignored) {}
            }

            if (userId != null) {
                StompPrincipal principal = new StompPrincipal(userId, role);
                if (accessor.isMutable()) {
                    accessor.setUser(principal);
                }
                return principal;
            }
        }

        return null;
    }
}
