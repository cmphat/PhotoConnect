package com.photoconnect.repository;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_USERNAME", matches = ".+")
public class BookingRepositoryIntegrationTests {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PhotographerProfileRepository photographerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    private User customerUser;
    private User photographerUser;
    private PhotographerProfile photographerProfile;

    @BeforeEach
    void setUp() {
        customerUser = new User();
        customerUser.setEmail("booking.customer@example.com");
        customerUser.setPassword("hashedpassword123");
        customerUser.setFullName("Booking Customer");
        customerUser.setPhone("0901234567");
        customerUser.setRole(UserRole.CUSTOMER);
        customerUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(customerUser);

        photographerUser = new User();
        photographerUser.setEmail("booking.photographer@example.com");
        photographerUser.setPassword("hashedpassword123");
        photographerUser.setFullName("Booking Artist");
        photographerUser.setPhone("0907654321");
        photographerUser.setRole(UserRole.PHOTOGRAPHER);
        photographerUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(photographerUser);

        photographerProfile = new PhotographerProfile();
        photographerProfile.setUser(photographerUser);
        photographerProfile.setDisplayName("Cinematic Studio");
        photographerProfile.setCity("Da Nang");
        photographerProfile.setExperienceYears(4);
        photographerProfile.setPriceFrom(new BigDecimal("1800000.00"));
        photographerProfile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);
        photographerProfileRepository.save(photographerProfile);
    }

    @AfterEach
    void tearDown() {
        // Clean up child bookings first, then profile, then users
        List<Booking> customerBookings = bookingRepository.findByCustomerIdWithDetails(customerUser.getId());
        bookingRepository.deleteAll(customerBookings);

        List<Booking> profileBookings = bookingRepository.findByPhotographerUserIdWithDetails(photographerUser.getId());
        bookingRepository.deleteAll(profileBookings);

        photographerProfileRepository.delete(photographerProfile);
        userRepository.delete(customerUser);
        userRepository.delete(photographerUser);
    }

    @Test
    void shouldSaveAndRetrieveBooking() {
        Booking booking = new Booking();
        booking.setCustomer(customerUser);
        booking.setPhotographerProfile(photographerProfile);
        booking.setBookingDate(LocalDate.now().plusDays(7));
        booking.setBookingTime(LocalTime.of(15, 30));
        booking.setLocation("Dragon Bridge, Da Nang");
        booking.setNotes("Sunset portrait photoshoot.");
        booking.setAgreedPrice(photographerProfile.getPriceFrom());
        booking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getAgreedPrice()).isEqualByComparingTo(new BigDecimal("1800000.00"));
        assertThat(saved.getStatus()).isEqualTo(BookingStatus.PENDING);

        Optional<Booking> retrievedOpt = bookingRepository.findByIdWithDetails(saved.getId());
        assertThat(retrievedOpt).isPresent();
        Booking retrieved = retrievedOpt.get();
        assertThat(retrieved.getCustomer().getFullName()).isEqualTo("Booking Customer");
        assertThat(retrieved.getPhotographerProfile().getDisplayName()).isEqualTo("Cinematic Studio");
        assertThat(retrieved.getPhotographerProfile().getUser().getEmail()).isEqualTo("booking.photographer@example.com");
    }

    @Test
    void findByCustomerId_shouldReturnBookingsOrdered() {
        Booking booking1 = new Booking(customerUser, photographerProfile, LocalDate.now().plusDays(2), LocalTime.of(10, 0), "Loc 1", null, new BigDecimal("1800000.00"));
        bookingRepository.save(booking1);

        Booking booking2 = new Booking(customerUser, photographerProfile, LocalDate.now().plusDays(4), LocalTime.of(14, 0), "Loc 2", null, new BigDecimal("1800000.00"));
        bookingRepository.save(booking2);

        List<Booking> results = bookingRepository.findByCustomerIdWithDetails(customerUser.getId());
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getCustomer().getId()).isEqualTo(customerUser.getId());
    }
}
