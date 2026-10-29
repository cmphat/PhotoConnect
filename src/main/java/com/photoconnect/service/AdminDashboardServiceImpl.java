package com.photoconnect.service;

import com.photoconnect.dto.AdminDashboardStatsDto;
import com.photoconnect.dto.AdminDashboardAnalyticsDto;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final PhotographerProfileRepository photographerProfileRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final DepositRepository depositRepository;
    private final com.photoconnect.repository.UserReportRepository userReportRepository;
    private com.photoconnect.repository.PlatformFeeRepository platformFeeRepository;

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

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setPlatformFeeRepository(com.photoconnect.repository.PlatformFeeRepository platformFeeRepository) {
        this.platformFeeRepository = platformFeeRepository;
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
        long totalManagers = userRepository.countByRole(UserRole.MANAGER);

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
        dto.setTotalManagers(totalManagers);

        BigDecimal totalVolume = bookingRepository.calculateTotalPlatformBookingVolume();
        BigDecimal completedRev = bookingRepository.calculateTotalPlatformCompletedRevenue();
        dto.setTotalPlatformBookingVolume(totalVolume != null ? totalVolume : BigDecimal.ZERO);
        dto.setCompletedRevenue(completedRev != null ? completedRev : BigDecimal.ZERO);

        // Real ledger numbers from platform_fees table, avoiding duplicate or estimated figures
        BigDecimal feeIncurred = platformFeeRepository != null ? platformFeeRepository.sumTotalFeeAmount() : null;
        BigDecimal feeCollected = platformFeeRepository != null ? platformFeeRepository.sumTotalPaidAmount() : null;
        BigDecimal feeOutstanding = platformFeeRepository != null ? platformFeeRepository.sumTotalOutstandingAmount() : null;

        dto.setPlatformFeeRevenue(feeIncurred != null ? feeIncurred : (completedRev != null ? completedRev.multiply(new BigDecimal("0.10")) : BigDecimal.ZERO));
        dto.setPlatformFeeCollected(feeCollected != null ? feeCollected : BigDecimal.ZERO);
        dto.setPlatformFeeOutstanding(feeOutstanding != null ? feeOutstanding : dto.getPlatformFeeRevenue().subtract(dto.getPlatformFeeCollected()));

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

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardAnalyticsDto getDashboardAnalytics(LocalDate startDate, LocalDate endDate) {
        LocalDate safeEnd = endDate != null ? endDate : LocalDate.now();
        LocalDate safeStart = startDate != null ? startDate : safeEnd.minusMonths(11).withDayOfMonth(1);
        if (safeStart.isAfter(safeEnd)) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }
        if (safeStart.isBefore(safeEnd.minusYears(5))) {
            throw new IllegalArgumentException("Analytics range cannot exceed five years");
        }

        LocalDateTime startAt = safeStart.atStartOfDay();
        LocalDateTime endAt = safeEnd.plusDays(1).atStartOfDay();
        Map<YearMonth, MonthlyAnalytics> months = new LinkedHashMap<>();
        YearMonth cursor = YearMonth.from(safeStart);
        YearMonth last = YearMonth.from(safeEnd);
        while (!cursor.isAfter(last)) {
            months.put(cursor, new MonthlyAnalytics());
            cursor = cursor.plusMonths(1);
        }

        for (Object[] row : userRepository.countRegistrationsByMonthAndRole(startAt, endAt)) {
            YearMonth month = YearMonth.of(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
            MonthlyAnalytics value = months.get(month);
            if (value == null) continue;
            long count = ((Number) row[3]).longValue();
            value.users += count;
            UserRole role = (UserRole) row[2];
            if (role == UserRole.CUSTOMER) value.customers += count;
            if (role == UserRole.PHOTOGRAPHER) value.photographers += count;
        }
        for (Object[] row : bookingRepository.summarizeByCreatedMonth(startAt, endAt)) {
            YearMonth month = YearMonth.of(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
            MonthlyAnalytics value = months.get(month);
            if (value == null) continue;
            value.bookings = ((Number) row[2]).longValue();
            value.completedValue = row[3] instanceof BigDecimal amount ? amount : BigDecimal.ZERO;
        }

        AdminDashboardAnalyticsDto dto = new AdminDashboardAnalyticsDto();
        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("MM/yyyy");
        List<String> labels = new ArrayList<>();
        List<Long> users = new ArrayList<>();
        List<Long> customers = new ArrayList<>();
        List<Long> photographers = new ArrayList<>();
        List<Long> bookings = new ArrayList<>();
        List<BigDecimal> completedValues = new ArrayList<>();
        List<BigDecimal> platformFeeValues = new ArrayList<>();
        List<BigDecimal> platformFeePaidValues = new ArrayList<>();
        List<BigDecimal> platformFeeUnpaidValues = new ArrayList<>();
        months.forEach((month, value) -> {
            labels.add(month.format(labelFormat));
            users.add(value.users);
            customers.add(value.customers);
            photographers.add(value.photographers);
            bookings.add(value.bookings);
            completedValues.add(value.completedValue);
            BigDecimal fee = value.completedValue.multiply(new BigDecimal("0.10"));
            platformFeeValues.add(fee);
            platformFeePaidValues.add(BigDecimal.ZERO);
            platformFeeUnpaidValues.add(fee);
        });
        dto.setLabels(labels);
        dto.setUserRegistrations(users);
        dto.setCustomerRegistrations(customers);
        dto.setPhotographerRegistrations(photographers);
        dto.setBookings(bookings);
        dto.setCompletedBookingValue(completedValues);
        dto.setPlatformFeeValues(platformFeeValues);
        dto.setPlatformFeePaidValues(platformFeePaidValues);
        dto.setPlatformFeeUnpaidValues(platformFeeUnpaidValues);

        for (Object[] row : bookingRepository.countByStatusInCreatedRange(startAt, endAt)) {
            BookingStatus status = (BookingStatus) row[0];
            dto.getBookingStatusLabels().add(status.getDisplayName());
            dto.getBookingStatusCounts().add(((Number) row[1]).longValue());
        }
        return dto;
    }

    private static final class MonthlyAnalytics {
        private long users;
        private long customers;
        private long photographers;
        private long bookings;
        private BigDecimal completedValue = BigDecimal.ZERO;
    }
}
