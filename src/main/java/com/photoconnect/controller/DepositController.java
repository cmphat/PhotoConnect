package com.photoconnect.controller;

import com.photoconnect.dto.BookingViewDto;
import com.photoconnect.dto.DepositViewDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.service.BookingService;
import com.photoconnect.service.DepositService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DepositController {

    private final DepositService depositService;
    private final BookingService bookingService;

    public DepositController(DepositService depositService, BookingService bookingService) {
        this.depositService = depositService;
        this.bookingService = bookingService;
    }

    /**
     * Renders the deposit payment page. Creates a PENDING deposit if one doesn't exist.
     */
    @GetMapping("/bookings/{id}/deposit")
    public String showDepositPage(@PathVariable("id") Long bookingId, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // Ensure booking exists and belongs to the customer
            Booking booking = bookingService.getBookingForCustomer(bookingId, userId);
            
            // Get or create deposit
            DepositViewDto deposit = depositService.getOrCreateDepositForBooking(bookingId, userId);
            
            model.addAttribute("booking", BookingViewDto.from(booking));
            model.addAttribute("deposit", deposit);
            
            return "deposit";
        } catch (InvalidBookingException | UnauthorizedException ex) {
            return "redirect:/bookings/" + bookingId;
        }
    }

    /**
     * Development-only simulated payment endpoint.
     */
    @PostMapping("/bookings/{id}/deposit/simulate-payment")
    public String simulatePayment(@PathVariable("id") Long bookingId, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            depositService.simulateSuccessfulPayment(bookingId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Payment simulated successfully.");
        } catch (InvalidBookingException | UnauthorizedException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/bookings/" + bookingId;
    }
}
