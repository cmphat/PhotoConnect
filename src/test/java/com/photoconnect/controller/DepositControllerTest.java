package com.photoconnect.controller;

import com.photoconnect.dto.BookingViewDto;
import com.photoconnect.dto.DepositViewDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.entity.User;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.service.BookingService;
import com.photoconnect.service.DepositService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepositController.class)
public class DepositControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepositService depositService;

    @MockBean
    private BookingService bookingService;

    private Booking booking;
    private DepositViewDto deposit;

    @BeforeEach
    void setUp() {
        User customer = new User();
        customer.setId(10L);
        
        User photographer = new User();
        photographer.setFullName("Photographer Name");
        com.photoconnect.entity.PhotographerProfile profile = new com.photoconnect.entity.PhotographerProfile();
        profile.setUser(photographer);

        booking = new Booking();
        booking.setId(100L);
        booking.setCustomer(customer);
        booking.setPhotographerProfile(profile);
        booking.setAgreedPrice(new BigDecimal("2000.00"));
        
        deposit = new DepositViewDto();
        deposit.setId(1L);
        deposit.setAmount(new BigDecimal("600.00"));
        deposit.setStatus(DepositStatus.PENDING);
    }

    @Test
    void showDepositPage_guest_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/bookings/100/deposit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void showDepositPage_customer_showsDeposit() throws Exception {
        when(bookingService.getBookingForCustomer(anyLong(), anyLong())).thenReturn(booking);
        when(depositService.getOrCreateDepositForBooking(anyLong(), anyLong())).thenReturn(deposit);

        mockMvc.perform(get("/bookings/100/deposit").sessionAttr("userId", 10L))
                .andExpect(status().isOk())
                .andExpect(view().name("deposit"))
                .andExpect(model().attributeExists("booking", "deposit"));
    }

    @Test
    void showDepositPage_invalidBooking_redirectsToBookings() throws Exception {
        when(bookingService.getBookingForCustomer(anyLong(), anyLong())).thenThrow(new InvalidBookingException("Invalid"));

        mockMvc.perform(get("/bookings/100/deposit").sessionAttr("userId", 10L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/100"));
    }

    @Test
    void simulatePayment_customer_simulatesAndRedirects() throws Exception {
        when(depositService.simulateSuccessfulPayment(anyLong(), anyLong())).thenReturn(deposit);

        mockMvc.perform(post("/bookings/100/deposit/simulate-payment").sessionAttr("userId", 10L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/100"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}
