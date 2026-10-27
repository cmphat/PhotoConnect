package com.photoconnect.service;

import com.photoconnect.dto.BookingRequest;
import com.photoconnect.dto.PriceBreakdownDto;
import com.photoconnect.entity.*;
import com.photoconnect.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingPriceSnapshotTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ServicePackageRepository servicePackageRepository;

    @Mock
    private PhotographerProfileRepository photographerProfileRepository;

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private PricingService pricingService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User customer;
    private User photographer;
    private PhotographerProfile profile;
    private ServicePackage servicePackage;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(10L);
        customer.setFullName("Customer A");
        customer.setRole(UserRole.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);

        photographer = new User();
        photographer.setId(20L);
        photographer.setFullName("Photographer B");
        photographer.setRole(UserRole.PHOTOGRAPHER);
        photographer.setStatus(UserStatus.ACTIVE);

        profile = new PhotographerProfile();
        profile.setId(1L);
        profile.setUser(photographer);
        profile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);

        servicePackage = new ServicePackage();
        servicePackage.setId(100L);
        servicePackage.setName("Portrait Standard");
        servicePackage.setPrice(new BigDecimal("1200000"));
        servicePackage.setDurationMinutes(120);
        servicePackage.setEditedPhotoCount(20);
        servicePackage.setDeliveryDays(3);
        servicePackage.setActive(true);
        servicePackage.setPhotographerProfile(profile);
    }

    @Test
    @DisplayName("Booking creation persists authoritative pricing snapshot that remains immutable after package price changes")
    void testBookingPriceSnapshotImmutability() {
        BookingRequest request = new BookingRequest();
        request.setPackageId(100L);
        request.setBookingDate(LocalDate.now().plusDays(5));
        request.setBookingTime(LocalTime.of(19, 0));
        request.setLocation("Hanoi Old Quarter");
        request.setTravelDistanceKm(35);
        request.setNightShoot(true);
        request.setExtraHours(1);
        request.setExtraPhotos(2);
        request.setAdditionalPhotographer(false);

        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(photographerProfileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(servicePackageRepository.findByIdAndPhotographerProfileId(100L, 1L)).thenReturn(Optional.of(servicePackage));
        when(scheduleService.isDateAvailable(eq(1L), any(LocalDate.class))).thenReturn(true);

        // Pricing breakdown: 1,200,000 base + 240,000 night + 75,000 travel + 300,000 extras = 1,815,000 total
        PriceBreakdownDto breakdown = new PriceBreakdownDto();
        breakdown.setBasePrice(new BigDecimal("1200000"));
        breakdown.setNightSurcharge(new BigDecimal("240000"));
        breakdown.setNightShoot(true);
        breakdown.setTravelDistanceKm(35);
        breakdown.setTravelSurcharge(new BigDecimal("75000"));
        breakdown.setExtraHours(1);
        breakdown.setExtraHoursFee(new BigDecimal("200000"));
        breakdown.setExtraPhotos(2);
        breakdown.setExtraPhotosFee(new BigDecimal("100000"));
        breakdown.setAdditionalPhotographer(false);
        breakdown.setAdditionalPhotographerFee(BigDecimal.ZERO);
        breakdown.setExtrasFee(new BigDecimal("300000"));
        breakdown.setTotalPrice(new BigDecimal("1815000"));
        breakdown.setDepositAmount(new BigDecimal("544500"));
        breakdown.setRemainingBalance(new BigDecimal("1270500"));

        when(pricingService.calculatePricing(
                eq(servicePackage),
                eq(LocalTime.of(19, 0)),
                eq(true),
                eq(35),
                eq(1),
                eq(2),
                eq(false)
        )).thenReturn(breakdown);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(500L);
            return b;
        });

        Booking createdBooking = bookingService.createBooking(10L, 1L, request);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        Booking savedBooking = captor.getValue();

        // Verify snapshot values saved on the entity
        assertThat(savedBooking.getAgreedPrice()).isEqualByComparingTo("1815000");
        assertThat(savedBooking.getBasePrice()).isEqualByComparingTo("1200000");
        assertThat(savedBooking.getNightSurcharge()).isEqualByComparingTo("240000");
        assertThat(savedBooking.getTravelDistanceKm()).isEqualTo(35);
        assertThat(savedBooking.getTravelSurcharge()).isEqualByComparingTo("75000");
        assertThat(savedBooking.getExtrasFee()).isEqualByComparingTo("300000");
        assertThat(savedBooking.getExtraHours()).isEqualTo(1);
        assertThat(savedBooking.getExtraPhotos()).isEqualTo(2);
        assertThat(savedBooking.getAdditionalPhotographer()).isFalse();

        // Now mutate the photographer's package price (e.g. inflation or price increase to 3,000,000)
        servicePackage.setPrice(new BigDecimal("3000000"));

        // Saved booking snapshot is independent of subsequent package alterations
        assertThat(savedBooking.getAgreedPrice()).isEqualByComparingTo("1815000");
        assertThat(savedBooking.getBasePrice()).isEqualByComparingTo("1200000");
    }
}
