package com.photoconnect.controller;

import com.photoconnect.dto.BookingViewDto;
import com.photoconnect.dto.DemoPaymentRequest;
import com.photoconnect.dto.DepositViewDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.entity.UserRole;
import com.photoconnect.service.BookingService;
import com.photoconnect.service.DemoQrCodeService;
import com.photoconnect.service.DepositService;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;

@Controller
public class DepositController {

    private final DepositService depositService;
    private final BookingService bookingService;
    private final DemoQrCodeService qrCodeService;

    public DepositController(DepositService depositService,
                             BookingService bookingService,
                             DemoQrCodeService qrCodeService) {
        this.depositService = depositService;
        this.bookingService = bookingService;
        this.qrCodeService = qrCodeService;
    }

    /** Keeps the TASK-015 URL usable while making checkout the canonical route. */
    @GetMapping("/bookings/{id}/deposit")
    public String redirectToCheckout(@PathVariable("id") Long bookingId, HttpSession session) {
        String redirect = requireCustomer(session);
        return redirect != null ? redirect : "redirect:/bookings/" + bookingId + "/deposit/checkout";
    }

    @GetMapping("/bookings/{id}/deposit/checkout")
    public String showCheckout(@PathVariable("id") Long bookingId, HttpSession session, Model model) {
        String redirect = requireCustomer(session);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        Booking booking = bookingService.getBookingForCustomer(bookingId, userId);
        DepositViewDto deposit = depositService.getOrCreateDepositForBooking(bookingId, userId);
        if (deposit.getStatus() == DepositStatus.PAID) {
            return "redirect:/bookings/" + bookingId + "/deposit/receipt";
        }

        addPaymentModel(model, booking, deposit);
        DemoQrCodeService.DemoQrCode qr = qrCodeService.generate(
                bookingId, deposit.getAmount(), deposit.getPaymentReference());
        model.addAttribute("demoQrPayload", qr.payload());
        model.addAttribute("demoQrDataUri", qr.dataUri());
        model.addAttribute("paymentRequest", new DemoPaymentRequest());
        return "deposit";
    }

    @PostMapping("/bookings/{id}/deposit/process")
    public String processPayment(@PathVariable("id") Long bookingId,
                                 @ModelAttribute DemoPaymentRequest paymentRequest,
                                 HttpSession session) {
        String redirect = requireCustomer(session);
        if (redirect != null) return redirect;
        depositService.processDemoPayment(bookingId, SessionSecurityUtils.userId(session), paymentRequest);
        return "redirect:/bookings/" + bookingId + "/deposit/result";
    }

    @PostMapping("/bookings/{id}/deposit/cancel")
    public String cancelPayment(@PathVariable("id") Long bookingId, HttpSession session) {
        String redirect = requireCustomer(session);
        if (redirect != null) return redirect;
        depositService.cancelDemoPayment(bookingId, SessionSecurityUtils.userId(session));
        return "redirect:/bookings/" + bookingId + "/deposit/result";
    }

    @GetMapping("/bookings/{id}/deposit/result")
    public String showResult(@PathVariable("id") Long bookingId, HttpSession session, Model model) {
        String redirect = requireCustomer(session);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);
        Booking booking = bookingService.getBookingForCustomer(bookingId, userId);
        DepositViewDto deposit = depositService.getCustomerDeposit(bookingId, userId);
        if (deposit == null) {
            return "redirect:/bookings/" + bookingId + "/deposit/checkout";
        }
        addPaymentModel(model, booking, deposit);
        return "deposit-result";
    }

    @GetMapping("/bookings/{id}/deposit/receipt")
    public String showReceipt(@PathVariable("id") Long bookingId, HttpSession session, Model model) {
        String redirect = requireCustomer(session);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);
        Booking booking = bookingService.getBookingForCustomer(bookingId, userId);
        DepositViewDto deposit = depositService.getCustomerDeposit(bookingId, userId);
        if (deposit == null || deposit.getStatus() != DepositStatus.PAID) {
            return "redirect:/bookings/" + bookingId + "/deposit/result";
        }
        addPaymentModel(model, booking, deposit);
        return "deposit-receipt";
    }

    private void addPaymentModel(Model model, Booking booking, DepositViewDto deposit) {
        BookingViewDto bookingView = BookingViewDto.from(booking);
        BigDecimal remainingBalance = bookingView.getAgreedPrice().subtract(deposit.getAmount());
        model.addAttribute("booking", bookingView);
        model.addAttribute("deposit", deposit);
        model.addAttribute("remainingBalance", remainingBalance);
    }

    private String requireCustomer(HttpSession session) {
        return SessionSecurityUtils.requireRole(session, UserRole.CUSTOMER);
    }
}
