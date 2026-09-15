package com.photoconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photoconnect.dto.ChatMessageDto;
import com.photoconnect.dto.SendMessageRequest;
import com.photoconnect.exception.ChatAccessDeniedException;
import com.photoconnect.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatApiController.class)
class ChatApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    @Test
    void getMessages_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/bookings/100/messages"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getMessages_malformedSessionUserId_shouldReturn401WithoutServiceCall() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "10");

        mockMvc.perform(get("/api/bookings/100/messages").session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(chatService);
    }

    @Test
    void getMessages_authorized_shouldReturnMessageList() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 10L);

        ChatMessageDto msg = new ChatMessageDto(1L, 100L, 10L, "Customer", 20L, "Hi", false, LocalDateTime.now());
        when(chatService.getMessageHistory(100L, 10L)).thenReturn(List.of(msg));

        mockMvc.perform(get("/api/bookings/100/messages").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].content").value("Hi"));
    }

    @Test
    void getMessages_unauthorizedUser_shouldReturn403() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 999L);

        when(chatService.getMessageHistory(100L, 999L))
                .thenThrow(new ChatAccessDeniedException("You are not authorized"));

        mockMvc.perform(get("/api/bookings/100/messages").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CHAT_002_ACCESS_DENIED"));
    }

    @Test
    void sendMessage_unauthenticated_shouldReturn401() throws Exception {
        SendMessageRequest request = new SendMessageRequest(100L, "Hello");

        mockMvc.perform(post("/api/bookings/100/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void sendMessage_validRequest_shouldReturn201Created() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 10L);

        SendMessageRequest request = new SendMessageRequest(100L, "Hello, can we shoot at sunset?");
        ChatMessageDto created = new ChatMessageDto(1L, 100L, 10L, "Customer", 20L, "Hello, can we shoot at sunset?", false, LocalDateTime.now());

        when(chatService.sendMessage(eq(100L), eq(10L), eq("Hello, can we shoot at sunset?")))
                .thenReturn(created);

        mockMvc.perform(post("/api/bookings/100/messages")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("Hello, can we shoot at sunset?"));
    }

    @Test
    void sendMessage_blankContent_shouldReturn400BadRequest() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 10L);

        SendMessageRequest request = new SendMessageRequest(100L, "   ");

        mockMvc.perform(post("/api/bookings/100/messages")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
