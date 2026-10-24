package com.photoconnect.controller;

import com.photoconnect.dto.BookingViewDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.UserRole;
import com.photoconnect.exception.ErrorCode;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.PhotographerUnavailableException;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.service.BookingService;
import com.photoconnect.service.BookingStateMachineService;
import com.photoconnect.service.RescheduleService;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/photographer/bookings")
public class PhotographerBookingController {

    private final BookingService bookingService;
    private final BookingStateMachineService bookingStateMachineService;
    private final RescheduleService rescheduleService;

    @Autowired
    public PhotographerBookingController(BookingService bookingService,
                                         @Autowired(required = false) BookingStateMachineService bookingStateMachineService,
                                         @Autowired(required = false) RescheduleService rescheduleService) {
        this.bookingService = bookingService;
        this.bookingStateMachineService = bookingStateMachineService;
        this.rescheduleService = rescheduleService;
    }

    public PhotographerBookingController(BookingService bookingService,
                                         BookingStateMachineService bookingStateMachineService) {
        this(bookingService, bookingStateMachineService, null);
    }

    public PhotographerBookingController(BookingService bookingService) {
        this(bookingService, null, null);
    }

    @GetMapping
    public String listPhotographerBookings(HttpSession session, Model model) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        List<Booking> bookings = bookingService.getPhotographerBookings(userId);
        model.addAttribute("bookings", bookings.stream().map(BookingViewDto::from).toList());
        return "photographer-bookings";
    }

    @GetMapping("/{id}")
    public String viewPhotographerBooking(@PathVariable("id") Long bookingId, HttpSession session, Model model) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        try {
            Booking booking = bookingService.getBookingForPhotographer(bookingId, userId);
            BookingViewDto viewDto = BookingViewDto.from(booking);
            if (booking.getCustomer() != null && booking.getPhotographerProfile() != null) {
                long shootCount = bookingService.getCompletedShootCountWithCustomer(
                        booking.getPhotographerProfile().getId(),
                        booking.getCustomer().getId()
                );
                viewDto.setPastCompletedShootsWithClient(shootCount);
            }
            model.addAttribute("booking", viewDto);
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
            if (ex.getErrorCode() == ErrorCode.BOOKING_005_NOT_OWNER || ex.getErrorCode() == ErrorCode.AUTH_005_ACCESS_DENIED) {
                throw ex;
            }
            return "redirect:/photographer/bookings";
        }
    }

    @PostMapping("/{id}/accept")
    public String acceptBooking(@PathVariable("id") Long bookingId, HttpSession session, RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        try {
            bookingService.acceptBooking(bookingId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Booking accepted successfully.");
        } catch (InvalidBookingException ex) {
            if (ex.getErrorCode() == ErrorCode.BOOKING_005_NOT_OWNER || ex.getErrorCode() == ErrorCode.AUTH_005_ACCESS_DENIED
                    || ex.getErrorCode() == ErrorCode.BOOKING_003_INVALID_STATUS || ex.getErrorCode() == ErrorCode.BOOKING_002_TIME_CONFLICT) {
                throw ex;
            }
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/photographer/bookings/" + bookingId;
    }

    @PostMapping("/{id}/reject")
    public String rejectBooking(@PathVariable("id") Long bookingId, HttpSession session, RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        try {
            bookingService.rejectBooking(bookingId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Booking rejected.");
        } catch (InvalidBookingException ex) {
            if (ex.getErrorCode() == ErrorCode.BOOKING_005_NOT_OWNER || ex.getErrorCode() == ErrorCode.AUTH_005_ACCESS_DENIED
                    || ex.getErrorCode() == ErrorCode.BOOKING_003_INVALID_STATUS) {
                throw ex;
            }
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/photographer/bookings/" + bookingId;
    }

    @PostMapping("/{id}/complete")
    public String completeBooking(@PathVariable("id") Long bookingId, HttpSession session, RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        try {
            bookingService.completeBooking(bookingId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Booking marked as completed.");
        } catch (InvalidBookingException ex) {
            if (ex.getErrorCode() == ErrorCode.BOOKING_005_NOT_OWNER || ex.getErrorCode() == ErrorCode.AUTH_005_ACCESS_DENIED
                    || ex.getErrorCode() == ErrorCode.BOOKING_003_INVALID_STATUS) {
                throw ex;
            }
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/photographer/bookings/" + bookingId;
    }

    @PostMapping("/{id}/advance-status")
    public String advanceStatus(@PathVariable("id") Long bookingId,
                                @RequestParam("targetStatus") BookingStatus targetStatus,
                                @RequestParam(value = "reason", required = false) String reason,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        // SEC-01: Validate actor's authority over that booking
        bookingService.getBookingForPhotographer(bookingId, userId);

        // SEC-01: Restrict target status transitions allowed by photographer via advance-status
        if (targetStatus != BookingStatus.SHOOTING && targetStatus != BookingStatus.POST_PROCESSING) {
            throw new InvalidBookingException(ErrorCode.BOOKING_003_INVALID_STATUS,
                    "Photographers can only advance status to SHOOTING or POST_PROCESSING.");
        }

        if (bookingStateMachineService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "State machine service is not configured.");
            return "redirect:/photographer/bookings/" + bookingId;
        }

        try {
            bookingStateMachineService.transition(bookingId, targetStatus, userId, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Booking advanced to " + targetStatus.getDisplayName() + ".");
        } catch (InvalidBookingException | UnauthorizedException | IllegalStateException ex) {
            throw ex;
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
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        try {
            bookingService.cancelBookingByPhotographer(bookingId, userId, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled. Client has been notified and any paid deposit credited for full refund.");
        } catch (InvalidBookingException ex) {
            if (ex.getErrorCode() == ErrorCode.BOOKING_005_NOT_OWNER || ex.getErrorCode() == ErrorCode.AUTH_005_ACCESS_DENIED
                    || ex.getErrorCode() == ErrorCode.BOOKING_003_INVALID_STATUS) {
                throw ex;
            }
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
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        // SEC-01: Validate actor's authority over that booking
        bookingService.getBookingForPhotographer(bookingId, userId);

        if (rescheduleService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rescheduling service is currently unavailable.");
            return "redirect:/photographer/bookings/" + bookingId;
        }

        try {
            rescheduleService.createRescheduleRequest(bookingId, userId, proposedDate, proposedTime, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Reschedule proposal submitted to client.");
        } catch (InvalidBookingException | PhotographerUnavailableException | UnauthorizedException | IllegalStateException ex) {
            throw ex;
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
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        // SEC-01: Validate actor's authority over that booking
        bookingService.getBookingForPhotographer(bookingId, userId);

        if (rescheduleService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rescheduling service is currently unavailable.");
            return "redirect:/photographer/bookings/" + bookingId;
        }

        try {
            rescheduleService.acceptReschedule(requestId, userId, responseNote);
            redirectAttributes.addFlashAttribute("successMessage", "Reschedule confirmed! Session date and time updated.");
        } catch (InvalidBookingException | PhotographerUnavailableException | UnauthorizedException | IllegalStateException ex) {
            throw ex;
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
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        // SEC-01: Validate actor's authority over that booking
        bookingService.getBookingForPhotographer(bookingId, userId);

        if (rescheduleService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rescheduling service is currently unavailable.");
            return "redirect:/photographer/bookings/" + bookingId;
        }

        try {
            rescheduleService.rejectReschedule(requestId, userId, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Reschedule request declined. Original schedule preserved.");
        } catch (InvalidBookingException | PhotographerUnavailableException | UnauthorizedException | IllegalStateException ex) {
            throw ex;
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
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        // SEC-01: Validate actor's authority over that booking
        bookingService.getBookingForPhotographer(bookingId, userId);

        if (rescheduleService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rescheduling service is currently unavailable.");
            return "redirect:/photographer/bookings/" + bookingId;
        }

        try {
            rescheduleService.cancelReschedule(requestId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Reschedule proposal withdrawn.");
        } catch (InvalidBookingException | PhotographerUnavailableException | UnauthorizedException | IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/photographer/bookings/" + bookingId;
    }

    @PostMapping("/{id}/notes")
    public String updatePhotographerNotes(@PathVariable("id") Long bookingId,
                                          @RequestParam(value = "notes", required = false) String notes,
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        try {
            bookingService.updatePhotographerNotes(bookingId, userId, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Ghi chú nội bộ studio đã được cập nhật thành công.");
        } catch (InvalidBookingException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/photographer/bookings/" + bookingId;
    }
}
