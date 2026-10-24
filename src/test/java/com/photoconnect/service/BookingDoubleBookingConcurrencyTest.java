package com.photoconnect.service;

import com.photoconnect.entity.*;
import com.photoconnect.exception.PhotographerUnavailableException;
import com.photoconnect.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingDoubleBookingConcurrencyTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PhotographerProfileRepository photographerProfileRepository;

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User photographerUser;
    private PhotographerProfile profile;
    private Booking booking1;
    private Booking booking2;
    private LocalDate shootDate;

    @BeforeEach
    void setUp() {
        photographerUser = new User();
        photographerUser.setId(20L);
        photographerUser.setFullName("Photographer Bob");
        photographerUser.setRole(UserRole.PHOTOGRAPHER);
        photographerUser.setStatus(UserStatus.ACTIVE);

        profile = new PhotographerProfile();
        profile.setId(10L);
        profile.setUser(photographerUser);
        profile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);

        shootDate = LocalDate.now().plusDays(5);

        User customer1 = new User();
        customer1.setId(101L);
        customer1.setFullName("Customer 1");

        User customer2 = new User();
        customer2.setId(102L);
        customer2.setFullName("Customer 2");

        // Conflicting bookings on the same date and overlapping times
        booking1 = new Booking(customer1, profile, shootDate, LocalTime.of(10, 0), "Park", "Shoot 1", BigDecimal.valueOf(2000000));
        booking1.setId(1L);
        booking1.setDurationMinutes(120); // 10:00 - 12:00
        booking1.setStatus(BookingStatus.PENDING);

        booking2 = new Booking(customer2, profile, shootDate, LocalTime.of(11, 0), "Studio", "Shoot 2", BigDecimal.valueOf(2000000));
        booking2.setId(2L);
        booking2.setDurationMinutes(120); // 11:00 - 13:00 (overlaps with 10:00 - 12:00)
        booking2.setStatus(BookingStatus.PENDING);
    }

    @Test
    @DisplayName("SEC-02: Simultaneous acceptance of conflicting bookings results in exactly 1 success and 1 rejection")
    void simultaneousAcceptanceOfConflictingBookings_preventsDoubleBooking() throws Exception {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking1));
        when(bookingRepository.findByIdWithDetails(2L)).thenReturn(Optional.of(booking2));

        // Simulate database pessimistic write locking and slot availability transition
        ReentrantLock dbLock = new ReentrantLock();
        Set<Long> acceptedBookingIds = Collections.synchronizedSet(new HashSet<>());

        when(photographerProfileRepository.findByIdWithLock(10L)).thenAnswer(inv -> {
            dbLock.lock();
            return Optional.of(profile);
        });

        doAnswer(inv -> {
            // If any conflicting booking has already been accepted, slot is no longer available
            if (!acceptedBookingIds.isEmpty()) {
                throw new PhotographerUnavailableException("The requested time slot conflicts with an existing confirmed booking.");
            }
            return null;
        }).when(scheduleService).validateSlotAvailability(eq(10L), eq(shootDate), any(), anyInt(), any());

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            try {
                Booking b = inv.getArgument(0);
                acceptedBookingIds.add(b.getId());
                return b;
            } finally {
                if (dbLock.isHeldByCurrentThread()) {
                    dbLock.unlock();
                }
            }
        });

        int numThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        executor.submit(() -> {
            try {
                startLatch.await();
                bookingService.acceptBooking(1L, 20L);
                successCount.incrementAndGet();
            } catch (PhotographerUnavailableException e) {
                conflictCount.incrementAndGet();
            } catch (Exception e) {
                // Unexpected error
            } finally {
                if (dbLock.isHeldByCurrentThread()) {
                    dbLock.unlock();
                }
                doneLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                bookingService.acceptBooking(2L, 20L);
                successCount.incrementAndGet();
            } catch (PhotographerUnavailableException e) {
                conflictCount.incrementAndGet();
            } catch (Exception e) {
                // Unexpected error
            } finally {
                if (dbLock.isHeldByCurrentThread()) {
                    dbLock.unlock();
                }
                doneLatch.countDown();
            }
        });

        startLatch.countDown(); // Trigger both threads simultaneously
        boolean completed = doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(1);
        assertThat(acceptedBookingIds).hasSize(1);
    }

    @Test
    @DisplayName("SEC-02: Rescheduling into a slot occupied by active status throws PhotographerUnavailableException")
    void rescheduleIntoOccupiedSlot_throwsPhotographerUnavailableException() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking1));
        when(photographerProfileRepository.findByIdWithLock(10L)).thenReturn(Optional.of(profile));

        doThrow(new PhotographerUnavailableException("The requested time slot conflicts with an existing confirmed booking."))
                .when(scheduleService).validateSlotAvailability(eq(10L), eq(shootDate), eq(LocalTime.of(10, 0)), eq(120), eq(1L));

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                bookingService.acceptBooking(1L, 20L))
                .isInstanceOf(PhotographerUnavailableException.class)
                .hasMessageContaining("conflicts with an existing confirmed booking");
    }
}
