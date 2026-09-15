package com.photoconnect.controller;

import com.photoconnect.dto.SendMessageRequest;
import com.photoconnect.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebSocketChatController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketChatController.class);

    private final ChatService chatService;

    public WebSocketChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload SendMessageRequest request,
                                  SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes == null) {
            log.warn("STOMP message received with no session attributes");
            return;
        }

        Object userIdObj = sessionAttributes.get("userId");
        if (!(userIdObj instanceof Long senderUserId)) {
            log.warn("STOMP message received with no userId in session attributes");
            return;
        }
        if (request == null || request.getBookingId() == null || request.getContent() == null) {
            log.warn("STOMP message payload is invalid");
            return;
        }

        try {
            chatService.sendMessage(request.getBookingId(), senderUserId, request.getContent());
        } catch (Exception e) {
            log.error("Failed to process STOMP chat message: {}", e.getMessage());
        }
    }
}
