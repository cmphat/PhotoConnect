package com.photoconnect.service;

import com.photoconnect.dto.AdminDashboardStatsDto;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.UserRole;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.DepositRepository;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.ReviewRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PhotographerProfileRepository photographerProfileRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private DepositRepository depositRepository;

    private AdminDashboardService adminDashboardService;

    @BeforeEach
    void setUp() {
        adminDashboardService = new AdminDashboardServiceImpl(
                userRepository,
                photographerProfileRepository,
                bookingRepository,
                reviewRepository,
                depositRepository
        );
    }

    @Test
    void getDashboardStats_shouldAggregateAllMetricsAccurately() {
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByRole(UserRole.CUSTOMER)).thenReturn(75L);
        when(userRepository.countByRole(UserRole.PHOTOGRAPHER)).thenReturn(24L);

        when(photographerProfileRepository.countByVerificationStatus(PhotographerVerificationStatus.PENDING))
                .thenReturn(4L);
        when(photographerProfileRepository.countByVerificationStatus(PhotographerVerificationStatus.APPROVED))
                .thenReturn(20L);

        when(bookingRepository.count()).thenReturn(50L);
        when(bookingRepository.countByStatus(BookingStatus.PENDING)).thenReturn(10L);
        when(bookingRepository.countByStatus(BookingStatus.COMPLETED)).thenReturn(30L);

        when(reviewRepository.count()).thenReturn(25L);

        when(depositRepository.countByStatus(DepositStatus.PAID)).thenReturn(18L);
        when(depositRepository.sumAmountByStatus(DepositStatus.PAID)).thenReturn(new BigDecimal("9000000.00"));

        AdminDashboardStatsDto stats = adminDashboardService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(100L, stats.getTotalUsers());
        assertEquals(75L, stats.getTotalCustomers());
        assertEquals(24L, stats.getTotalPhotographers());
        assertEquals(4L, stats.getPendingPhotographerApprovals());
        assertEquals(20L, stats.getApprovedPhotographers());
        assertEquals(50L, stats.getTotalBookings());
        assertEquals(10L, stats.getPendingBookings());
        assertEquals(30L, stats.getCompletedBookings());
        assertEquals(25L, stats.getTotalReviews());
        assertEquals(18L, stats.getPaidDepositsCount());
        assertEquals(new BigDecimal("9000000.00"), stats.getSimulatedDepositAmount());

        verify(userRepository).count();
        verify(userRepository).countByRole(UserRole.CUSTOMER);
        verify(userRepository).countByRole(UserRole.PHOTOGRAPHER);
        verify(photographerProfileRepository).countByVerificationStatus(PhotographerVerificationStatus.PENDING);
        verify(photographerProfileRepository).countByVerificationStatus(PhotographerVerificationStatus.APPROVED);
        verify(bookingRepository).count();
        verify(bookingRepository).countByStatus(BookingStatus.PENDING);
        verify(bookingRepository).countByStatus(BookingStatus.COMPLETED);
        verify(reviewRepository).count();
        verify(depositRepository).countByStatus(DepositStatus.PAID);
        verify(depositRepository).sumAmountByStatus(DepositStatus.PAID);
    }
}
