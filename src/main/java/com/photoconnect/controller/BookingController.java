package com.photoconnect.controller;

import com.photoconnect.dto.BookingRequest;
import com.photoconnect.dto.BookingResponseDto;
import com.photoconnect.dto.PhotographerPublicDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.UserRole;
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
import java.time.LocalTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final PublicPhotographerService publicPhotographerService;
    private final com.photoconnect.service.DepositService depositService;
    private final com.photoconnect.service.ReviewService reviewService;
    private final com.photoconnect.service.ServicePackageService servicePackageService;
    private final com.photoconnect.service.RescheduleService rescheduleService;
    private final com.photoconnect.service.ReportService reportService;

    private String requireCustomer(HttpSession session) {
        return com.photoconnect.util.SessionSecurityUtils.requireRole(session, UserRole.CUSTOMER);
    }

    public BookingController(BookingService bookingService,
                             PublicPhotographerService publicPhotographerService,
                             com.photoconnect.service.DepositService depositService,
                             com.photoconnect.service.ReviewService reviewService) {
        this(bookingService, publicPhotographerService, depositService, reviewService, null, null, null);
    }

    public BookingController(BookingService bookingService,
                             PublicPhotographerService publicPhotographerService,
                             com.photoconnect.service.DepositService depositService,
                             com.photoconnect.service.ReviewService reviewService,
                             com.photoconnect.service.ServicePackageService servicePackageService) {
        this(bookingService, publicPhotographerService, depositService, reviewService, servicePackageService, null, null);
    }

    public BookingController(BookingService bookingService,
                             PublicPhotographerService publicPhotographerService,
                             com.photoconnect.service.DepositService depositService,
                             com.photoconnect.service.ReviewService reviewService,
                             com.photoconnect.service.ServicePackageService servicePackageService,
                             com.photoconnect.service.RescheduleService rescheduleService) {
        this(bookingService, publicPhotographerService, depositService, reviewService, servicePackageService, rescheduleService, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public BookingController(BookingService bookingService,
                             PublicPhotographerService publicPhotographerService,
                             com.photoconnect.service.DepositService depositService,
                             com.photoconnect.service.ReviewService reviewService,
                             @org.springframework.beans.factory.annotation.Autowired(required = false) com.photoconnect.service.ServicePackageService servicePackageService,
                             @org.springframework.beans.factory.annotation.Autowired(required = false) com.photoconnect.service.RescheduleService rescheduleService,
                             @org.springframework.beans.factory.annotation.Autowired(required = false) com.photoconnect.service.ReportService reportService) {
        this.bookingService = bookingService;
        this.publicPhotographerService = publicPhotographerService;
        this.depositService = depositService;
        this.reviewService = reviewService;
        this.servicePackageService = servicePackageService;
        this.rescheduleService = rescheduleService;
        this.reportService = reportService;
    }

    /**
     * Renders the booking request form for an approved photographer.
     */
    @GetMapping("/photographers/{id}/book")
    public String showBookingForm(@PathVariable("id") Long photographerId,
                                  @org.springframework.web.bind.annotation.RequestParam(value = "packageId", required = false) Long packageId,
                                  HttpSession session,
                                  Model model) {
        String redirect = requireCustomer(session);
        if (redirect != null) return redirect;

        try {
            PhotographerPublicDto photographer = publicPhotographerService.getApprovedPhotographerById(photographerId);

            BookingRequest bookingRequest = new BookingRequest();
            bookingRequest.setPhotographerId(photographerId);
            bookingRequest.setPackageId(packageId);
            bookingRequest.setBookingDate(LocalDate.now().plusDays(1));

            List<com.photoconnect.dto.ServicePackageDto> packages = servicePackageService != null
                    ? servicePackageService.getActivePackagesForPhotographer(photographerId)
                    : java.util.Collections.emptyList();

            model.addAttribute("photographer", photographer);
            model.addAttribute("bookingRequest", bookingRequest);
            model.addAttribute("packages", packages);
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
        String redirect = requireCustomer(session);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

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
        } catch (SelfBookingNotAllowedException | InvalidBookingException | com.photoconnect.exception.PhotographerUnavailableException ex) {
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
        String redirect = requireCustomer(session);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        try {
            Booking booking = bookingService.getBookingForCustomer(bookingId, userId);
            model.addAttribute("booking", BookingResponseDto.from(booking));
            return "booking-success";
        } catch (InvalidBookingException ex) {
            return "redirect:/photographers";
        }
    }

    /**
     * List customer bookings.
     */
    @GetMapping("/bookings")
    public String listCustomerBookings(@RequestParam(value = "status", required = false) String statusStr,
                                       HttpSession session, Model model) {
        String redirect = requireCustomer(session);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        java.util.List<Booking> bookings = bookingService.getCustomerBookings(userId);
        if (statusStr != null && !statusStr.isBlank() && !"ALL".equalsIgnoreCase(statusStr)) {
            String norm = statusStr.trim().toUpperCase();
            if ("UPCOMING".equals(norm)) {
                java.time.LocalDate today = java.time.LocalDate.now();
                bookings = bookings.stream()
                        .filter(b -> (b.getStatus() == com.photoconnect.entity.BookingStatus.ACCEPTED
                                || b.getStatus() == com.photoconnect.entity.BookingStatus.PENDING)
                                && !b.getBookingDate().isBefore(today))
                        .toList();
            } else {
                try {
                    com.photoconnect.entity.BookingStatus filterStatus = com.photoconnect.entity.BookingStatus.valueOf(norm);
                    bookings = bookings.stream().filter(b -> b.getStatus() == filterStatus).toList();
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        model.addAttribute("bookings", bookings.stream().map(com.photoconnect.dto.BookingViewDto::from).toList());
        model.addAttribute("selectedStatus", statusStr != null ? statusStr.trim().toUpperCase() : "ALL");
        return "bookings";
    }

    /**
     * View customer booking details.
     */
    /**
     * View customer booking details.
     */
    @GetMapping("/bookings/{id}")
    public String viewCustomerBooking(@PathVariable("id") Long bookingId, HttpSession session, Model model) {
        String redirect = requireCustomer(session);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        try {
            Booking booking = bookingService.getBookingForCustomer(bookingId, userId);
            model.addAttribute("booking", com.photoconnect.dto.BookingViewDto.from(booking));
            
            // Add deposit information
            com.photoconnect.dto.DepositViewDto deposit = depositService.getCustomerDeposit(bookingId, userId);
            model.addAttribute("deposit", deposit);

            // Add review information
            com.photoconnect.dto.ReviewDto review = reviewService.getReviewByBookingId(bookingId, userId);
            model.addAttribute("review", review);

            // Add rescheduling information
            if (rescheduleService != null) {
                model.addAttribute("rescheduleRequests", rescheduleService.getRescheduleRequestsForBooking(bookingId, userId));
                rescheduleService.getPendingRescheduleRequest(bookingId, userId)
                        .ifPresent(p -> model.addAttribute("pendingReschedule", p));
            }
            
            return "booking-detail";
        } catch (InvalidBookingException ex) {
            return "redirect:/bookings";
        }
    }

    /**
     * Cancel customer booking.
     */
    @PostMapping("/bookings/{id}/cancel")
    public String cancelCustomerBooking(@PathVariable("id") Long bookingId,
                                        @RequestParam(value = "reason", required = false) String reason,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        String redirect = requireCustomer(session);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        try {
            bookingService.cancelBooking(bookingId, userId, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully.");
        } catch (InvalidBookingException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/bookings/" + bookingId;
    }

    /**
     * Customer requests session reschedule.
     */
    @PostMapping("/bookings/{id}/reschedule")
    public String requestCustomerReschedule(@PathVariable("id") Long bookingId,
                                            @RequestParam("proposedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate proposedDate,
                                            @RequestParam("proposedTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime proposedTime,
                                            @RequestParam(value = "reason", required = false) String reason,
                                            HttpSession session,
                                            RedirectAttributes redirectAttributes) {
        String redirect = requireCustomer(session);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        if (rescheduleService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rescheduling service is currently unavailable.");
            return "redirect:/bookings/" + bookingId;
        }

        try {
            rescheduleService.createRescheduleRequest(bookingId, userId, proposedDate, proposedTime, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Reschedule proposal submitted to photographer.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/bookings/" + bookingId;
    }

    /**
     * Customer accepts photographer-initiated reschedule request.
     */
    @PostMapping("/bookings/{id}/reschedule/{requestId}/accept")
    public String acceptCustomerReschedule(@PathVariable("id") Long bookingId,
                                          @PathVariable("requestId") Long requestId,
                                          @RequestParam(value = "responseNote", required = false) String responseNote,
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {
        String redirect = requireCustomer(session);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        if (rescheduleService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rescheduling service is currently unavailable.");
            return "redirect:/bookings/" + bookingId;
        }

        try {
            rescheduleService.acceptReschedule(requestId, userId, responseNote);
            redirectAttributes.addFlashAttribute("successMessage", "Reschedule confirmed! Session time updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/bookings/" + bookingId;
    }

    /**
     * Customer declines photographer-initiated reschedule request.
     */
    @PostMapping("/bookings/{id}/reschedule/{requestId}/reject")
    public String rejectCustomerReschedule(@PathVariable("id") Long bookingId,
                                          @PathVariable("requestId") Long requestId,
                                          @RequestParam(value = "reason", required = false) String reason,
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {
        String redirect = requireCustomer(session);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        if (rescheduleService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rescheduling service is currently unavailable.");
            return "redirect:/bookings/" + bookingId;
        }

        try {
            rescheduleService.rejectReschedule(requestId, userId, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Reschedule request declined. Original time preserved.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/bookings/" + bookingId;
    }

    /**
     * Customer cancels their own pending reschedule request.
     */
    @PostMapping("/bookings/{id}/reschedule/{requestId}/cancel")
    public String cancelCustomerReschedule(@PathVariable("id") Long bookingId,
                                          @PathVariable("requestId") Long requestId,
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {
        String redirect = requireCustomer(session);
        if (redirect != null) return redirect;
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);

        if (rescheduleService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rescheduling service is currently unavailable.");
            return "redirect:/bookings/" + bookingId;
        }

        try {
            rescheduleService.cancelReschedule(requestId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Reschedule proposal withdrawn.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/bookings/" + bookingId;
    }

    /**
     * Submit a trust & safety / dispute report for a booking.
     */
    @PostMapping("/bookings/{id}/report")
    public String submitBookingReport(@PathVariable("id") Long bookingId,
                                      @RequestParam("category") com.photoconnect.entity.ReportCategory category,
                                      @RequestParam("details") String details,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        if (reportService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hệ thống báo cáo sự cố hiện đang bảo trì.");
            return "redirect:/bookings/" + bookingId;
        }

        try {
            com.photoconnect.dto.CreateReportRequest request = new com.photoconnect.dto.CreateReportRequest(
                    category, details, bookingId, null
            );
            reportService.submitReport(userId, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Báo cáo sự cố của bạn đã được gửi tới Ban quản trị. Chúng tôi sẽ xử lý và liên hệ trong vòng 24 giờ.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/bookings/" + bookingId;
    }
}
