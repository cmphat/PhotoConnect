package com.photoconnect.controller;

import com.photoconnect.dto.ChatMessageDto;
import com.photoconnect.dto.SendMessageRequest;
import com.photoconnect.exception.ChatAccessDeniedException;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.service.ChatService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings/{bookingId}/messages")
public class ChatApiController {

    private final ChatService chatService;

    public ChatApiController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<?> getMessages(@PathVariable("bookingId") Long bookingId,
                                         HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Authentication required"));
        }

        Long userId = (Long) userIdObj;
        List<ChatMessageDto> messages = chatService.getMessageHistory(bookingId, userId);
        return ResponseEntity.ok(Map.of("success", true, "data", messages));
    }

    @PostMapping
    public ResponseEntity<?> sendMessage(@PathVariable("bookingId") Long bookingId,
                                         @Valid @RequestBody SendMessageRequest request,
                                         HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Authentication required"));
        }

        Long userId = (Long) userIdObj;
        ChatMessageDto created = chatService.sendMessage(bookingId, userId, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "data", created));
    }

    @ExceptionHandler(ChatAccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(ChatAccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("success", false, "errorCode", "CHAT_002_ACCESS_DENIED", "message", e.getMessage()));
    }

    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity<?> handleInvalidBooking(InvalidBookingException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "errorCode", "BOOKING_001_NOT_FOUND", "message", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "errorCode", "SYSTEM_003_VALIDATION_ERROR", "message", e.getMessage()));
    }
}
