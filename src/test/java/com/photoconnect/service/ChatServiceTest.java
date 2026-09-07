package com.photoconnect.service;

import com.photoconnect.dto.ChatMessageDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.Message;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.ChatAccessDeniedException;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @InjectMocks
    private ChatServiceImpl chatService;

    private User customer;
    private User photographerUser;
    private PhotographerProfile photographerProfile;
    private Booking booking;

    @BeforeEach
    void setUp() {
        customer = new User("customer@example.com", "hashed_pwd", "Customer Name", "0900000001", UserRole.CUSTOMER, UserStatus.ACTIVE);
        customer.setId(10L);

        photographerUser = new User("photo@example.com", "hashed_pwd", "Photographer Name", "0900000002", UserRole.PHOTOGRAPHER, UserStatus.ACTIVE);
        photographerUser.setId(20L);

        photographerProfile = new PhotographerProfile();
        photographerProfile.setId(5L);
        photographerProfile.setUser(photographerUser);
        photographerProfile.setDisplayName("Studio Lens");

        booking = new Booking();
        booking.setId(100L);
        booking.setCustomer(customer);
        booking.setPhotographerProfile(photographerProfile);
        booking.setBookingDate(LocalDate.now().plusDays(2));
        booking.setBookingTime(LocalTime.of(10, 0));
        booking.setLocation("District 1, HCMC");
        booking.setAgreedPrice(new BigDecimal("2000000.00"));
        booking.setStatus(BookingStatus.ACCEPTED);
    }

    @Test
    void getMessageHistory_validCustomer_shouldMarkReadAndReturnList() {
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));

        Message msg1 = new Message(booking, customer, photographerUser, "Hello!");
        msg1.setId(1L);
        msg1.setSentAt(LocalDateTime.now().minusMinutes(5));

        when(messageRepository.findByBookingIdWithDetails(100L)).thenReturn(List.of(msg1));

        List<ChatMessageDto> history = chatService.getMessageHistory(100L, 10L);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getContent()).isEqualTo("Hello!");
        assertThat(history.get(0).getSenderName()).isEqualTo("Customer Name");
        verify(messageRepository).markMessagesAsRead(100L, 10L);
    }

    @Test
    void getMessageHistory_validPhotographer_shouldMarkReadAndReturnList() {
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));

        Message msg1 = new Message(booking, customer, photographerUser, "Hello!");
        msg1.setId(1L);
        msg1.setSentAt(LocalDateTime.now().minusMinutes(5));

        when(messageRepository.findByBookingIdWithDetails(100L)).thenReturn(List.of(msg1));

        List<ChatMessageDto> history = chatService.getMessageHistory(100L, 20L);

        assertThat(history).hasSize(1);
        verify(messageRepository).markMessagesAsRead(100L, 20L);
    }

    @Test
    void getMessageHistory_unauthorizedUser_shouldThrowChatAccessDeniedException() {
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> chatService.getMessageHistory(100L, 999L))
                .isInstanceOf(ChatAccessDeniedException.class)
                .hasMessageContaining("not authorized");

        verify(messageRepository, never()).markMessagesAsRead(any(), any());
    }

    @Test
    void getMessageHistory_bookingNotFound_shouldThrowInvalidBookingException() {
        when(bookingRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getMessageHistory(999L, 10L))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    void sendMessage_customerSender_shouldSetReceiverAsPhotographerAndBroadcast() {
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message m = invocation.getArgument(0);
            m.setId(50L);
            return m;
        });

        ChatMessageDto result = chatService.sendMessage(100L, 10L, "Hello, can we meet at 9 AM instead?");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(50L);
        assertThat(result.getContent()).isEqualTo("Hello, can we meet at 9 AM instead?");
        assertThat(result.getSenderId()).isEqualTo(10L);
        assertThat(result.getReceiverId()).isEqualTo(20L);

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(messageCaptor.capture());
        Message captured = messageCaptor.getValue();
        assertThat(captured.getSender().getId()).isEqualTo(10L);
        assertThat(captured.getReceiver().getId()).isEqualTo(20L);
        assertThat(captured.getContent()).isEqualTo("Hello, can we meet at 9 AM instead?");

        verify(messagingTemplate).convertAndSend(eq("/topic/booking/100/chat"), any(ChatMessageDto.class));
    }

    @Test
    void sendMessage_photographerSender_shouldSetReceiverAsCustomerAndBroadcast() {
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message m = invocation.getArgument(0);
            m.setId(51L);
            return m;
        });

        ChatMessageDto result = chatService.sendMessage(100L, 20L, "Sure, 9 AM works great for me.");

        assertThat(result).isNotNull();
        assertThat(result.getSenderId()).isEqualTo(20L);
        assertThat(result.getReceiverId()).isEqualTo(10L);

        verify(messagingTemplate).convertAndSend(eq("/topic/booking/100/chat"), any(ChatMessageDto.class));
    }

    @Test
    void sendMessage_unauthorizedSender_shouldThrowChatAccessDeniedException() {
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> chatService.sendMessage(100L, 999L, "Random intrusion"))
                .isInstanceOf(ChatAccessDeniedException.class)
                .hasMessageContaining("not authorized");

        verify(messageRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(any(String.class), any(Object.class));
    }

    @Test
    void sendMessage_blankContent_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> chatService.sendMessage(100L, 10L, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be empty");

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessage_contentTooLong_shouldThrowIllegalArgumentException() {
        String longContent = "A".repeat(2001);

        assertThatThrownBy(() -> chatService.sendMessage(100L, 10L, longContent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceed 2000");

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessage_bookingNotFound_shouldThrowInvalidBookingException() {
        when(bookingRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.sendMessage(999L, 10L, "Hello"))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    void getBookingForParticipant_unauthenticated_shouldThrowChatAccessDeniedException() {
        assertThatThrownBy(() -> chatService.getBookingForParticipant(100L, null))
                .isInstanceOf(ChatAccessDeniedException.class)
                .hasMessageContaining("not authenticated");
    }
}
