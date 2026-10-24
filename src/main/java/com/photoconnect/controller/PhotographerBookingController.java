package com.photoconnect.controller;

import com.photoconnect.dto.BookingViewDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.UserRole;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/photographer/bookings")
public class PhotographerBookingController {

    private final BookingService bookingService;
    private final com.photoconnect.service.BookingStateMachineService bookingStateMachineService;
    private final com.photoconnect.service.RescheduleService rescheduleService;

    @org.springframework.beans.factory.annotation.Autowired
    public PhotographerBookingController(BookingService bookingService,
                                         @org.springframework.beans.factory.annotation.Autowired(required = false) com.photoconnect.service.BookingStateMachineService bookingStateMachineService,
                                         @org.springframework.beans.factory.annotation.Autowired(required = false) com.photoconnect.service.RescheduleService rescheduleService) {
        this.bookingService = bookingService;
        this.bookingStateMachineService = bookingStateMachineService;
        this.rescheduleService = rescheduleService;
    }

    public PhotographerBookingController(BookingService bookingService,
                                         com.photoconnect.service.BookingStateMachineService bookingStateMachineService) {
        this(bookingService, bookingStateMachineService, null);
    }

    public PhotographerBookingController(BookingService bookingService) {
        this(bookingService, null, null);
    }

    @GetMapping
    public String listPhotographerBookings(HttpSession session, Model model) {
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        List<Booking> bookings = bookingService.getPhotographerBookings(userId);
        model.addAttribute("bookings", bookings.stream().map(BookingViewDto::from).toList());
        return "photographer-bookings";
    }

    @GetMapping("/{id}")
    public String viewPhotographerBooking(@PathVariable("id") Long bookingId, HttpSession session, Model model) {
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        try {
            Booking booking = bookingService.getBookingForPhotographer(bookingId, userId);
            model.addAttribute("booking", BookingViewDto.from(booking));
            if (bookingStateMachineService != null) {
                model.addAttribute("history", bookingStateMachineService.getHistoryForBooking(bookingId, userId));
            }
            if (rescheduleService != null) {
                model.addAttribute("rescheduleRequests", rescheduleService.getRescheduleRequestsForBooking(bookingId, userId));
                rescheduleService.getPendingRescheduleRequest(bookingId, userId)
                        .ifPresent(p -> model.addAttribute("pendingReschedule", p));
            }
            return "photographer-booking-detail";
        } catch (InvalidBookingException ex) {
            return "redirect:/photographer/bookings";
        }
    }

    @PostMapping("/{id}/accept")
    public String acceptBooking(@PathVariable("id") Long bookingId, HttpSession session, RedirectAttributes redirectAttributes) {
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

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
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

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
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        try {
            bookingService.completeBooking(bookingId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Booking marked as completed.");
        } catch (InvalidBookingException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/photographer/bookings/" + bookingId;
    }

    @PostMapping("/{id}/advance-status")
    public String advanceStatus(@PathVariable("id") Long bookingId,
                                @org.springframework.web.bind.annotation.RequestParam("targetStatus") com.photoconnect.entity.BookingStatus targetStatus,
                                @org.springframework.web.bind.annotation.RequestParam(value = "reason", required = false) String reason,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        if (bookingStateMachineService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "State machine service is not configured.");
            return "redirect:/photographer/bookings/" + bookingId;
        }

        try {
            bookingStateMachineService.transition(bookingId, targetStatus, userId, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Booking advanced to " + targetStatus.getDisplayName() + ".");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/photographer/bookings/" + bookingId;
    }

    @PostMapping("/{id}/cancel")
    public String cancelPhotographerBooking(@PathVariable("id") Long bookingId,
                                            @RequestParam(value = "reason", required = false) String reason,
                                            HttpSession session,
                                            RedirectAttributes redirectAttributes) {
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        try {
            bookingService.cancelBookingByPhotographer(bookingId, userId, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled. Client has been notified and any paid deposit credited for full refund.");
        } catch (InvalidBookingException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/photographer/bookings/" + bookingId;
    }

    @PostMapping("/{id}/reschedule")
    public String requestPhotographerReschedule(@PathVariable("id") Long bookingId,
                                                @RequestParam("proposedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate proposedDate,
                                                @RequestParam("proposedTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime proposedTime,
                                                @RequestParam(value = "reason", required = false) String reason,
                                                HttpSession session,
                                                RedirectAttributes redirectAttributes) {
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        if (rescheduleService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rescheduling service is currently unavailable.");
            return "redirect:/photographer/bookings/" + bookingId;
        }

        try {
            rescheduleService.createRescheduleRequest(bookingId, userId, proposedDate, proposedTime, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Reschedule proposal submitted to client.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/photographer/bookings/" + bookingId;
    }

    @PostMapping("/{id}/reschedule/{requestId}/accept")
    public String acceptPhotographerReschedule(@PathVariable("id") Long bookingId,
                                              @PathVariable("requestId") Long requestId,
                                              @RequestParam(value = "responseNote", required = false) String responseNote,
                                              HttpSession session,
                                              RedirectAttributes redirectAttributes) {
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        if (rescheduleService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rescheduling service is currently unavailable.");
            return "redirect:/photographer/bookings/" + bookingId;
        }

        try {
            rescheduleService.acceptReschedule(requestId, userId, responseNote);
            redirectAttributes.addFlashAttribute("successMessage", "Reschedule confirmed! Session date and time updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/photographer/bookings/" + bookingId;
    }

    @PostMapping("/{id}/reschedule/{requestId}/reject")
    public String rejectPhotographerReschedule(@PathVariable("id") Long bookingId,
                                              @PathVariable("requestId") Long requestId,
                                              @RequestParam(value = "reason", required = false) String reason,
                                              HttpSession session,
                                              RedirectAttributes redirectAttributes) {
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        if (rescheduleService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rescheduling service is currently unavailable.");
            return "redirect:/photographer/bookings/" + bookingId;
        }

        try {
            rescheduleService.rejectReschedule(requestId, userId, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Reschedule request declined. Original schedule preserved.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/photographer/bookings/" + bookingId;
    }

    @PostMapping("/{id}/reschedule/{requestId}/cancel")
    public String cancelPhotographerReschedule(@PathVariable("id") Long bookingId,
                                              @PathVariable("requestId") Long requestId,
                                              HttpSession session,
                                              RedirectAttributes redirectAttributes) {
        String redirect = com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        if (rescheduleService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rescheduling service is currently unavailable.");
            return "redirect:/photographer/bookings/" + bookingId;
        }

        try {
            rescheduleService.cancelReschedule(requestId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Reschedule proposal withdrawn.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/photographer/bookings/" + bookingId;
    }
}
