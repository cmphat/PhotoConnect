package com.photoconnect.service;

import com.photoconnect.dto.PhotographerFeeSummaryDto;
import com.photoconnect.dto.PlatformFeeDto;
import com.photoconnect.entity.*;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.PlatformFeeRepository;
import com.photoconnect.repository.SystemSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Platform Fee Ledger Service Unit Tests")
class PlatformFeeServiceTest {

    @Mock
    private PlatformFeeRepository platformFeeRepository;

    @Mock
    private SystemSettingRepository systemSettingRepository;

    @Mock
    private BookingRepository bookingRepository;

    private PlatformFeeService platformFeeService;

    private User photographerUser;
    private PhotographerProfile photographerProfile;
    private Booking completedBooking;

    @BeforeEach
    void setUp() {
        platformFeeService = new PlatformFeeServiceImpl(
                platformFeeRepository,
                systemSettingRepository,
                bookingRepository
        );

        photographerUser = new User();
        photographerUser.setId(10L);
        photographerUser.setFullName("Le Photographer");
        photographerUser.setRole(UserRole.PHOTOGRAPHER);

        photographerProfile = new PhotographerProfile();
        photographerProfile.setId(100L);
        photographerProfile.setUser(photographerUser);
        photographerProfile.setDisplayName("Le Studio");

        completedBooking = new Booking();
        completedBooking.setId(555L);
        completedBooking.setStatus(BookingStatus.COMPLETED);
        completedBooking.setAgreedPrice(new BigDecimal("2000000"));
        completedBooking.setPhotographerProfile(photographerProfile);
    }

    @Test
    @DisplayName("Recording fee for completed booking calculates 10% fee and marks as UNPAID")
    void recordFee_calculatesTenPercent_unpaid() {
        when(platformFeeRepository.findByBookingId(555L)).thenReturn(Optional.empty());
        when(systemSettingRepository.findByKey(PlatformFeeServiceImpl.SETTING_KEY_FEE_RATE))
                .thenReturn(Optional.of(new SystemSetting(PlatformFeeServiceImpl.SETTING_KEY_FEE_RATE, "0.10", "10% rate")));
        when(platformFeeRepository.save(any(PlatformFee.class))).thenAnswer(inv -> {
            PlatformFee saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        PlatformFee fee = platformFeeService.recordFeeForCompletedBooking(completedBooking);

        assertThat(fee.getBookingAmount()).isEqualByComparingTo(new BigDecimal("2000000"));
        assertThat(fee.getFeeRate()).isEqualByComparingTo(new BigDecimal("0.10"));
        assertThat(fee.getFeeAmount()).isEqualByComparingTo(new BigDecimal("200000"));
        assertThat(fee.getPaidAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(fee.getOutstandingAmount()).isEqualByComparingTo(new BigDecimal("200000"));
        assertThat(fee.getStatus()).isEqualTo(PlatformFeeStatus.UNPAID);
    }

    @Test
    @DisplayName("Idempotency check: duplicate completion callback does not record duplicate fee")
    void recordFee_idempotent_skipsDuplicate() {
        PlatformFee existingFee = new PlatformFee(
                completedBooking,
                photographerProfile,
                photographerUser,
                new BigDecimal("2000000"),
                new BigDecimal("0.10"),
                new BigDecimal("200000")
        );
        existingFee.setId(99L);
        when(platformFeeRepository.findByBookingId(555L)).thenReturn(Optional.of(existingFee));

        PlatformFee fee = platformFeeService.recordFeeForCompletedBooking(completedBooking);

        assertThat(fee.getId()).isEqualTo(99L);
        verify(platformFeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cannot accrue platform fee on non-COMPLETED bookings")
    void recordFee_nonCompleted_throwsException() {
        completedBooking.setStatus(BookingStatus.PENDING);

        assertThatThrownBy(() -> platformFeeService.recordFeeForCompletedBooking(completedBooking))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("COMPLETED");
    }

    @Test
    @DisplayName("Photographer paying full outstanding fee marks status as PAID")
    void processPayment_fullPayment_marksPaid() {
        PlatformFee fee = new PlatformFee(
                completedBooking,
                photographerProfile,
                photographerUser,
                new BigDecimal("2000000"),
                new BigDecimal("0.10"),
                new BigDecimal("200000")
        );
        fee.setId(1L);
        fee.setOutstandingAmount(new BigDecimal("200000"));
        fee.setPaidAmount(BigDecimal.ZERO);
        fee.setStatus(PlatformFeeStatus.UNPAID);

        when(platformFeeRepository.findById(1L)).thenReturn(Optional.of(fee));
        when(platformFeeRepository.save(any(PlatformFee.class))).thenAnswer(inv -> inv.getArgument(0));

        PlatformFeeDto dto = platformFeeService.processPhotographerFeePayment(10L, 1L, new BigDecimal("200000"), "DEMO_PAYMENT");

        assertThat(dto.getStatus()).isEqualTo(PlatformFeeStatus.PAID);
        assertThat(dto.getPaidAmount()).isEqualByComparingTo(new BigDecimal("200000"));
        assertThat(dto.getOutstandingAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.getTransactionCode()).startsWith("PF-PAY-");
    }

    @Test
    @DisplayName("Photographer cannot pay fee for another photographer")
    void processPayment_crossPhotographerSecurity_throwsException() {
        PlatformFee fee = new PlatformFee(
                completedBooking,
                photographerProfile,
                photographerUser,
                new BigDecimal("2000000"),
                new BigDecimal("0.10"),
                new BigDecimal("200000")
        );
        fee.setId(1L);

        when(platformFeeRepository.findById(1L)).thenReturn(Optional.of(fee));

        assertThatThrownBy(() -> platformFeeService.processPhotographerFeePayment(999L, 1L, new BigDecimal("100000"), "DEMO"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    @DisplayName("Admin can update platform fee rate within valid bounds")
    void updateFeeRate_validRate_persisted() {
        when(systemSettingRepository.findByKey(PlatformFeeServiceImpl.SETTING_KEY_FEE_RATE))
                .thenReturn(Optional.of(new SystemSetting(PlatformFeeServiceImpl.SETTING_KEY_FEE_RATE, "0.10", "desc")));

        platformFeeService.updateFeeRate(new BigDecimal("0.15"), 1L);

        verify(systemSettingRepository).save(any(SystemSetting.class));
    }
}
