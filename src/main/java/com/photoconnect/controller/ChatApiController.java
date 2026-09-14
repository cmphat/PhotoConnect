package com.photoconnect.controller;

import com.photoconnect.dto.ApiResponse;
import com.photoconnect.dto.ChatMessageDto;
import com.photoconnect.dto.SendMessageRequest;
import com.photoconnect.exception.ErrorCode;
import com.photoconnect.service.ChatService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings/{bookingId}/messages")
public class ChatApiController {

    private final ChatService chatService;

    public ChatApiController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatMessageDto>>> getMessages(@PathVariable("bookingId") Long bookingId,
                                                                         HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCode.AUTH_003_TOKEN_INVALID.getCode(), "Authentication required"));
        }

        Long userId = (Long) userIdObj;
        List<ChatMessageDto> messages = chatService.getMessageHistory(bookingId, userId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatMessageDto>> sendMessage(@PathVariable("bookingId") Long bookingId,
                                                                   @Valid @RequestBody SendMessageRequest request,
                                                                   HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCode.AUTH_003_TOKEN_INVALID.getCode(), "Authentication required"));
        }

        Long userId = (Long) userIdObj;
        ChatMessageDto created = chatService.sendMessage(bookingId, userId, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Message sent successfully"));
    }
}
