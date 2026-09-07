package com.photoconnect.service;

import com.photoconnect.dto.ChatMessageDto;
import com.photoconnect.entity.Booking;

import java.util.List;

public interface ChatService {

    List<ChatMessageDto> getMessageHistory(Long bookingId, Long userId);

    ChatMessageDto sendMessage(Long bookingId, Long senderUserId, String content);

    Booking getBookingForParticipant(Long bookingId, Long userId);
}
