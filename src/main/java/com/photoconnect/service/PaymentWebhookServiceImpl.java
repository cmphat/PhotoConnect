package com.photoconnect.service;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.Deposit;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.entity.NotificationType;
import com.photoconnect.entity.PaymentTransaction;
import com.photoconnect.payment.PaymentGatewayAdapter;
import com.photoconnect.payment.PaymentGatewayFactory;
import com.photoconnect.payment.WebhookVerificationResult;
import com.photoconnect.repository.DepositRepository;
import com.photoconnect.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentWebhookServiceImpl implements PaymentWebhookService {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookServiceImpl.class);

    private final PaymentGatewayFactory paymentGatewayFactory;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final DepositRepository depositRepository;
    private final BookingStateMachineService bookingStateMachineService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public PaymentWebhookServiceImpl(
            PaymentGatewayFactory paymentGatewayFactory,
            PaymentTransactionRepository paymentTransactionRepository,
            DepositRepository depositRepository,
            @Autowired(required = false) BookingStateMachineService bookingStateMachineService,
            @Autowired(required = false) NotificationService notificationService,
            @Autowired(required = false) SimpMessagingTemplate messagingTemplate) {
        this.paymentGatewayFactory = paymentGatewayFactory;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.depositRepository = depositRepository;
        this.bookingStateMachineService = bookingStateMachineService;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public WebhookVerificationResult processWebhook(String providerName, String rawBody, Map<String, String> headers) {
        log.info("Processing incoming payment webhook from provider: {}", providerName);

        if (!paymentGatewayFactory.isRealModeActive()) {
            return WebhookVerificationResult.invalid(providerName,
                    "Real payment webhooks are disabled or the provider configuration is incomplete.", rawBody);
        }

        PaymentGatewayAdapter adapter = paymentGatewayFactory.getAdapterByName(providerName);
        if (adapter == null) {
            log.error("Unknown payment provider for webhook: {}", providerName);
            return WebhookVerificationResult.invalid(providerName, "Unknown payment provider: " + providerName, rawBody);
        }

        // 1. Authenticate & Verify Webhook Signature
        WebhookVerificationResult verification = adapter.verifyAndParseWebhook(rawBody, headers);
        if (!verification.isValid()) {
            log.warn("Invalid webhook signature or payload from {}: {}", providerName, verification.getFailureReason());
            return verification;
        }

        String externalTxId = verification.getExternalTransactionId();
        if (externalTxId == null || externalTxId.isBlank()) {
            return WebhookVerificationResult.invalid(providerName, "Missing external transaction ID", rawBody);
        }

        // 2. Prevent Replay Attack / Deduplicate
        if (paymentTransactionRepository.existsByProviderNameAndExternalTransactionId(providerName, externalTxId)) {
            log.info("Duplicate webhook received: provider={}, externalId={}. Ignoring gracefully.",
                    providerName, externalTxId);
            return verification;
        }

        // 3. Locate Target Deposit
        Optional<Deposit> depositOpt = Optional.empty();
        if (verification.getOrderCode() != null) {
            depositOpt = depositRepository.findByProviderOrderCodeWithDetails(verification.getOrderCode());
        }
        if (depositOpt.isEmpty() && verification.getPaymentReference() != null && !verification.getPaymentReference().isBlank()) {
            depositOpt = depositRepository.findByPaymentReferenceWithDetails(verification.getPaymentReference());
        }

        if (depositOpt.isEmpty()) {
            log.warn("Deposit not found for provider={} ref={} orderCode={}",
                    providerName, verification.getPaymentReference(), verification.getOrderCode());
            return WebhookVerificationResult.invalid(providerName, "Deposit not found for reference", rawBody);
        }

        Deposit deposit = depositOpt.get();
        deposit = depositRepository.findByIdForUpdate(deposit.getId()).orElseThrow();
        Booking booking = deposit.getBooking();

        if (paymentTransactionRepository.existsByProviderNameAndExternalTransactionId(providerName, externalTxId)) {
            log.info("Duplicate webhook received after deposit lock: provider={}, externalId={}", providerName, externalTxId);
            return verification;
        }
        if (deposit.getProviderName() == null || !deposit.getProviderName().equalsIgnoreCase(providerName)) {
            return WebhookVerificationResult.invalid(providerName, "Provider does not match the checkout provider", rawBody);
        }
        if (verification.getOrderCode() != null && deposit.getProviderOrderCode() != null
                && !verification.getOrderCode().equals(deposit.getProviderOrderCode())) {
            return WebhookVerificationResult.invalid(providerName, "Order code does not match deposit", rawBody);
        }
        if (verification.getPaymentReference() != null && !verification.getPaymentReference().isBlank()
                && deposit.getPaymentReference() != null
                && !verification.getPaymentReference().equals(deposit.getPaymentReference())) {
            return WebhookVerificationResult.invalid(providerName, "Payment reference does not match deposit", rawBody);
        }
        if (deposit.getExpiresAt() != null && deposit.getExpiresAt().isBefore(LocalDateTime.now())) {
            return WebhookVerificationResult.invalid(providerName, "Payment request has expired", rawBody);
        }
        if (deposit.getStatus() != DepositStatus.PENDING && deposit.getStatus() != DepositStatus.PROCESSING) {
            return WebhookVerificationResult.invalid(providerName, "Deposit is not payable in its current state", rawBody);
        }
        if (booking.getStatus() != BookingStatus.ACCEPTED && booking.getStatus() != BookingStatus.DEPOSIT_PENDING) {
            return WebhookVerificationResult.invalid(providerName, "Booking is not payable in its current state", rawBody);
        }

        // 4. Validate Amount and Currency
        if (!"VND".equalsIgnoreCase(verification.getCurrency())) {
            return WebhookVerificationResult.invalid(providerName, "Unsupported currency: " + verification.getCurrency(), rawBody);
        }

        BigDecimal expectedAmount = deposit.getAmount();
        BigDecimal receivedAmount = verification.getAmount();
        if (receivedAmount == null || expectedAmount.compareTo(receivedAmount) != 0) {
            log.error("Payment amount mismatch for deposit #{}: expected={}, received={}",
                    deposit.getId(), expectedAmount, receivedAmount);
            return WebhookVerificationResult.invalid(providerName, "Amount mismatch", rawBody);
        }

        // 5. Validate Provider Transaction Success
        if (!verification.isTransactionSuccessful()) {
            log.warn("Provider reported failed transaction: {}", verification.getFailureReason());
            return verification;
        }

        // 6. Record Successful Audit Transaction (Enforces UQ_payment_transactions_provider_ext_id)
        PaymentTransaction tx = recordAuditTransaction(deposit, providerName, externalTxId, receivedAmount,
                verification.getCurrency(), verification.getPaymentReference(), rawBody, "SUCCESS");

        // 7. Update Deposit Status
        deposit.setStatus(DepositStatus.PAID);
        deposit.setPaidAt(LocalDateTime.now());
        deposit.setProviderName(providerName);
        deposit.setProviderTransactionId(externalTxId);
        depositRepository.saveAndFlush(deposit);

        // 8. Transition Booking to CONFIRMED
        if (booking.getStatus() == BookingStatus.ACCEPTED || booking.getStatus() == BookingStatus.DEPOSIT_PENDING) {
            if (bookingStateMachineService != null) {
                bookingStateMachineService.transition(
                        booking.getId(),
                        BookingStatus.CONFIRMED,
                        booking.getCustomer().getId(),
                        "VietQR 30% deposit payment confirmed by " + providerName + " (Tx: " + externalTxId + ")"
                );
            } else {
                booking.setStatus(BookingStatus.CONFIRMED);
            }
        }

        // 9. Dispatch STOMP & Push Notifications
        try {
            if (notificationService != null) {
                if (booking.getPhotographerProfile() != null && booking.getPhotographerProfile().getUser() != null) {
                    notificationService.sendNotification(
                            booking.getPhotographerProfile().getUser().getId(),
                            NotificationType.DEPOSIT_CONFIRMED,
                            "Đặt cọc thành công",
                            "Khách hàng " + booking.getCustomer().getFullName() + " đã thanh toán cọc 30% cho lịch chụp #" + booking.getId(),
                            booking.getId(),
                            "/photographer/bookings/" + booking.getId()
                    );
                }
                notificationService.sendNotification(
                        booking.getCustomer().getId(),
                        NotificationType.DEPOSIT_CONFIRMED,
                        "Xác nhận đặt cọc",
                        "Thanh toán cọc 30% cho lịch chụp #" + booking.getId() + " đã được xác nhận thành công.",
                        booking.getId(),
                        "/bookings/" + booking.getId() + "/deposit/receipt"
                );
            }

            if (messagingTemplate != null) {
                messagingTemplate.convertAndSend("/topic/bookings/" + booking.getId() + "/payment", Map.of(
                        "bookingId", booking.getId(),
                        "status", BookingStatus.CONFIRMED.name(),
                        "depositStatus", DepositStatus.PAID.name(),
                        "amount", deposit.getAmount().toPlainString(),
                        "paidAt", deposit.getPaidAt().toString()
                ));
            }
        } catch (Exception ex) {
            log.warn("Failed to dispatch post-payment notifications: {}", ex.getMessage());
        }

        log.info("Deposit #{} and Booking #{} successfully CONFIRMED via provider {}",
                deposit.getId(), booking.getId(), providerName);
        return verification;
    }

    private PaymentTransaction recordAuditTransaction(Deposit deposit, String providerName,
                                                     String externalTxId, BigDecimal amount,
                                                     String currency, String paymentRef,
                                                     String rawPayload, String status) {
        PaymentTransaction tx = new PaymentTransaction(
                deposit,
                providerName,
                externalTxId,
                amount,
                currency,
                paymentRef != null ? paymentRef : deposit.getPaymentReference(),
                rawPayload,
                status
        );
        return paymentTransactionRepository.saveAndFlush(tx);
    }
}
