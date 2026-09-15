package com.photoconnect.controller;

import com.photoconnect.dto.SendMessageRequest;
import com.photoconnect.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class WebSocketChatControllerTest {

    @Test
    void malformedSessionUserId_isRejectedWithoutClassCastOrServiceCall() {
        ChatService chatService = mock(ChatService.class);
        WebSocketChatController controller = new WebSocketChatController(chatService);
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create();
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", "10");
        headers.setSessionAttributes(sessionAttributes);

        controller.handleChatMessage(new SendMessageRequest(100L, "Hello"), headers);

        verifyNoInteractions(chatService);
    }
}
