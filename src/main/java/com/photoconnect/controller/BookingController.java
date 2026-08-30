package com.photoconnect.controller;

import com.photoconnect.dto.BookingRequest;
import com.photoconnect.dto.BookingResponseDto;
import com.photoconnect.dto.PhotographerPublicDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.SelfBookingNotAllowedException;
import com.photoconnect.service.BookingService;
import com.photoconnect.service.PublicPhotographerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final PublicPhotographerService publicPhotographerService;

    public BookingController(BookingService bookingService,
                             PublicPhotographerService publicPhotographerService) {
        this.bookingService = bookingService;
        this.publicPhotographerService = publicPhotographerService;
    }

    /**
     * Renders the booking request form for an approved photographer.
     */
    @GetMapping("/photographers/{id}/book")
    public String showBookingForm(@PathVariable("id") Long photographerId,
                                  HttpSession session,
                                  Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            PhotographerPublicDto photographer = publicPhotographerService.getApprovedPhotographerById(photographerId);

            BookingRequest bookingRequest = new BookingRequest();
            bookingRequest.setPhotographerId(photographerId);
            bookingRequest.setBookingDate(LocalDate.now().plusDays(1));

            model.addAttribute("photographer", photographer);
            model.addAttribute("bookingRequest", bookingRequest);
            model.addAttribute("minBookingDate", LocalDate.now().toString());

            return "booking-form";
        } catch (IllegalArgumentException e) {
            return "redirect:/photographers";
        }
    }

    /**
     * Processes booking request submission.
     */
    @PostMapping("/photographers/{id}/book")
    public String submitBooking(@PathVariable("id") Long photographerId,
                                @Valid @ModelAttribute("bookingRequest") BookingRequest bookingRequest,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        PhotographerPublicDto photographer;
        try {
            photographer = publicPhotographerService.getApprovedPhotographerById(photographerId);
        } catch (IllegalArgumentException e) {
            return "redirect:/photographers";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("photographer", photographer);
            model.addAttribute("minBookingDate", LocalDate.now().toString());
            return "booking-form";
        }

        try {
            Booking booking = bookingService.createBooking(userId, photographerId, bookingRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Your booking request has been submitted successfully!");
            return "redirect:/bookings/" + booking.getId() + "/success";
        } catch (SelfBookingNotAllowedException | InvalidBookingException ex) {
            model.addAttribute("photographer", photographer);
            model.addAttribute("minBookingDate", LocalDate.now().toString());
            model.addAttribute("errorMessage", ex.getMessage());
            return "booking-form";
        }
    }

    /**
     * Renders the booking confirmation / success page for the customer who made the booking.
     */
    @GetMapping("/bookings/{id}/success")
    public String showBookingSuccess(@PathVariable("id") Long bookingId,
                                     HttpSession session,
                                     Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            Booking booking = bookingService.getBookingForCustomer(bookingId, userId);
            model.addAttribute("booking", BookingResponseDto.from(booking));
            return "booking-success";
        } catch (InvalidBookingException ex) {
            return "redirect:/photographers";
        }
    }
}
