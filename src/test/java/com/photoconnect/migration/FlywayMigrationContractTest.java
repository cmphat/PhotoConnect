package com.photoconnect.migration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Flyway Migration Contract & Safety Verification Test")
class FlywayMigrationContractTest {

    private static final Path ROOT = Path.of(System.getProperty("user.dir"));
    private static final Path MIGRATIONS = ROOT.resolve("src/main/resources/db/migration");

    private static final Pattern MIGRATION_NAME_PATTERN =
            Pattern.compile("^V\\d{3}__[a-z0-9_]+\\.sql$");

    @Test
    @DisplayName("All Flyway migration scripts adhere to strict naming conventions and valid suffixes")
    void migrationFiles_followNamingConventions() throws IOException {
        try (var files = Files.walk(MIGRATIONS, 1)) {
            List<Path> migrationScripts = files
                    .filter(path -> !path.equals(MIGRATIONS))
                    .filter(path -> path.toString().endsWith(".sql"))
                    .toList();

            assertThat(migrationScripts)
                    .as("Must contain all 18 migration scripts")
                    .hasSize(18);

            for (Path script : migrationScripts) {
                String fileName = script.getFileName().toString();
                assertThat(MIGRATION_NAME_PATTERN.matcher(fileName).matches())
                        .as("Migration script %s must follow Vxxx__description.sql pattern", fileName)
                        .isTrue();
            }
        }
    }

    @Test
    @DisplayName("Migration scripts do not contain dangerous unshielded DROP or TRUNCATE operations")
    void migrationScripts_avoidUnshieldedDestructiveCommands() throws IOException {
        Pattern forbiddenDestructive = Pattern.compile("(?i)\\b(DROP\\s+DATABASE|TRUNCATE\\s+TABLE)\\b");

        try (var files = Files.walk(MIGRATIONS, 1)) {
            List<Path> migrationScripts = files
                    .filter(path -> path.toString().endsWith(".sql"))
                    .toList();

            for (Path script : migrationScripts) {
                String content = Files.readString(script);
                assertThat(forbiddenDestructive.matcher(content).find())
                        .as("Migration script %s must not contain DROP DATABASE or unshielded TRUNCATE", script.getFileName())
                        .isFalse();
            }
        }
    }

    @Test
    @DisplayName("Application properties enforces production-grade Flyway migration safety settings")
    void applicationProperties_enforcesFlywaySafety() throws IOException {
        String properties = Files.readString(ROOT.resolve("src/main/resources/application.properties"));

        assertThat(properties)
                .contains("spring.flyway.baseline-on-migrate=true")
                .contains("spring.flyway.baseline-version=0")
                .contains("spring.flyway.locations=classpath:db/migration")
                .contains("spring.flyway.clean-disabled=true")
                .contains("spring.flyway.validate-on-migrate=true");
    }

    @Test
    @DisplayName("Latest migrations V022 through V026 define expected PhotoConnect V5 feature tables and columns")
    void recentMigrations_defineExpectedV5Schema() throws IOException {
        // V022: Email OTP tokens
        String v022 = Files.readString(MIGRATIONS.resolve("V022__create_email_verification_tokens.sql"));
        assertThat(v022).contains("email_verification_tokens").contains("token_hash").contains("target_value");

        // V023: Photographer audit and social channels
        String v023 = Files.readString(MIGRATIONS.resolve("V023__add_photographer_review_audit_and_social_urls.sql"));
        assertThat(v023).contains("tiktok_url").contains("youtube_url").contains("rejection_reason").contains("admin_notes");

        // V024: Shoot Brief fields
        String v024 = Files.readString(MIGRATIONS.resolve("V024__add_booking_shoot_brief.sql"));
        assertThat(v024).contains("concept_description").contains("moodboard_url").contains("backup_location").contains("shot_list");

        // V025: User reports dispute management
        String v025 = Files.readString(MIGRATIONS.resolve("V025__create_user_reports.sql"));
        assertThat(v025).contains("user_reports").contains("category").contains("status").contains("admin_notes");

        // V026: Photographer CRM notes
        String v026 = Files.readString(MIGRATIONS.resolve("V026__add_photographer_notes_to_bookings.sql"));
        assertThat(v026).contains("photographer_notes");
    }
}
