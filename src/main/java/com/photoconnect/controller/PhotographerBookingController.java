package com.photoconnect.controller;

import com.photoconnect.dto.BookingViewDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.service.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/photographer/bookings")
public class PhotographerBookingController {

    private final BookingService bookingService;

    public PhotographerBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public String listPhotographerBookings(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null || !"PHOTOGRAPHER".equals(session.getAttribute("userRole"))) {
            return "redirect:/login";
        }

        List<Booking> bookings = bookingService.getPhotographerBookings(userId);
        model.addAttribute("bookings", bookings.stream().map(BookingViewDto::from).toList());
        return "photographer-bookings";
    }

    @GetMapping("/{id}")
    public String viewPhotographerBooking(@PathVariable("id") Long bookingId, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null || !"PHOTOGRAPHER".equals(session.getAttribute("userRole"))) {
            return "redirect:/login";
        }

        try {
            Booking booking = bookingService.getBookingForPhotographer(bookingId, userId);
            model.addAttribute("booking", BookingViewDto.from(booking));
            return "photographer-booking-detail";
        } catch (InvalidBookingException ex) {
            return "redirect:/photographer/bookings";
        }
    }

    @PostMapping("/{id}/accept")
    public String acceptBooking(@PathVariable("id") Long bookingId, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null || !"PHOTOGRAPHER".equals(session.getAttribute("userRole"))) {
            return "redirect:/login";
        }

        try {
            bookingService.acceptBooking(bookingId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Booking accepted successfully.");
        } catch (InvalidBookingException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/photographer/bookings/" + bookingId;
    }

    @PostMapping("/{id}/reject")
    public String rejectBooking(@PathVariable("id") Long bookingId, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null || !"PHOTOGRAPHER".equals(session.getAttribute("userRole"))) {
            return "redirect:/login";
        }

        try {
            bookingService.rejectBooking(bookingId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Booking rejected.");
        } catch (InvalidBookingException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/photographer/bookings/" + bookingId;
    }

    @PostMapping("/{id}/complete")
    public String completeBooking(@PathVariable("id") Long bookingId, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null || !"PHOTOGRAPHER".equals(session.getAttribute("userRole"))) {
            return "redirect:/login";
        }

        try {
            bookingService.completeBooking(bookingId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Booking marked as completed.");
        } catch (InvalidBookingException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/photographer/bookings/" + bookingId;
    }
}
