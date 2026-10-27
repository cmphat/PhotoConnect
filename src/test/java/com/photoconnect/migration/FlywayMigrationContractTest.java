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
                    .as("Must retain the complete migration history through V034")
                    .hasSizeGreaterThanOrEqualTo(26)
                    .extracting(path -> path.getFileName().toString())
                    .contains("V034__configure_package_pricing_and_payment_order_integrity.sql");

            long distinctVersions = migrationScripts.stream()
                    .map(path -> path.getFileName().toString().substring(0, 4))
                    .distinct()
                    .count();
            assertThat(distinctVersions)
                    .as("Every Flyway migration version must be unique")
                    .isEqualTo(migrationScripts.size());

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
    @DisplayName("Latest migrations V022 through V030 define expected PhotoConnect feature tables and constraints")
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

        // V027: Manager operations & audit logs
        String v027 = Files.readString(MIGRATIONS.resolve("V027__add_manager_operations.sql"));
        assertThat(v027)
                .contains("operational_audit_logs")
                .contains("support_tickets")
                .contains("support_ticket_messages")
                .contains("assigned_manager_id")
                .contains("investigation_notes")
                .contains("FK_saved_photographers_profile");

        // V028: forward-only reconciliation for Booking optimistic locking
        String v028 = Files.readString(MIGRATIONS.resolve("V028__add_booking_optimistic_lock_version.sql"));
        assertThat(v028)
                .contains("ALTER TABLE dbo.bookings ADD version BIGINT NULL")
                .contains("sp_executesql N'UPDATE dbo.bookings SET version = 0 WHERE version IS NULL;")
                .contains("sp_executesql N'ALTER TABLE dbo.bookings ALTER COLUMN version BIGINT NOT NULL;")
                .contains("DF_bookings_version");

        // V029: pending accounts must be persisted before OTP verification.
        String v029 = Files.readString(MIGRATIONS.resolve("V029__allow_pending_user_verification_status.sql"));
        assertThat(v029)
                .contains("c.name = 'status'")
                .contains("DROP CONSTRAINT")
                .contains("CONSTRAINT CK_users_status")
                .contains("'PENDING_VERIFICATION'");

        // V030: native OIDC identity mapping must reject duplicate provider links per user.
        String v030 = Files.readString(MIGRATIONS.resolve("V030__harden_external_identity_uniqueness.sql"));
        assertThat(v030)
                .contains("UQ_external_auth_user_provider")
                .contains("UNIQUE (user_id, provider)")
                .contains("THROW 51031");
    }

    @Test
    @DisplayName("Migration configuration validates checksums without automatically repairing schema history")
    void migrationStartup_doesNotHideChecksumDrift() throws IOException {
        String v013 = Files.readString(MIGRATIONS.resolve("V013__create_saved_photographers.sql"));
        assertThat(v013)
                .as("V013 FK_saved_photographers_profile must use ON DELETE NO ACTION to prevent SQL Server Error 1785 on fresh install")
                .contains("REFERENCES dbo.photographer_profiles(id) ON DELETE NO ACTION");

        String v027 = Files.readString(MIGRATIONS.resolve("V027__add_manager_operations.sql"));
        assertThat(v027)
                .as("V027 must provide idempotent constraint alignment for existing databases")
                .contains("ALTER TABLE dbo.saved_photographers DROP CONSTRAINT FK_saved_photographers_profile")
                .contains("REFERENCES dbo.photographer_profiles(id) ON DELETE NO ACTION");

        Path flywayConfig = ROOT.resolve("src/main/java/com/photoconnect/config/FlywayConfig.java");
        assertThat(Files.exists(flywayConfig))
                .as("No custom startup strategy may hide checksum mismatches with flyway.repair()")
                .isFalse();

        Path mainJava = ROOT.resolve("src/main/java");
        try (var files = Files.walk(mainJava)) {
            List<Path> repairCallers = files
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> {
                        try {
                            return Files.readString(path).contains("flyway.repair(");
                        } catch (IOException exception) {
                            throw new java.io.UncheckedIOException(exception);
                        }
                    })
                    .toList();
            assertThat(repairCallers)
                    .as("Application startup must fail explicitly on migration checksum drift")
                    .isEmpty();
        }

        String properties = Files.readString(ROOT.resolve("src/main/resources/application.properties"));
        assertThat(properties).contains("spring.flyway.validate-on-migrate=true");
    }
}
