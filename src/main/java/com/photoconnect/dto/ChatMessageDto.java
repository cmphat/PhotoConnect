package com.photoconnect.dto;

import com.photoconnect.entity.Message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ChatMessageDto {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm", Locale.US);

    private Long id;
    private Long bookingId;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String content;
    private boolean isRead;
    private LocalDateTime sentAt;
    private String formattedSentAt;

    public ChatMessageDto() {
    }

    public ChatMessageDto(Long id, Long bookingId, Long senderId, String senderName, Long receiverId, String content, boolean isRead, LocalDateTime sentAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.content = content;
        this.isRead = isRead;
        this.sentAt = sentAt;
        this.formattedSentAt = sentAt != null ? sentAt.format(FORMATTER) : "";
    }

    public static ChatMessageDto fromEntity(Message message) {
        if (message == null) {
            return null;
        }
        Long bId = message.getBooking() != null ? message.getBooking().getId() : null;
        Long sId = message.getSender() != null ? message.getSender().getId() : null;
        String sName = message.getSender() != null ? message.getSender().getFullName() : "User";
        Long rId = message.getReceiver() != null ? message.getReceiver().getId() : null;

        return new ChatMessageDto(
                message.getId(),
                bId,
                sId,
                sName,
                rId,
                message.getContent(),
                message.isRead(),
                message.getSentAt()
        );
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
        this.formattedSentAt = sentAt != null ? sentAt.format(FORMATTER) : "";
    }

    public String getFormattedSentAt() {
        return formattedSentAt;
    }

    public void setFormattedSentAt(String formattedSentAt) {
        this.formattedSentAt = formattedSentAt;
    }
}
