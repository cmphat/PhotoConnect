package com.photoconnect.service;

import com.photoconnect.dto.DemoPaymentRequest;
import com.photoconnect.dto.DepositViewDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.DemoPaymentMethod;
import com.photoconnect.entity.Deposit;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.entity.UserRole;
import com.photoconnect.exception.ErrorCode;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.DepositRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
public class DepositServiceImpl implements DepositService {

    static final BigDecimal DEPOSIT_RATE = new BigDecimal("0.30");
    static final String SUCCESS_CARD = "4242424242424242";
    static final String FAILED_CARD = "4000000000000002";

    private final DepositRepository depositRepository;
    private final BookingRepository bookingRepository;

    @Value("${photoconnect.payment.simulation-enabled:true}")
    private boolean simulationEnabled;

    public DepositServiceImpl(DepositRepository depositRepository, BookingRepository bookingRepository) {
        this.depositRepository = depositRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public DepositViewDto getOrCreateDepositForBooking(Long bookingId, Long customerUserId) {
        requireSimulationEnabled();
        Booking booking = requireOwnedBooking(bookingId, customerUserId);
        Optional<Deposit> existingDeposit = depositRepository.findByBookingIdWithDetails(bookingId);

        if (existingDeposit.isPresent()) {
            Deposit deposit = existingDeposit.get();
            if (deposit.getStatus() == DepositStatus.PAID) {
                return mapToDto(deposit);
            }
            requireAccepted(booking);

            if (deposit.getStatus() == DepositStatus.FAILED || deposit.getStatus() == DepositStatus.CANCELLED) {
                deposit.setStatus(DepositStatus.PENDING);
                deposit.setPaymentReference(generateTransactionReference());
                deposit.setPaymentMethod(null);
                deposit.setFailureReason(null);
                deposit.setPaidAt(null);
                return mapToDto(depositRepository.save(deposit));
            }
            if (deposit.getPaymentReference() == null || deposit.getPaymentReference().isBlank()) {
                deposit.setPaymentReference(generateTransactionReference());
                return mapToDto(depositRepository.save(deposit));
            }
            return mapToDto(deposit);
        }

        requireAccepted(booking);
        BigDecimal depositAmount = booking.getAgreedPrice()
                .multiply(DEPOSIT_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        Deposit deposit = new Deposit(booking, depositAmount);
        deposit.setPaymentReference(generateTransactionReference());
        return mapToDto(depositRepository.save(deposit));
    }

    @Override
    @Transactional(readOnly = true)
    public DepositViewDto getCustomerDeposit(Long bookingId, Long customerUserId) {
        requireOwnedBooking(bookingId, customerUserId);
        return depositRepository.findByBookingIdWithDetails(bookingId)
                .map(this::mapToDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public DepositViewDto processDemoPayment(Long bookingId, Long customerUserId, DemoPaymentRequest request) {
        requireSimulationEnabled();
        Booking booking = requireOwnedBooking(bookingId, customerUserId);
        Deposit deposit = depositRepository.findByBookingIdForUpdate(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Deposit not found for this booking."));

        if (deposit.getStatus() == DepositStatus.PAID) {
            return mapToDto(deposit);
        }

        requireAccepted(booking);
        if (deposit.getStatus() == DepositStatus.PROCESSING) {
            throw new InvalidBookingException(ErrorCode.BOOKING_003_INVALID_STATUS,
                    "This demo payment is already processing.");
        }
        if (deposit.getStatus() == DepositStatus.REFUNDED || deposit.getStatus() == DepositStatus.FORFEITED) {
            throw new InvalidBookingException(ErrorCode.BOOKING_003_INVALID_STATUS,
                    "This deposit cannot be paid in its current state.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Payment details are required.");
        }

        DemoPaymentMethod method = DemoPaymentMethod.fromFormValue(request.getPaymentMethod());
        if (deposit.getStatus() == DepositStatus.FAILED || deposit.getStatus() == DepositStatus.CANCELLED
                || deposit.getPaymentReference() == null || deposit.getPaymentReference().isBlank()) {
            deposit.setPaymentReference(generateTransactionReference());
        }

        deposit.setPaymentMethod(method);
        deposit.setFailureReason(null);
        deposit.setPaidAt(null);
        deposit.setStatus(DepositStatus.PROCESSING);
        depositRepository.saveAndFlush(deposit);

        if (method == DemoPaymentMethod.DEMO_QR) {
            completeSuccessfully(deposit);
        } else {
            applyDemoCardOutcome(deposit, request);
        }

        return mapToDto(depositRepository.save(deposit));
    }

    @Override
    @Transactional
    public DepositViewDto cancelDemoPayment(Long bookingId, Long customerUserId) {
        requireSimulationEnabled();
        Booking booking = requireOwnedBooking(bookingId, customerUserId);
        Deposit deposit = depositRepository.findByBookingIdForUpdate(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Deposit not found for this booking."));

        if (deposit.getStatus() == DepositStatus.PAID) {
            return mapToDto(deposit);
        }
        requireAccepted(booking);
        if (deposit.getStatus() == DepositStatus.PROCESSING) {
            throw new InvalidBookingException(ErrorCode.BOOKING_003_INVALID_STATUS,
                    "A processing payment cannot be cancelled.");
        }
        if (deposit.getStatus() != DepositStatus.PENDING) {
            throw new InvalidBookingException(ErrorCode.BOOKING_003_INVALID_STATUS,
                    "Only a pending demo payment can be cancelled.");
        }

        if (deposit.getPaymentReference() == null || deposit.getPaymentReference().isBlank()) {
            deposit.setPaymentReference(generateTransactionReference());
        }
        deposit.setStatus(DepositStatus.CANCELLED);
        deposit.setPaymentMethod(null);
        deposit.setFailureReason("Payment cancelled by customer.");
        deposit.setPaidAt(null);
        return mapToDto(depositRepository.save(deposit));
    }

    /** Retained for TASK-015 service compatibility; no longer exposed as a developer UI endpoint. */
    @Override
    @Deprecated(forRemoval = false)
    @Transactional
    public DepositViewDto simulateSuccessfulPayment(Long bookingId, Long customerUserId) {
        DemoPaymentRequest request = new DemoPaymentRequest();
        request.setPaymentMethod(DemoPaymentMethod.DEMO_QR.name());
        return processDemoPayment(bookingId, customerUserId, request);
    }

    private Booking requireOwnedBooking(Long bookingId, Long customerUserId) {
        if (bookingId == null || customerUserId == null) {
            throw new UnauthorizedException("A signed-in customer is required.");
        }
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking not found."));
        if (booking.getCustomer() == null || !customerUserId.equals(booking.getCustomer().getId())) {
            throw new UnauthorizedException("You do not own this booking.");
        }
        if (booking.getCustomer().getRole() != UserRole.CUSTOMER) {
            throw new UnauthorizedException("Only the booking customer can use demo checkout.");
        }
        return booking;
    }

    private void requireAccepted(Booking booking) {
        if (booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new InvalidBookingException(ErrorCode.BOOKING_003_INVALID_STATUS,
                    "Deposit payment is available only for an ACCEPTED booking.");
        }
    }

    private void requireSimulationEnabled() {
        if (!simulationEnabled) {
            throw new IllegalStateException("Demo payment is disabled in this environment.");
        }
    }

    private void applyDemoCardOutcome(Deposit deposit, DemoPaymentRequest request) {
        String normalizedNumber = normalizeCardNumber(request.getCardNumber());
        boolean validMetadata = isValidCardholder(request.getCardholderName())
                && isValidExpiry(request.getExpiry())
                && request.getCvv() != null
                && request.getCvv().matches("\\d{3,4}");

        if (validMetadata && SUCCESS_CARD.equals(normalizedNumber)) {
            completeSuccessfully(deposit);
            return;
        }

        deposit.setStatus(DepositStatus.FAILED);
        deposit.setPaidAt(null);
        if (validMetadata && FAILED_CARD.equals(normalizedNumber)) {
            deposit.setFailureReason("The demo issuer declined this test card.");
        } else if (!validMetadata) {
            deposit.setFailureReason("The demo card details could not be validated.");
        } else {
            deposit.setFailureReason("Use one of the documented PhotoConnect demo card numbers.");
        }
    }

    private void completeSuccessfully(Deposit deposit) {
        deposit.setStatus(DepositStatus.PAID);
        deposit.setPaidAt(LocalDateTime.now());
        deposit.setFailureReason(null);
    }

    private boolean isValidCardholder(String value) {
        return value != null && !value.isBlank() && value.trim().length() <= 120;
    }

    private boolean isValidExpiry(String value) {
        if (value == null || !value.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            return false;
        }
        int month = Integer.parseInt(value.substring(0, 2));
        int year = 2000 + Integer.parseInt(value.substring(3));
        LocalDate endOfMonth = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
        return !endOfMonth.isBefore(LocalDate.now());
    }

    private String normalizeCardNumber(String value) {
        return value == null ? "" : value.replaceAll("[\\s-]", "");
    }

    private String generateTransactionReference() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "PC-" + date + "-" + random;
    }

    private DepositViewDto mapToDto(Deposit deposit) {
        DepositViewDto dto = new DepositViewDto();
        dto.setId(deposit.getId());
        dto.setBookingId(deposit.getBooking().getId());
        dto.setAmount(deposit.getAmount());
        dto.setStatus(deposit.getStatus());
        dto.setPaymentReference(deposit.getPaymentReference());
        dto.setPaymentMethod(deposit.getPaymentMethod());
        dto.setFailureReason(deposit.getFailureReason());
        dto.setPaidAt(deposit.getPaidAt());
        return dto;
    }
}
