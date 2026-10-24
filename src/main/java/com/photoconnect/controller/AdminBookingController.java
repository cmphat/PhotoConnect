package com.photoconnect.controller;

import com.photoconnect.dto.ApiResponse;
import com.photoconnect.dto.BookingHistoryDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.exception.ErrorCode;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.service.AdminBookingService;
import com.photoconnect.util.AdminSecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    public AdminBookingController(AdminBookingService adminBookingService) {
        this.adminBookingService = adminBookingService;
    }

    @GetMapping
    public String listBookings(@RequestParam(value = "status", required = false) String statusStr,
                               @RequestParam(value = "search", required = false) String search,
                               @RequestParam(value = "startDate", required = false) String startDateStr,
                               @RequestParam(value = "endDate", required = false) String endDateStr,
                               HttpSession session,
                               Model model) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        BookingStatus statusFilter = null;
        if (statusStr != null && !statusStr.isBlank() && !"ALL".equalsIgnoreCase(statusStr)) {
            try {
                statusFilter = BookingStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        LocalDate startDate = null;
        if (startDateStr != null && !startDateStr.isBlank()) {
            try {
                startDate = LocalDate.parse(startDateStr.trim());
            } catch (DateTimeParseException ignored) {
            }
        }

        LocalDate endDate = null;
        if (endDateStr != null && !endDateStr.isBlank()) {
            try {
                endDate = LocalDate.parse(endDateStr.trim());
            } catch (DateTimeParseException ignored) {
            }
        }

        List<Booking> bookings;
        if ((search == null || search.isBlank()) && startDate == null && endDate == null) {
            bookings = adminBookingService.listBookings(statusFilter);
        } else {
            bookings = adminBookingService.searchBookings(statusFilter, search, startDate, endDate);
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("selectedStatus", statusFilter != null ? statusFilter.name() : "ALL");
        model.addAttribute("search", search != null ? search.trim() : "");
        model.addAttribute("startDate", startDateStr != null ? startDateStr.trim() : "");
        model.addAttribute("endDate", endDateStr != null ? endDateStr.trim() : "");
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("activeTab", "bookings");

        return "admin-bookings";
    }

    @GetMapping("/{id}")
    public String bookingDetail(@PathVariable("id") Long id,
                                HttpSession session,
                                Model model) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            Booking booking = adminBookingService.getBookingDetails(id);
            List<BookingHistoryDto> auditLogs = adminBookingService.getBookingAuditLogs(id);

            model.addAttribute("booking", booking);
            model.addAttribute("auditLogs", auditLogs);
            model.addAttribute("activeTab", "bookings");

            return "admin-booking-detail";
        } catch (IllegalArgumentException ex) {
            return "redirect:/admin/bookings";
        }
    }

    @PostMapping("/{id}/resolve-dispute")
    public Object resolveDispute(@PathVariable("id") Long id,
                                 @RequestParam("resolution") String resolution,
                                 @RequestParam(value = "adminNotes", required = false) String adminNotes,
                                 HttpServletRequest request,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            if (isJsonRequest(request)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(ErrorCode.AUTH_001_INVALID_CREDENTIALS, "Authentication required"));
            }
            return redirect;
        }

        Long adminUserId = (Long) session.getAttribute("userId");
        try {
            adminBookingService.resolveDispute(id, resolution, adminNotes, adminUserId);
            if (isJsonRequest(request)) {
                return ResponseEntity.ok(ApiResponse.success("Dispute for booking #" + id + " has been successfully resolved (" + resolution + ")."));
            }
            redirectAttributes.addFlashAttribute("successMessage",
                    "Dispute for booking #" + id + " has been successfully resolved (" + resolution + ").");
            return "redirect:/admin/bookings/" + id;
        } catch (InvalidBookingException | IllegalStateException ex) {
            if (isJsonRequest(request)) {
                throw ex;
            }
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/bookings/" + id;
        } catch (Exception ex) {
            if (isJsonRequest(request)) {
                throw ex;
            }
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/bookings/" + id;
        }
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/api/");
    }
}
