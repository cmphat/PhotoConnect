package com.photoconnect.service;

import com.photoconnect.dto.ChatMessageDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.Message;
import com.photoconnect.entity.User;
import com.photoconnect.exception.ChatAccessDeniedException;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.MessageRepository;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    public static final int MAX_CONTENT_LENGTH = 2000;

    private final MessageRepository messageRepository;
    private final BookingRepository bookingRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    public ChatServiceImpl(MessageRepository messageRepository,
                           BookingRepository bookingRepository,
                           SimpMessageSendingOperations messagingTemplate) {
        this.messageRepository = messageRepository;
        this.bookingRepository = bookingRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public List<ChatMessageDto> getMessageHistory(Long bookingId, Long userId) {
        Booking booking = getBookingForParticipant(bookingId, userId);

        // Mark any unread messages addressed to the current user as read
        messageRepository.markMessagesAsRead(booking.getId(), userId);

        List<Message> messages = messageRepository.findByBookingIdWithDetails(bookingId);
        return messages.stream()
                .map(ChatMessageDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatMessageDto sendMessage(Long bookingId, Long senderUserId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty.");
        }

        String trimmedContent = content.trim();
        if (trimmedContent.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Message cannot exceed " + MAX_CONTENT_LENGTH + " characters.");
        }

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking not found."));

        User customer = booking.getCustomer();
        User photographerUser = booking.getPhotographerProfile() != null
                ? booking.getPhotographerProfile().getUser()
                : null;

        if (customer == null || photographerUser == null) {
            throw new InvalidBookingException("Booking has incomplete customer or photographer details.");
        }

        User sender;
        User receiver;

        if (customer.getId().equals(senderUserId)) {
            sender = customer;
            receiver = photographerUser;
        } else if (photographerUser.getId().equals(senderUserId)) {
            sender = photographerUser;
            receiver = customer;
        } else {
            throw new ChatAccessDeniedException("You are not authorized to send messages for this booking.");
        }

        Message message = new Message(booking, sender, receiver, trimmedContent);
        Message saved = messageRepository.save(message);

        ChatMessageDto dto = ChatMessageDto.fromEntity(saved);

        // Broadcast to WebSocket subscribers for this booking
        try {
            messagingTemplate.convertAndSend("/topic/booking/" + bookingId + "/chat", dto);
        } catch (Exception e) {
            // Log warning but do not fail the persistent transaction
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingForParticipant(Long bookingId, Long userId) {
        if (userId == null) {
            throw new ChatAccessDeniedException("User is not authenticated.");
        }

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking not found."));

        Long customerUserId = booking.getCustomer() != null ? booking.getCustomer().getId() : null;
        Long photographerUserId = (booking.getPhotographerProfile() != null && booking.getPhotographerProfile().getUser() != null)
                ? booking.getPhotographerProfile().getUser().getId()
                : null;

        if (!userId.equals(customerUserId) && !userId.equals(photographerUserId)) {
            throw new ChatAccessDeniedException("You are not authorized to access chat for this booking.");
        }

        return booking;
    }
}
