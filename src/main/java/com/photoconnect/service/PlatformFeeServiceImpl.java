package com.photoconnect.service;

import com.photoconnect.dto.PhotographerFeeSummaryDto;
import com.photoconnect.dto.PlatformFeeDto;
import com.photoconnect.entity.*;
import com.photoconnect.exception.ResourceNotFoundException;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.PlatformFeeRepository;
import com.photoconnect.repository.SystemSettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class PlatformFeeServiceImpl implements PlatformFeeService {

    private static final Logger log = LoggerFactory.getLogger(PlatformFeeServiceImpl.class);
    public static final String SETTING_KEY_FEE_RATE = "PLATFORM_FEE_RATE";
    public static final BigDecimal DEFAULT_FEE_RATE = new BigDecimal("0.10");

    private final PlatformFeeRepository platformFeeRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final BookingRepository bookingRepository;
    private OperationalAuditService auditService;

    @Autowired
    public PlatformFeeServiceImpl(PlatformFeeRepository platformFeeRepository,
                                  SystemSettingRepository systemSettingRepository,
                                  BookingRepository bookingRepository,
                                  @Autowired(required = false) OperationalAuditService auditService) {
        this.platformFeeRepository = platformFeeRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.bookingRepository = bookingRepository;
        this.auditService = auditService;
    }

    public PlatformFeeServiceImpl(PlatformFeeRepository platformFeeRepository,
                                  SystemSettingRepository systemSettingRepository,
                                  BookingRepository bookingRepository) {
        this(platformFeeRepository, systemSettingRepository, bookingRepository, null);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCurrentFeeRate() {
        return systemSettingRepository.findByKey(SETTING_KEY_FEE_RATE)
                .map(setting -> {
                    try {
                        return new BigDecimal(setting.getValue());
                    } catch (Exception e) {
                        return DEFAULT_FEE_RATE;
                    }
                })
                .orElse(DEFAULT_FEE_RATE);
    }

    @Override
    public void updateFeeRate(BigDecimal newRate, Long adminUserId) {
        if (newRate == null || newRate.compareTo(BigDecimal.ZERO) < 0 || newRate.compareTo(new BigDecimal("0.50")) > 0) {
            throw new IllegalArgumentException("Platform fee rate must be between 0% and 50% (0.00 to 0.50).");
        }
        SystemSetting setting = systemSettingRepository.findByKey(SETTING_KEY_FEE_RATE)
                .orElse(new SystemSetting(SETTING_KEY_FEE_RATE, "0.10", "Default platform fee rate applied to completed bookings"));
        String oldRate = setting.getValue();
        setting.setValue(newRate.setScale(4, RoundingMode.HALF_UP).toPlainString());
        systemSettingRepository.save(setting);

        if (auditService != null && adminUserId != null) {
            auditService.record(adminUserId, "SYSTEM_CONFIG_UPDATE", "SystemSetting", 0L,
                    AuditOutcome.SUCCESS, "Updated platform fee rate from " + oldRate + " to " + setting.getValue());
        }
        log.info("Admin [{}] updated platform fee rate from [{}] to [{}]", adminUserId, oldRate, setting.getValue());
    }

    @Override
    public PlatformFee recordFeeForCompletedBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null when recording platform fee.");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException("Platform fee can only be levied on COMPLETED bookings. Current status: " + booking.getStatus());
        }

        // Idempotency: verify if fee already exists for this booking
        Optional<PlatformFee> existing = platformFeeRepository.findByBookingId(booking.getId());
        if (existing.isPresent()) {
            log.info("Platform fee already exists for booking [{}]. Skipping duplicate ledger entry.", booking.getId());
            return existing.get();
        }

        PhotographerProfile profile = booking.getPhotographerProfile();
        if (profile == null || profile.getUser() == null) {
            throw new IllegalStateException("Booking [" + booking.getId() + "] lacks associated photographer profile/user.");
        }

        BigDecimal rate = getCurrentFeeRate();
        BigDecimal bookingAmount = booking.getAgreedPrice() != null ? booking.getAgreedPrice() : BigDecimal.ZERO;
        BigDecimal feeAmount = bookingAmount.multiply(rate).setScale(0, RoundingMode.HALF_UP);

        PlatformFee fee = new PlatformFee(
                booking,
                profile,
                profile.getUser(),
                bookingAmount,
                rate,
                feeAmount
        );
        fee.setStatus(PlatformFeeStatus.UNPAID);
        fee.setNotes("10% platform fee accrued upon booking completion.");

        PlatformFee saved = platformFeeRepository.save(fee);
        log.info("Recorded platform fee [{}] for booking [{}] (amount: {} VND, rate: {})",
                saved.getId(), booking.getId(), feeAmount, rate);
        return saved;
    }

    @Override
    public void cancelFeeForBooking(Booking booking, String reason) {
        if (booking == null) return;
        platformFeeRepository.findByBookingId(booking.getId()).ifPresent(fee -> {
            if (fee.getStatus() == PlatformFeeStatus.UNPAID || fee.getStatus() == PlatformFeeStatus.PENDING) {
                fee.setStatus(PlatformFeeStatus.CANCELLED);
                fee.setNotes("Cancelled: " + (reason != null ? reason : "Booking cancelled or refunded."));
                platformFeeRepository.save(fee);
                log.info("Platform fee [{}] for booking [{}] marked as CANCELLED.", fee.getId(), booking.getId());
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PhotographerFeeSummaryDto getPhotographerSummary(Long photographerUserId) {
        PhotographerFeeSummaryDto summary = new PhotographerFeeSummaryDto();
        BigDecimal currentRate = getCurrentFeeRate();
        summary.setCurrentFeeRatePercent((currentRate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString()) + "%");

        BigDecimal grossEarnings = bookingRepository.sumAgreedPriceByPhotographerUserId(photographerUserId);
        summary.setGrossEarnings(grossEarnings != null ? grossEarnings : BigDecimal.ZERO);

        BigDecimal feesIncurred = platformFeeRepository.sumFeeAmountByPhotographerUserId(photographerUserId);
        summary.setFeesIncurred(feesIncurred != null ? feesIncurred : BigDecimal.ZERO);

        BigDecimal feesPaid = platformFeeRepository.sumPaidAmountByPhotographerUserId(photographerUserId);
        summary.setFeesPaid(feesPaid != null ? feesPaid : BigDecimal.ZERO);

        BigDecimal feesOutstanding = platformFeeRepository.sumOutstandingAmountByPhotographerUserId(photographerUserId);
        summary.setFeesOutstanding(feesOutstanding != null ? feesOutstanding : BigDecimal.ZERO);

        BigDecimal netExpected = summary.getGrossEarnings().subtract(summary.getFeesIncurred());
        summary.setNetEarningsExpected(netExpected.compareTo(BigDecimal.ZERO) > 0 ? netExpected : BigDecimal.ZERO);

        List<PlatformFee> fees = platformFeeRepository.findByPhotographerUserIdWithDetails(photographerUserId);
        summary.setFees(fees.stream().map(PlatformFeeDto::fromEntity).toList());

        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlatformFeeDto> listAllFees(PlatformFeeStatus statusFilter) {
        List<PlatformFee> fees = (statusFilter != null)
                ? platformFeeRepository.findByStatusWithDetails(statusFilter)
                : platformFeeRepository.findAllWithDetails();
        return fees.stream().map(PlatformFeeDto::fromEntity).toList();
    }

    @Override
    public PlatformFeeDto processPhotographerFeePayment(Long photographerUserId, Long feeId, BigDecimal amount, String paymentMethod) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0.");
        }

        PlatformFee fee = platformFeeRepository.findById(feeId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform fee record not found: " + feeId));

        if (!fee.getPhotographerUser().getId().equals(photographerUserId)) {
            throw new UnauthorizedException("You are not authorized to pay platform fee for another photographer.");
        }

        if (fee.getStatus() == PlatformFeeStatus.PAID || fee.getStatus() == PlatformFeeStatus.CANCELLED) {
            throw new IllegalStateException("Fee is already " + fee.getStatus() + " and cannot receive further payments.");
        }

        BigDecimal newPaid = fee.getPaidAmount().add(amount);
        BigDecimal newOutstanding = fee.getFeeAmount().subtract(newPaid);

        if (newOutstanding.compareTo(BigDecimal.ZERO) <= 0) {
            fee.setPaidAmount(fee.getFeeAmount());
            fee.setOutstandingAmount(BigDecimal.ZERO);
            fee.setStatus(PlatformFeeStatus.PAID);
            fee.setPaidAt(LocalDateTime.now());
        } else {
            fee.setPaidAmount(newPaid);
            fee.setOutstandingAmount(newOutstanding);
            fee.setStatus(PlatformFeeStatus.PARTIALLY_PAID);
        }

        String txCode = "PF-PAY-" + System.currentTimeMillis();
        fee.setTransactionCode(txCode);
        fee.setPaymentMethod(paymentMethod != null ? paymentMethod : "DEMO_PAYMENT");
        fee.setNotes("Payment of " + amount + " VND processed via " + fee.getPaymentMethod());

        PlatformFee saved = platformFeeRepository.save(fee);
        log.info("Processed platform fee payment [{}] by photographer [{}]: amount = {} VND, status = {}",
                saved.getId(), photographerUserId, amount, saved.getStatus());
        return PlatformFeeDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAdminFeeStats() {
        Map<String, Object> stats = new HashMap<>();
        BigDecimal totalIncurred = platformFeeRepository.sumTotalFeeAmount();
        BigDecimal totalPaid = platformFeeRepository.sumTotalPaidAmount();
        BigDecimal totalOutstanding = platformFeeRepository.sumTotalOutstandingAmount();
        long photographersWithDebt = platformFeeRepository.countPhotographersWithOutstandingFee();

        stats.put("totalIncurred", totalIncurred != null ? totalIncurred : BigDecimal.ZERO);
        stats.put("totalPaid", totalPaid != null ? totalPaid : BigDecimal.ZERO);
        stats.put("totalOutstanding", totalOutstanding != null ? totalOutstanding : BigDecimal.ZERO);
        stats.put("photographersWithDebt", photographersWithDebt);
        stats.put("currentFeeRate", getCurrentFeeRate());
        return stats;
    }
}
