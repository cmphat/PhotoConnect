package com.photoconnect.controller;

import com.photoconnect.dto.ChatMessageDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.exception.ChatAccessDeniedException;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/bookings/{bookingId}/chat")
    public String viewChat(@PathVariable("bookingId") Long bookingId,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/login";
        }

        Long currentUserId = (Long) userIdObj;

        try {
            Booking booking = chatService.getBookingForParticipant(bookingId, currentUserId);
            List<ChatMessageDto> messages = chatService.getMessageHistory(bookingId, currentUserId);

            boolean isCustomer = booking.getCustomer() != null && booking.getCustomer().getId().equals(currentUserId);
            String partnerName;
            String partnerRole;
            String backUrl;

            if (isCustomer) {
                partnerName = booking.getPhotographerProfile() != null
                        ? booking.getPhotographerProfile().getDisplayName()
                        : "Photographer";
                partnerRole = "Photographer";
                backUrl = "/bookings/" + bookingId;
            } else {
                partnerName = booking.getCustomer() != null
                        ? booking.getCustomer().getFullName()
                        : "Customer";
                partnerRole = "Customer";
                backUrl = "/photographer/bookings/" + bookingId;
            }

            model.addAttribute("booking", booking);
            model.addAttribute("messages", messages);
            model.addAttribute("currentUserId", currentUserId);
            model.addAttribute("partnerName", partnerName);
            model.addAttribute("partnerRole", partnerRole);
            model.addAttribute("backUrl", backUrl);

            return "chat";
        } catch (ChatAccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/bookings";
        } catch (InvalidBookingException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/bookings";
        }
    }
}
