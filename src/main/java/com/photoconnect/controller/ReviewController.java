package com.photoconnect.controller;

import com.photoconnect.dto.BookingViewDto;
import com.photoconnect.dto.ReviewRequest;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.exception.BookingNotCompletedException;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.InvalidReviewException;
import com.photoconnect.exception.ReviewAccessDeniedException;
import com.photoconnect.exception.ReviewAlreadyExistsException;
import com.photoconnect.service.BookingService;
import com.photoconnect.service.ReviewService;
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

@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final BookingService bookingService;

    public ReviewController(ReviewService reviewService, BookingService bookingService) {
        this.reviewService = reviewService;
        this.bookingService = bookingService;
    }

    @GetMapping("/bookings/{id}/review")
    public String showReviewForm(@PathVariable("id") Long bookingId,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            Booking booking = bookingService.getBookingForCustomer(bookingId, userId);

            if (booking.getStatus() != BookingStatus.COMPLETED) {
                redirectAttributes.addFlashAttribute("errorMessage", "Only completed bookings can be reviewed.");
                return "redirect:/bookings/" + bookingId;
            }

            if (reviewService.hasReviewForBooking(bookingId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "A review has already been submitted for this booking.");
                return "redirect:/bookings/" + bookingId;
            }

            model.addAttribute("booking", BookingViewDto.from(booking));
            if (!model.containsAttribute("reviewRequest")) {
                model.addAttribute("reviewRequest", new ReviewRequest());
            }

            return "review-form";
        } catch (InvalidBookingException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/bookings";
        }
    }

    @PostMapping("/bookings/{id}/review")
    public String submitReview(@PathVariable("id") Long bookingId,
                               @Valid @ModelAttribute("reviewRequest") ReviewRequest reviewRequest,
                               BindingResult bindingResult,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            try {
                Booking booking = bookingService.getBookingForCustomer(bookingId, userId);
                model.addAttribute("booking", BookingViewDto.from(booking));
                return "review-form";
            } catch (InvalidBookingException ex) {
                return "redirect:/bookings";
            }
        }

        try {
            reviewService.createReview(bookingId, userId, reviewRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you! Your review has been submitted.");
            return "redirect:/bookings/" + bookingId;
        } catch (ReviewAlreadyExistsException | BookingNotCompletedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/bookings/" + bookingId;
        } catch (ReviewAccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/bookings";
        } catch (InvalidReviewException ex) {
            try {
                Booking booking = bookingService.getBookingForCustomer(bookingId, userId);
                model.addAttribute("booking", BookingViewDto.from(booking));
                model.addAttribute("errorMessage", ex.getMessage());
                return "review-form";
            } catch (InvalidBookingException ibEx) {
                return "redirect:/bookings";
            }
        }
    }
}
