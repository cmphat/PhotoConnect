package com.photoconnect.repository;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.Deposit;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.entity.DemoPaymentMethod;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_USERNAME", matches = ".+")
public class DepositRepositoryIntegrationTests {

    @Autowired
    private DepositRepository depositRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PhotographerProfileRepository photographerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    private User customerUser;
    private User photographerUser;
    private PhotographerProfile photographerProfile;
    private Booking booking;

    @BeforeEach
    void setUp() {
        customerUser = new User();
        customerUser.setEmail("deposit.customer@example.com");
        customerUser.setPassword("pass");
        customerUser.setFullName("Deposit Cust");
        customerUser.setRole(UserRole.CUSTOMER);
        customerUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(customerUser);

        photographerUser = new User();
        photographerUser.setEmail("deposit.photog@example.com");
        photographerUser.setPassword("pass");
        photographerUser.setFullName("Deposit Photog");
        photographerUser.setRole(UserRole.PHOTOGRAPHER);
        photographerUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(photographerUser);

        photographerProfile = new PhotographerProfile();
        photographerProfile.setUser(photographerUser);
        photographerProfile.setDisplayName("Dep Studio");
        photographerProfile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);
        photographerProfileRepository.save(photographerProfile);

        booking = new Booking(customerUser, photographerProfile, LocalDate.now().plusDays(1), LocalTime.NOON, "Loc", null, new BigDecimal("1000.00"));
        bookingRepository.save(booking);
    }

    @AfterEach
    void tearDown() {
        depositRepository.deleteAll();
        bookingRepository.deleteAll();
        photographerProfileRepository.deleteAll();
        userRepository.delete(customerUser);
        userRepository.delete(photographerUser);
    }

    @Test
    void shouldSaveAndRetrieveDeposit() {
        Deposit deposit = new Deposit(booking, new BigDecimal("300.00"));
        deposit.setStatus(DepositStatus.PENDING);
        deposit.setPaymentMethod(DemoPaymentMethod.DEMO_QR);
        deposit.setPaymentReference("PC-20260915-INTEG001");
        
        Deposit saved = depositRepository.save(deposit);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        
        Optional<Deposit> retrievedOpt = depositRepository.findByBookingIdWithDetails(booking.getId());
        assertThat(retrievedOpt).isPresent();
        assertThat(retrievedOpt.get().getAmount()).isEqualByComparingTo("300.00");
        assertThat(retrievedOpt.get().getPaymentMethod()).isEqualTo(DemoPaymentMethod.DEMO_QR);
        assertThat(retrievedOpt.get().getPaymentReference()).isEqualTo("PC-20260915-INTEG001");
        assertThat(retrievedOpt.get().getBooking().getCustomer().getId()).isEqualTo(customerUser.getId());
    }

    @Test
    void shouldNotAllowDuplicateDepositsForSameBooking() {
        Deposit deposit1 = new Deposit(booking, new BigDecimal("300.00"));
        depositRepository.save(deposit1);

        Deposit deposit2 = new Deposit(booking, new BigDecimal("300.00"));
        assertThrows(Exception.class, () -> depositRepository.saveAndFlush(deposit2));
    }
}
