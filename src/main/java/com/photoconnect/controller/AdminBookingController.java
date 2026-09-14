package com.photoconnect.controller;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.service.AdminBookingService;
import com.photoconnect.util.AdminSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

        List<Booking> bookings = adminBookingService.listBookings(statusFilter);

        model.addAttribute("bookings", bookings);
        model.addAttribute("selectedStatus", statusFilter != null ? statusFilter.name() : "ALL");
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("activeTab", "bookings");

        return "admin-bookings";
    }
}
