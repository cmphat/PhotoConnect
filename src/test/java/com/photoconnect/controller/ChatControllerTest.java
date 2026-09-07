package com.photoconnect.controller;

import com.photoconnect.dto.ChatMessageDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.ChatAccessDeniedException;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    private Booking booking;
    private User customer;
    private User photographerUser;
    private PhotographerProfile photographerProfile;

    @BeforeEach
    void setUp() {
        customer = new User("customer@example.com", "hash", "Customer Doe", "0900000001", UserRole.CUSTOMER, UserStatus.ACTIVE);
        customer.setId(10L);

        photographerUser = new User("photo@example.com", "hash", "Photographer Pro", "0900000002", UserRole.PHOTOGRAPHER, UserStatus.ACTIVE);
        photographerUser.setId(20L);

        photographerProfile = new PhotographerProfile();
        photographerProfile.setId(5L);
        photographerProfile.setUser(photographerUser);
        photographerProfile.setDisplayName("Golden Hour Studios");

        booking = new Booking();
        booking.setId(100L);
        booking.setCustomer(customer);
        booking.setPhotographerProfile(photographerProfile);
        booking.setBookingDate(LocalDate.now().plusDays(2));
        booking.setBookingTime(LocalTime.of(14, 0));
        booking.setLocation("District 1, HCMC");
        booking.setAgreedPrice(new BigDecimal("1500000.00"));
        booking.setStatus(BookingStatus.ACCEPTED);
    }

    @Test
    void viewChat_unauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/bookings/100/chat"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void viewChat_authorizedCustomer_shouldRenderChatViewWithModel() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 10L);
        session.setAttribute("userRole", "CUSTOMER");

        ChatMessageDto msg = new ChatMessageDto(1L, 100L, 20L, "Golden Hour Studios", 10L, "Hello!", false, LocalDateTime.now());
        when(chatService.getBookingForParticipant(100L, 10L)).thenReturn(booking);
        when(chatService.getMessageHistory(100L, 10L)).thenReturn(List.of(msg));

        mockMvc.perform(get("/bookings/100/chat").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("chat"))
                .andExpect(model().attributeExists("booking", "messages", "currentUserId", "partnerName", "partnerRole", "backUrl"))
                .andExpect(model().attribute("partnerName", "Golden Hour Studios"))
                .andExpect(model().attribute("partnerRole", "Photographer"))
                .andExpect(model().attribute("backUrl", "/bookings/100"));
    }

    @Test
    void viewChat_authorizedPhotographer_shouldRenderChatViewWithModel() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 20L);
        session.setAttribute("userRole", "PHOTOGRAPHER");

        ChatMessageDto msg = new ChatMessageDto(1L, 100L, 10L, "Customer Doe", 20L, "Looking forward!", false, LocalDateTime.now());
        when(chatService.getBookingForParticipant(100L, 20L)).thenReturn(booking);
        when(chatService.getMessageHistory(100L, 20L)).thenReturn(List.of(msg));

        mockMvc.perform(get("/bookings/100/chat").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("chat"))
                .andExpect(model().attributeExists("booking", "messages", "currentUserId", "partnerName", "partnerRole", "backUrl"))
                .andExpect(model().attribute("partnerName", "Customer Doe"))
                .andExpect(model().attribute("partnerRole", "Customer"))
                .andExpect(model().attribute("backUrl", "/photographer/bookings/100"));
    }

    @Test
    void viewChat_unauthorizedUser_shouldRedirectToBookingsWithError() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 999L);
        session.setAttribute("userRole", "CUSTOMER");

        when(chatService.getBookingForParticipant(100L, 999L))
                .thenThrow(new ChatAccessDeniedException("You are not authorized to access chat for this booking."));

        mockMvc.perform(get("/bookings/100/chat").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void viewChat_invalidBooking_shouldRedirectToBookingsWithError() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 10L);
        session.setAttribute("userRole", "CUSTOMER");

        when(chatService.getBookingForParticipant(999L, 10L))
                .thenThrow(new InvalidBookingException("Booking not found."));

        mockMvc.perform(get("/bookings/999/chat").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}
