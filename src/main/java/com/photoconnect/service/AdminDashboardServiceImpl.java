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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final PhotographerProfileRepository photographerProfileRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final DepositRepository depositRepository;
    private final com.photoconnect.repository.UserReportRepository userReportRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public AdminDashboardServiceImpl(UserRepository userRepository,
                                     PhotographerProfileRepository photographerProfileRepository,
                                     BookingRepository bookingRepository,
                                     ReviewRepository reviewRepository,
                                     DepositRepository depositRepository,
                                     @org.springframework.beans.factory.annotation.Autowired(required = false) com.photoconnect.repository.UserReportRepository userReportRepository) {
        this.userRepository = userRepository;
        this.photographerProfileRepository = photographerProfileRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.depositRepository = depositRepository;
        this.userReportRepository = userReportRepository;
    }

    public AdminDashboardServiceImpl(UserRepository userRepository,
                                     PhotographerProfileRepository photographerProfileRepository,
                                     BookingRepository bookingRepository,
                                     ReviewRepository reviewRepository,
                                     DepositRepository depositRepository) {
        this(userRepository, photographerProfileRepository, bookingRepository, reviewRepository, depositRepository, null);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStatsDto getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalCustomers = userRepository.countByRole(UserRole.CUSTOMER);
        long totalPhotographers = userRepository.countByRole(UserRole.PHOTOGRAPHER);

        long pendingApprovals = photographerProfileRepository
                .countByVerificationStatus(PhotographerVerificationStatus.PENDING);
        long approvedPhotographers = photographerProfileRepository
                .countByVerificationStatus(PhotographerVerificationStatus.APPROVED);

        long totalBookings = bookingRepository.count();
        long pendingBookings = bookingRepository.countByStatus(BookingStatus.PENDING);
        long completedBookings = bookingRepository.countByStatus(BookingStatus.COMPLETED);

        long totalReviews = reviewRepository.count();

        long paidDepositsCount = depositRepository.countByStatus(DepositStatus.PAID);
        BigDecimal simulatedDepositAmount = depositRepository.sumAmountByStatus(DepositStatus.PAID);

        AdminDashboardStatsDto dto = new AdminDashboardStatsDto(
                totalUsers,
                totalCustomers,
                totalPhotographers,
                pendingApprovals,
                approvedPhotographers,
                totalBookings,
                pendingBookings,
                completedBookings,
                totalReviews,
                paidDepositsCount,
                simulatedDepositAmount
        );

        dto.setDisputedBookings(bookingRepository.countByStatus(BookingStatus.DISPUTED));
        dto.setCancelledBookings(bookingRepository.countByStatus(BookingStatus.CANCELLED));

        BigDecimal totalVolume = bookingRepository.calculateTotalPlatformBookingVolume();
        BigDecimal completedRev = bookingRepository.calculateTotalPlatformCompletedRevenue();
        dto.setTotalPlatformBookingVolume(totalVolume != null ? totalVolume : BigDecimal.ZERO);
        dto.setCompletedRevenue(completedRev != null ? completedRev : BigDecimal.ZERO);
        dto.setPlatformFeeRevenue(dto.getCompletedRevenue().multiply(new BigDecimal("0.10")));

        BigDecimal refundedAmt = depositRepository.sumAmountByStatus(DepositStatus.REFUNDED);
        BigDecimal forfeitedAmt = depositRepository.sumAmountByStatus(DepositStatus.FORFEITED);
        dto.setRefundedDepositsAmount(refundedAmt != null ? refundedAmt : BigDecimal.ZERO);
        dto.setForfeitedDepositsAmount(forfeitedAmt != null ? forfeitedAmt : BigDecimal.ZERO);
        dto.setRefundedDepositsCount(depositRepository.countByStatus(DepositStatus.REFUNDED));
        dto.setForfeitedDepositsCount(depositRepository.countByStatus(DepositStatus.FORFEITED));

        if (userReportRepository != null) {
            dto.setPendingReportsCount(userReportRepository.countByStatus(com.photoconnect.entity.ReportStatus.PENDING));
        }

        return dto;
    }
}
