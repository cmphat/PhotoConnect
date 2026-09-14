package com.photoconnect.service;

import com.photoconnect.dto.DepositViewDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.Deposit;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.entity.User;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.DepositRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepositServiceTest {

    @Mock
    private DepositRepository depositRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private DepositServiceImpl depositService;

    private User customer;
    private Booking booking;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(depositService, "simulationEnabled", true);

        customer = new User();
        customer.setId(10L);

        booking = new Booking();
        booking.setId(100L);
        booking.setCustomer(customer);
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setAgreedPrice(new BigDecimal("2000000.00"));
    }

    @Test
    void getOrCreateDepositForBooking_shouldCreateDeposit_whenNoneExists() {
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));
        when(depositRepository.findByBookingIdWithDetails(100L)).thenReturn(Optional.empty());
        
        Deposit newDeposit = new Deposit(booking, new BigDecimal("600000.00"));
        newDeposit.setId(1L);
        when(depositRepository.save(any(Deposit.class))).thenReturn(newDeposit);

        DepositViewDto dto = depositService.getOrCreateDepositForBooking(100L, 10L);

        assertThat(dto.getAmount()).isEqualByComparingTo("600000.00");
        assertThat(dto.getStatus()).isEqualTo(DepositStatus.PENDING);
        verify(depositRepository, times(1)).save(any(Deposit.class));
    }

    @Test
    void getOrCreateDepositForBooking_shouldReturnExisting_whenAlreadyExists() {
        Deposit existing = new Deposit(booking, new BigDecimal("600000.00"));
        existing.setId(2L);
        
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));
        when(depositRepository.findByBookingIdWithDetails(100L)).thenReturn(Optional.of(existing));

        DepositViewDto dto = depositService.getOrCreateDepositForBooking(100L, 10L);

        assertThat(dto.getId()).isEqualTo(2L);
        verify(depositRepository, never()).save(any(Deposit.class));
    }

    @Test
    void getOrCreateDepositForBooking_shouldThrow_whenNotAccepted() {
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));

        assertThrows(InvalidBookingException.class, () -> depositService.getOrCreateDepositForBooking(100L, 10L));
    }

    @Test
    void simulateSuccessfulPayment_shouldMarkAsPaid_whenPending() {
        Deposit pending = new Deposit(booking, new BigDecimal("600000.00"));
        pending.setId(3L);
        pending.setStatus(DepositStatus.PENDING);

        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));
        when(depositRepository.findByBookingIdWithDetails(100L)).thenReturn(Optional.of(pending));
        when(depositRepository.save(any(Deposit.class))).thenReturn(pending); // Save returns same object updated

        DepositViewDto dto = depositService.simulateSuccessfulPayment(100L, 10L);

        assertThat(dto.getStatus()).isEqualTo(DepositStatus.PAID);
        assertThat(dto.getPaymentReference()).startsWith("DEV-");
        verify(depositRepository, times(1)).save(pending);
    }
    
    @Test
    void simulateSuccessfulPayment_shouldThrow_whenSimulationDisabled() {
        ReflectionTestUtils.setField(depositService, "simulationEnabled", false);
        
        assertThrows(IllegalStateException.class, () -> depositService.simulateSuccessfulPayment(100L, 10L));
    }
}
