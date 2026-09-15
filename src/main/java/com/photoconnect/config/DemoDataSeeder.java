package com.photoconnect.config;

import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("demo-seed")
@ConditionalOnProperty(name = "photoconnect.demo.seed-enabled", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final String EMAIL_DOMAIN = "@demo.photoconnect.local";

    private static final List<DemoPhotographer> PHOTOGRAPHERS = List.of(
            new DemoPhotographer("Linh Nguyen", "Saigon Editorial", "Ho Chi Minh City", 8, "1800000.00", "Editorial portraits and intimate city stories."),
            new DemoPhotographer("Minh Tran", "Golden Hour Studio", "Da Nang", 6, "2200000.00", "Warm wedding and lifestyle photography along the coast."),
            new DemoPhotographer("An Pham", "Northlight Weddings", "Hanoi", 10, "3500000.00", "Documentary wedding coverage with a quiet, timeless approach."),
            new DemoPhotographer("Mai Vo", "Maison Portraits", "Ho Chi Minh City", 4, "1400000.00", "Clean studio portraiture for people and personal brands."),
            new DemoPhotographer("Khanh Le", "Coastal Frames", "Nha Trang", 7, "2600000.00", "Destination sessions shaped by sea light and movement."),
            new DemoPhotographer("Thao Bui", "Hue Story House", "Hue", 9, "2100000.00", "Cultural celebrations and family stories in natural light."),
            new DemoPhotographer("Quang Do", "Urban Grain", "Hanoi", 5, "1650000.00", "Candid street portraits with bold architectural framing."),
            new DemoPhotographer("Vy Hoang", "Mekong Moments", "Can Tho", 3, "1200000.00", "Relaxed family, graduation, and riverside portrait sessions."),
            new DemoPhotographer("Son Dang", "Pine & Mist", "Da Lat", 11, "3200000.00", "Atmospheric engagement and wedding imagery in the highlands."),
            new DemoPhotographer("Nhi Truong", "Little Light Co.", "Da Nang", 2, "950000.00", "Friendly graduation and couple sessions for first-time clients."),
            new DemoPhotographer("Bao Nguyen", "Frame Foundry", "Ho Chi Minh City", 12, "4200000.00", "Commercial product and campaign photography for modern brands."),
            new DemoPhotographer("Yen Le", "Quiet Vows", "Hanoi", 7, "2900000.00", "Emotion-led elopement and small wedding documentation."),
            new DemoPhotographer("Tuan Pham", "Event Current", "Hai Phong", 6, "2400000.00", "Fast, polished conference and live-event coverage."),
            new DemoPhotographer("Ha Nguyen", "Soft Focus Family", "Bien Hoa", 5, "1350000.00", "Natural family portraits with calm direction and gentle color."),
            new DemoPhotographer("Duc Vo", "Analog Avenue", "Ho Chi Minh City", 9, "2750000.00", "Fashion and editorial work inspired by classic film aesthetics.")
    );

    private final UserRepository userRepository;
    private final PhotographerProfileRepository photographerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final String demoPassword;

    public DemoDataSeeder(UserRepository userRepository,
                          PhotographerProfileRepository photographerProfileRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${photoconnect.demo.password}") String demoPassword) {
        this.userRepository = userRepository;
        this.photographerProfileRepository = photographerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (demoPassword == null || demoPassword.length() < 12 || demoPassword.length() > 72) {
            throw new IllegalStateException("DEMO_PASSWORD must contain 12 to 72 characters.");
        }

        String passwordHash = passwordEncoder.encode(demoPassword);
        createAccountIfAbsent("admin" + EMAIL_DOMAIN, "Demo Administrator", UserRole.ADMIN, passwordHash);
        createAccountIfAbsent("customer" + EMAIL_DOMAIN, "Demo Customer", UserRole.CUSTOMER, passwordHash);

        for (int index = 0; index < PHOTOGRAPHERS.size(); index++) {
            DemoPhotographer data = PHOTOGRAPHERS.get(index);
            String email = String.format("photographer%02d%s", index + 1, EMAIL_DOMAIN);
            if (userRepository.findByEmail(email).isPresent()) {
                log.info("Demo account [{}] already exists; leaving its data unchanged", email);
                continue;
            }

            User user = userRepository.save(new User(
                    email, passwordHash, data.personName(), null, UserRole.PHOTOGRAPHER, UserStatus.ACTIVE));
            PhotographerProfile profile = new PhotographerProfile(
                    user,
                    data.displayName(),
                    data.bio(),
                    data.city(),
                    data.experienceYears(),
                    new BigDecimal(data.priceFrom()),
                    PhotographerVerificationStatus.APPROVED);
            photographerProfileRepository.save(profile);
        }

        log.info("PhotoConnect demo seed completed without modifying existing accounts");
    }

    private void createAccountIfAbsent(String email, String fullName, UserRole role, String passwordHash) {
        if (userRepository.findByEmail(email).isEmpty()) {
            userRepository.save(new User(email, passwordHash, fullName, null, role, UserStatus.ACTIVE));
        } else {
            log.info("Demo account [{}] already exists; leaving it unchanged", email);
        }
    }

    private record DemoPhotographer(String personName,
                                    String displayName,
                                    String city,
                                    int experienceYears,
                                    String priceFrom,
                                    String bio) {
    }
}
