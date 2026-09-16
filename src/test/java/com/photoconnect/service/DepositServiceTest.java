package com.photoconnect.service;

import com.photoconnect.dto.DemoPaymentRequest;
import com.photoconnect.dto.DepositViewDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.DemoPaymentMethod;
import com.photoconnect.entity.Deposit;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.DepositRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

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
        customer.setRole(UserRole.CUSTOMER);

        booking = new Booking();
        booking.setId(100L);
        booking.setCustomer(customer);
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setAgreedPrice(new BigDecimal("2000000.00"));
    }

    @Test
    void getOrCreateDeposit_calculatesExactlyThirtyPercentAndGeneratesServerReference() {
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));
        when(depositRepository.findByBookingIdWithDetails(100L)).thenReturn(Optional.empty());
        when(depositRepository.save(any(Deposit.class))).thenAnswer(invocation -> {
            Deposit saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        DepositViewDto dto = depositService.getOrCreateDepositForBooking(100L, 10L);

        assertThat(dto.getAmount()).isEqualByComparingTo("600000.00");
        assertThat(dto.getStatus()).isEqualTo(DepositStatus.PENDING);
        assertThat(dto.getPaymentReference()).matches("PC-\\d{8}-[A-F0-9]{8}");
    }

    @Test
    void getOrCreateDeposit_returnsExistingWithoutChangingHistoricalAmount() {
        Deposit existing = pendingDeposit();
        existing.setAmount(new BigDecimal("599999.00"));
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));
        when(depositRepository.findByBookingIdWithDetails(100L)).thenReturn(Optional.of(existing));

        DepositViewDto dto = depositService.getOrCreateDepositForBooking(100L, 10L);

        assertThat(dto.getAmount()).isEqualByComparingTo("599999.00");
        verify(depositRepository, never()).save(any());
    }

    @Test
    void checkoutRejectsInvalidBookingState() {
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));
        when(depositRepository.findByBookingIdWithDetails(100L)).thenReturn(Optional.empty());

        assertThrows(InvalidBookingException.class,
                () -> depositService.getOrCreateDepositForBooking(100L, 10L));
    }

    @Test
    void checkoutRejectsAnotherCustomer() {
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));

        assertThrows(UnauthorizedException.class,
                () -> depositService.getOrCreateDepositForBooking(100L, 99L));
        verify(depositRepository, never()).save(any());
    }

    @Test
    void checkoutRejectsNonCustomerBookingPrincipal() {
        customer.setRole(UserRole.PHOTOGRAPHER);
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));

        assertThrows(UnauthorizedException.class,
                () -> depositService.getOrCreateDepositForBooking(100L, 10L));
    }

    @Test
    void nonExistentBookingUsesExistingDomainExceptionSystem() {
        when(bookingRepository.findByIdWithDetails(404L)).thenReturn(Optional.empty());

        assertThrows(InvalidBookingException.class,
                () -> depositService.getOrCreateDepositForBooking(404L, 10L));
    }

    @Test
    void successfulDemoCardMovesThroughProcessingToPaid() {
        Deposit pending = pendingDeposit();
        stubPaymentAttempt(pending);

        DepositViewDto dto = depositService.processDemoPayment(100L, 10L,
                cardRequest("4242 4242 4242 4242", "12/40", "123"));

        assertThat(dto.getStatus()).isEqualTo(DepositStatus.PAID);
        assertThat(dto.getPaymentMethod()).isEqualTo(DemoPaymentMethod.DEMO_CARD);
        assertThat(dto.getPaidAt()).isNotNull();
        assertThat(dto.getFailureReason()).isNull();
        verify(depositRepository).saveAndFlush(pending);
        verify(depositRepository).save(pending);
    }

    @Test
    void failedDemoCardNeverBecomesPaid() {
        Deposit pending = pendingDeposit();
        stubPaymentAttempt(pending);

        DepositViewDto dto = depositService.processDemoPayment(100L, 10L,
                cardRequest("4000 0000 0000 0002", "12/40", "123"));

        assertThat(dto.getStatus()).isEqualTo(DepositStatus.FAILED);
        assertThat(dto.getPaidAt()).isNull();
        assertThat(dto.getFailureReason()).contains("declined");
    }

    @Test
    void invalidCardInputFailsSafelyWithoutEchoingSensitiveValues() {
        Deposit pending = pendingDeposit();
        stubPaymentAttempt(pending);

        DepositViewDto dto = depositService.processDemoPayment(100L, 10L,
                cardRequest("123", "99/10", "secret"));

        assertThat(dto.getStatus()).isEqualTo(DepositStatus.FAILED);
        assertThat(dto.getFailureReason()).isEqualTo("The demo card details could not be validated.");
        assertThat(dto.getFailureReason()).doesNotContain("123", "secret");
    }

    @Test
    void demoQrCompletesSuccessfullyWithoutCardInput() {
        Deposit pending = pendingDeposit();
        stubPaymentAttempt(pending);
        DemoPaymentRequest request = new DemoPaymentRequest();
        request.setPaymentMethod("DEMO_QR");

        DepositViewDto dto = depositService.processDemoPayment(100L, 10L, request);

        assertThat(dto.getStatus()).isEqualTo(DepositStatus.PAID);
        assertThat(dto.getPaymentMethod()).isEqualTo(DemoPaymentMethod.DEMO_QR);
    }

    @Test
    void cancelPendingAttemptNeverMarksPaid() {
        Deposit pending = pendingDeposit();
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));
        when(depositRepository.findByBookingIdForUpdate(100L)).thenReturn(Optional.of(pending));
        when(depositRepository.save(pending)).thenReturn(pending);

        DepositViewDto dto = depositService.cancelDemoPayment(100L, 10L);

        assertThat(dto.getStatus()).isEqualTo(DepositStatus.CANCELLED);
        assertThat(dto.getPaidAt()).isNull();
    }

    @Test
    void duplicateSuccessfulPostIsIdempotent() {
        Deposit paid = pendingDeposit();
        paid.setStatus(DepositStatus.PAID);
        paid.setPaidAt(LocalDateTime.now());
        paid.setPaymentMethod(DemoPaymentMethod.DEMO_CARD);
        booking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));
        when(depositRepository.findByBookingIdForUpdate(100L)).thenReturn(Optional.of(paid));

        DepositViewDto dto = depositService.processDemoPayment(100L, 10L,
                cardRequest("4000 0000 0000 0002", "12/40", "123"));

        assertThat(dto.getStatus()).isEqualTo(DepositStatus.PAID);
        assertThat(dto.getPaymentMethod()).isEqualTo(DemoPaymentMethod.DEMO_CARD);
        verify(depositRepository, never()).saveAndFlush(any());
        verify(depositRepository, never()).save(any());
    }

    @Test
    void browserCannotOverrideServerAmount() {
        Deposit pending = pendingDeposit();
        stubPaymentAttempt(pending);
        DemoPaymentRequest request = cardRequest("4242424242424242", "12/40", "123");

        DepositViewDto dto = depositService.processDemoPayment(100L, 10L, request);

        assertThat(dto.getAmount()).isEqualByComparingTo("600000.00");
        ArgumentCaptor<Deposit> captor = ArgumentCaptor.forClass(Deposit.class);
        verify(depositRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("600000.00");
    }

    @Test
    void sensitiveCardFieldsDoNotExistOnPersistedDepositModel() {
        assertThat(Arrays.stream(Deposit.class.getDeclaredFields()).map(java.lang.reflect.Field::getName))
                .doesNotContain("cardNumber", "cvv", "cardholderName", "expiry");
    }

    @Test
    void invalidBookingStateCannotBePaid() {
        booking.setStatus(BookingStatus.PENDING);
        Deposit pending = pendingDeposit();
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));
        when(depositRepository.findByBookingIdForUpdate(100L)).thenReturn(Optional.of(pending));

        assertThrows(InvalidBookingException.class,
                () -> depositService.processDemoPayment(100L, 10L, cardRequest("4242424242424242", "12/40", "123")));
        verify(depositRepository, never()).save(any());
    }

    @Test
    void simulationDisabledRejectsBeforeRepositoryAccess() {
        ReflectionTestUtils.setField(depositService, "simulationEnabled", false);

        assertThrows(IllegalStateException.class,
                () -> depositService.getOrCreateDepositForBooking(100L, 10L));
        verify(bookingRepository, never()).findByIdWithDetails(any());
    }

    private Deposit pendingDeposit() {
        Deposit deposit = new Deposit(booking, new BigDecimal("600000.00"));
        deposit.setId(3L);
        deposit.setStatus(DepositStatus.PENDING);
        deposit.setPaymentReference("PC-20260915-ABCDEF12");
        return deposit;
    }

    private void stubPaymentAttempt(Deposit deposit) {
        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));
        when(depositRepository.findByBookingIdForUpdate(100L)).thenReturn(Optional.of(deposit));
        when(depositRepository.saveAndFlush(deposit)).thenReturn(deposit);
        when(depositRepository.save(deposit)).thenReturn(deposit);
    }

    private DemoPaymentRequest cardRequest(String number, String expiry, String cvv) {
        DemoPaymentRequest request = new DemoPaymentRequest();
        request.setPaymentMethod("DEMO_CARD");
        request.setCardholderName("Demo Customer");
        request.setCardNumber(number);
        request.setExpiry(expiry);
        request.setCvv(cvv);
        return request;
    }
}
