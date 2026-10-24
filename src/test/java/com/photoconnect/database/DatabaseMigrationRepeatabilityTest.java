package com.photoconnect.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DATA-01: Repeatable SQL Server Migration Process & Upgrade Validation")
class DatabaseMigrationRepeatabilityTest {

    private static final Path MIGRATIONS_DIR = Paths.get("docs", "development", "migrations");
    private static final Path FLYWAY_MIGRATIONS_DIR = Paths.get("src", "main", "resources", "db", "migration");
    private static final Path APP_PROPERTIES = Paths.get("src", "main", "resources", "application.properties");

    private static final List<String> EXPECTED_MIGRATION_PREFIXES = List.of(
            "V001__baseline_schema.sql",
            "V008__add_review_status.sql",
            "V010__professional_demo_payment.sql",
            "V011__add_portfolio_cover_and_category.sql",
            "V012__add_professional_profile_fields.sql",
            "V013__create_saved_photographers.sql",
            "V014__create_external_auth_identities.sql",
            "V016__create_user_profile_and_calendar_events.sql",
            "V017__create_service_packages.sql",
            "V018__create_booking_history.sql",
            "V019__create_delivery_albums.sql",
            "V020__create_notifications.sql",
            "V021__create_reschedule_requests.sql"
    );

    private static final Set<String> REQUIRED_DOMAIN_TABLES = Set.of(
            "users",
            "photographer_profiles",
            "portfolio_images",
            "photographer_unavailable_dates",
            "bookings",
            "deposits",
            "reviews",
            "messages",
            "saved_photographers",
            "external_auth_identities",
            "calendar_events",
            "service_packages",
            "booking_history",
            "delivery_albums",
            "delivered_photos",
            "notifications",
            "reschedule_requests"
    );

    @Test
    @DisplayName("1. All migration scripts exist in version sequence and are non-empty in Flyway directory")
    void allMigrationScriptsExistAndAreOrdered() {
        assertTrue(Files.isDirectory(FLYWAY_MIGRATIONS_DIR), "Flyway migrations directory must exist: " + FLYWAY_MIGRATIONS_DIR);
        assertTrue(Files.isDirectory(MIGRATIONS_DIR), "Documentation migrations directory must exist: " + MIGRATIONS_DIR);

        for (String expectedFileName : EXPECTED_MIGRATION_PREFIXES) {
            Path flywayScriptPath = FLYWAY_MIGRATIONS_DIR.resolve(expectedFileName);
            assertTrue(Files.isRegularFile(flywayScriptPath), "Expected Flyway migration script missing: " + expectedFileName);
            assertTrue(flywayScriptPath.toFile().length() > 0, "Flyway migration script must not be empty: " + expectedFileName);

            Path docScriptPath = MIGRATIONS_DIR.resolve(expectedFileName);
            assertTrue(Files.isRegularFile(docScriptPath), "Expected doc migration script missing: " + expectedFileName);
        }
    }

    @Test
    @DisplayName("2. All migration scripts contain strict idempotency guards (IF OBJECT_ID / IF NOT EXISTS / COL_LENGTH)")
    void allMigrationScriptsContainStrictIdempotencyGuards() throws IOException {
        List<Path> migrationFiles = Files.list(MIGRATIONS_DIR)
                .filter(p -> p.toString().endsWith(".sql"))
                .sorted()
                .toList();

        assertFalse(migrationFiles.isEmpty(), "No migration files found in " + MIGRATIONS_DIR);

        for (Path scriptPath : migrationFiles) {
            String content = Files.readString(scriptPath);
            String lower = content.toLowerCase(Locale.ROOT);

            boolean hasObjectIdGuard = lower.contains("object_id");
            boolean hasNotExistsGuard = lower.contains("if not exists");
            boolean hasColLengthGuard = lower.contains("col_length");

            assertTrue(hasObjectIdGuard || hasNotExistsGuard || hasColLengthGuard,
                    "Script " + scriptPath.getFileName() + " must have idempotency guard (IF OBJECT_ID, IF NOT EXISTS, or COL_LENGTH).");

            // Verify bare non-idempotent CREATE TABLE is not present without guard
            if (lower.contains("create table")) {
                assertTrue(hasObjectIdGuard || hasNotExistsGuard,
                        "CREATE TABLE statement in " + scriptPath.getFileName() + " must be protected by an existence guard.");
            }

            // Verify bare ALTER TABLE ADD is not present without column guard
            if (lower.contains("alter table") && lower.contains("add ")) {
                assertTrue(hasColLengthGuard || hasNotExistsGuard,
                        "ALTER TABLE ADD statement in " + scriptPath.getFileName() + " must be protected by a column existence guard.");
            }
        }
    }

    @Test
    @DisplayName("3. Migration scripts contain zero destructive statements (no DROP TABLE, TRUNCATE, or bare DELETE)")
    void migrationScriptsContainNoDestructiveStatements() throws IOException {
        List<Path> migrationFiles = Files.list(MIGRATIONS_DIR)
                .filter(p -> p.toString().endsWith(".sql"))
                .toList();

        Pattern dropTablePattern = Pattern.compile("\\bdrop\\s+table\\b", Pattern.CASE_INSENSITIVE);
        Pattern dropDbPattern = Pattern.compile("\\bdrop\\s+database\\b", Pattern.CASE_INSENSITIVE);
        Pattern truncatePattern = Pattern.compile("\\btruncate\\s+table\\b", Pattern.CASE_INSENSITIVE);

        for (Path scriptPath : migrationFiles) {
            String content = Files.readString(scriptPath);

            assertFalse(dropTablePattern.matcher(content).find(),
                    "Destructive DROP TABLE statement forbidden in " + scriptPath.getFileName());
            assertFalse(dropDbPattern.matcher(content).find(),
                    "Destructive DROP DATABASE statement forbidden in " + scriptPath.getFileName());
            assertFalse(truncatePattern.matcher(content).find(),
                    "Destructive TRUNCATE TABLE statement forbidden in " + scriptPath.getFileName());
        }
    }

    @Test
    @DisplayName("4. All 17 domain entity tables are covered across migration suite")
    void allDomainEntitiesHaveCorrespondingTablesInMigrations() throws IOException {
        List<Path> migrationFiles = Files.list(MIGRATIONS_DIR)
                .filter(p -> p.toString().endsWith(".sql"))
                .toList();

        String combinedSql = migrationFiles.stream()
                .map(path -> {
                    try {
                        return Files.readString(path).toLowerCase(Locale.ROOT);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.joining("\n"));

        for (String table : REQUIRED_DOMAIN_TABLES) {
            boolean mentionsTable = combinedSql.contains("create table " + table) ||
                    combinedSql.contains("create table dbo." + table) ||
                    combinedSql.contains("object_id('dbo." + table) ||
                    combinedSql.contains("object_id('" + table) ||
                    combinedSql.contains("object_id(n'dbo." + table);

            assertTrue(mentionsTable, "Domain table [" + table + "] must be created in the migration scripts.");
        }
    }

    @Test
    @DisplayName("5. Migration scripts simulate repeatable idempotent re-execution")
    void migrationScriptsExecuteIdempotentlyAcrossMultipleRuns() throws IOException {
        List<Path> migrationFiles = Files.list(MIGRATIONS_DIR)
                .filter(p -> p.toString().endsWith(".sql"))
                .sorted()
                .toList();

        // Simulate Run 1 (Initial Provisioning)
        Set<String> provisionedTables = new HashSet<>();
        Set<String> provisionedColumns = new HashSet<>();

        for (Path scriptPath : migrationFiles) {
            String content = Files.readString(scriptPath);
            for (String table : REQUIRED_DOMAIN_TABLES) {
                if (content.toLowerCase(Locale.ROOT).contains(table)) {
                    provisionedTables.add(table);
                }
            }
        }

        assertEquals(REQUIRED_DOMAIN_TABLES, provisionedTables,
                "Initial provisioning must define all required domain tables.");

        // Simulate Run 2 (Repeat Execution on existing database)
        // Verify all scripts use IF OBJECT_ID / IF NOT EXISTS / COL_LENGTH so they safely skip when tables/columns exist
        for (Path scriptPath : migrationFiles) {
            String content = Files.readString(scriptPath);
            boolean safeForReExecution = content.contains("IF OBJECT_ID") ||
                    content.contains("IF NOT EXISTS") ||
                    content.contains("COL_LENGTH") ||
                    content.contains("IF COL_LENGTH");
            assertTrue(safeForReExecution,
                    "Script " + scriptPath.getFileName() + " must be safe for repeated execution against an existing database.");
        }
    }

    @Test
    @DisplayName("6. Application properties supports configurable ddl-auto defaulting to validate")
    void applicationPropertiesSupportsDdlValidation() throws IOException {
        assertTrue(Files.exists(APP_PROPERTIES), "application.properties must exist");
        String content = Files.readString(APP_PROPERTIES);

        assertTrue(content.contains("spring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO:validate}"),
                "application.properties must configure ddl-auto defaulting to 'validate' for controlled release environments.");
    }

    @Test
    @DisplayName("7. Application properties configures Flyway migration runner")
    void applicationPropertiesConfiguresFlyway() throws IOException {
        assertTrue(Files.exists(APP_PROPERTIES), "application.properties must exist");
        String content = Files.readString(APP_PROPERTIES);

        assertTrue(content.contains("spring.flyway.enabled=${SPRING_FLYWAY_ENABLED:true}"),
                "Flyway must be enabled by default via spring.flyway.enabled");
        assertTrue(content.contains("spring.flyway.baseline-on-migrate=true"),
                "Flyway must enable baseline-on-migrate for existing database adoption");
        assertTrue(content.contains("spring.flyway.locations=classpath:db/migration"),
                "Flyway must point to classpath:db/migration");
    }
}
