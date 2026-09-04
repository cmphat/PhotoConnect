package com.photoconnect.service;

import com.photoconnect.dto.DepositViewDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.Deposit;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.DepositRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class DepositServiceImpl implements DepositService {

    private final DepositRepository depositRepository;
    private final BookingRepository bookingRepository;

    @Value("${photoconnect.payment.simulation-enabled:true}")
    private boolean simulationEnabled;

    private static final BigDecimal DEPOSIT_RATE = new BigDecimal("0.30");

    public DepositServiceImpl(DepositRepository depositRepository, BookingRepository bookingRepository) {
        this.depositRepository = depositRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public DepositViewDto getOrCreateDepositForBooking(Long bookingId, Long customerUserId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking not found"));

        if (!booking.getCustomer().getId().equals(customerUserId)) {
            throw new UnauthorizedException("You do not own this booking");
        }

        if (booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new InvalidBookingException("Deposit can only be created for ACCEPTED bookings");
        }

        Optional<Deposit> existingDeposit = depositRepository.findByBookingIdWithDetails(bookingId);
        if (existingDeposit.isPresent()) {
            return mapToDto(existingDeposit.get());
        }

        BigDecimal depositAmount = booking.getAgreedPrice().multiply(DEPOSIT_RATE);
        Deposit deposit = new Deposit(booking, depositAmount);
        deposit = depositRepository.save(deposit);

        return mapToDto(deposit);
    }

    @Override
    @Transactional(readOnly = true)
    public DepositViewDto getCustomerDeposit(Long bookingId, Long customerUserId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking not found"));

        if (!booking.getCustomer().getId().equals(customerUserId)) {
            throw new UnauthorizedException("You do not own this booking");
        }

        return depositRepository.findByBookingIdWithDetails(bookingId)
                .map(this::mapToDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public DepositViewDto simulateSuccessfulPayment(Long bookingId, Long customerUserId) {
        if (!simulationEnabled) {
            throw new IllegalStateException("Payment simulation is disabled in this environment");
        }

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking not found"));

        if (!booking.getCustomer().getId().equals(customerUserId)) {
            throw new UnauthorizedException("You do not own this booking");
        }

        Deposit deposit = depositRepository.findByBookingIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Deposit not found for this booking"));

        if (deposit.getStatus() == DepositStatus.PAID) {
            // Idempotent return
            return mapToDto(deposit);
        }

        if (deposit.getStatus() != DepositStatus.PENDING) {
            throw new InvalidBookingException("Deposit is not in PENDING state");
        }

        deposit.setStatus(DepositStatus.PAID);
        deposit.setPaidAt(LocalDateTime.now());
        deposit.setPaymentReference("DEV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        deposit = depositRepository.save(deposit);

        return mapToDto(deposit);
    }

    private DepositViewDto mapToDto(Deposit deposit) {
        DepositViewDto dto = new DepositViewDto();
        dto.setId(deposit.getId());
        dto.setBookingId(deposit.getBooking().getId());
        dto.setAmount(deposit.getAmount());
        dto.setStatus(deposit.getStatus());
        dto.setPaymentReference(deposit.getPaymentReference());
        dto.setPaidAt(deposit.getPaidAt());
        return dto;
    }
}
