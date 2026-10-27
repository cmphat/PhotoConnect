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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.Map;

@Controller
public class DepositController {

    private final DepositService depositService;
    private final BookingService bookingService;
    private final DemoQrCodeService qrCodeService;
    private final com.photoconnect.service.BookingStateMachineService bookingStateMachineService;
    private final com.photoconnect.service.NotificationService notificationService;

    @Value("${photoconnect.payment.vietqr.bank-id:MB}")
    private String bankId;

    @Value("${photoconnect.payment.vietqr.account-no:999988887777}")
    private String accountNo;

    @Value("${photoconnect.payment.vietqr.account-name:PHOTOCONNECT VIETNAM}")
    private String accountName;

    @org.springframework.beans.factory.annotation.Autowired
    public DepositController(DepositService depositService,
                             BookingService bookingService,
                             DemoQrCodeService qrCodeService,
                             @org.springframework.beans.factory.annotation.Autowired(required = false) com.photoconnect.service.BookingStateMachineService bookingStateMachineService,
                             @org.springframework.beans.factory.annotation.Autowired(required = false) com.photoconnect.service.NotificationService notificationService) {
        this.depositService = depositService;
        this.bookingService = bookingService;
        this.qrCodeService = qrCodeService;
        this.bookingStateMachineService = bookingStateMachineService;
        this.notificationService = notificationService;
    }

    public DepositController(DepositService depositService,
                             BookingService bookingService,
                             DemoQrCodeService qrCodeService,
                             com.photoconnect.service.BookingStateMachineService bookingStateMachineService) {
        this(depositService, bookingService, qrCodeService, bookingStateMachineService, null);
    }

    public DepositController(DepositService depositService,
                             BookingService bookingService,
                             DemoQrCodeService qrCodeService) {
        this(depositService, bookingService, qrCodeService, null, null);
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
                                 HttpSession session,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String redirect = requireCustomer(session);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);
        try {
            DepositViewDto outcome = depositService.processDemoPayment(bookingId, userId, paymentRequest);
            if (outcome != null && outcome.getStatus() == com.photoconnect.entity.DepositStatus.PAID) {
                if (notificationService != null) {
                    try {
                        Booking b = bookingService.getBookingForCustomer(bookingId, userId);
                        if (b != null && b.getPhotographerProfile() != null && b.getPhotographerProfile().getUser() != null) {
                            notificationService.sendNotification(
                                    b.getPhotographerProfile().getUser().getId(),
                                    com.photoconnect.entity.NotificationType.DEPOSIT_CONFIRMED,
                                    "Deposit Paid",
                                    "Deposit payment confirmed for booking #" + bookingId,
                                    bookingId,
                                    "/photographer/bookings/" + bookingId
                            );
                        }
                    } catch (Exception ignored) {}
                }
            }
            return "redirect:/bookings/" + bookingId + "/deposit/result";
        } catch (com.photoconnect.exception.InvalidBookingException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/bookings/" + bookingId + "/deposit/checkout";
        }
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

    @GetMapping("/bookings/{id}/deposit/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(@PathVariable("id") Long bookingId, HttpSession session) {
        String redirect = requireCustomer(session);
        if (redirect != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        Long userId = SessionSecurityUtils.userId(session);
        DepositViewDto deposit = depositService.getCustomerDeposit(bookingId, userId);
        if (deposit == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Deposit not found"));
        }
        return ResponseEntity.ok(Map.of(
                "bookingId", bookingId,
                "status", deposit.getStatus().name(),
                "isPaid", deposit.getStatus() == DepositStatus.PAID,
                "paidAt", deposit.getPaidAt() != null ? deposit.getPaidAt().toString() : "",
                "paymentReference", deposit.getPaymentReference() != null ? deposit.getPaymentReference() : ""
        ));
    }

    private void addPaymentModel(Model model, Booking booking, DepositViewDto deposit) {
        BookingViewDto bookingView = BookingViewDto.from(booking);
        BigDecimal remainingBalance = bookingView.getAgreedPrice().subtract(deposit.getAmount());
        model.addAttribute("booking", bookingView);
        model.addAttribute("deposit", deposit);
        model.addAttribute("remainingBalance", remainingBalance);
        model.addAttribute("bankId", bankId);
        model.addAttribute("accountNo", accountNo);
        model.addAttribute("accountName", accountName);
        model.addAttribute("paymentMode", deposit.getPaymentMode() != null ? deposit.getPaymentMode() : "DEMO");
        model.addAttribute("qrCodeUrl", deposit.getQrCodeUrl());
    }

    private String requireCustomer(HttpSession session) {
        return SessionSecurityUtils.requireRole(session, UserRole.CUSTOMER);
    }
}
