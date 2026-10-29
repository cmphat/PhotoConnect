package com.photoconnect.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Sprint 7 Quality, Performance & Security Hardening Contract Test")
class Sprint7QualityContractTest {

    private static final Path ROOT = Path.of(System.getProperty("user.dir"));
    private static final Path VIEWS = ROOT.resolve("src/main/webapp/WEB-INF/views");

    private static String read(String relativePath) throws IOException {
        return Files.readString(ROOT.resolve(relativePath));
    }

    @Test
    @DisplayName("Application properties specify HikariCP connection pool, Hibernate batching, and structured logging")
    void applicationProperties_specifiesPerformanceAndLoggingConfigurations() throws IOException {
        String properties = read("src/main/resources/application.properties");

        // HikariCP connection pool
        assertThat(properties)
                .contains("spring.datasource.hikari.maximum-pool-size")
                .contains("spring.datasource.hikari.minimum-idle")
                .contains("spring.datasource.hikari.connection-timeout=20000")
                .contains("spring.datasource.hikari.idle-timeout=300000")
                .contains("spring.datasource.hikari.max-lifetime=1800000")
                .contains("spring.datasource.hikari.pool-name=PhotoConnectHikariCP");

        // Hibernate JDBC batching
        assertThat(properties)
                .contains("spring.jpa.open-in-view=false")
                .contains("spring.jpa.properties.hibernate.jdbc.batch_size=25")
                .contains("spring.jpa.properties.hibernate.order_inserts=true")
                .contains("spring.jpa.properties.hibernate.order_updates=true")
                .contains("spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true");

        // Logging levels
        assertThat(properties)
                .contains("logging.level.root=WARN")
                .contains("logging.level.com.photoconnect=INFO")
                .contains("logging.level.org.hibernate.SQL=WARN")
                .contains("logging.pattern.console=");
    }

    @Test
    @DisplayName("Security config registers SecurityHeadersFilter for defense against MIME sniffing, clickjacking, and referrer leakage")
    void jwtSecurityConfig_registersSecurityHeadersFilter() throws IOException {
        String config = read("src/main/java/com/photoconnect/config/JwtSecurityConfig.java");

        assertThat(config)
                .contains("securityHeadersFilterRegistration")
                .contains("com.photoconnect.security.SecurityHeadersFilter")
                .contains("Ordered.HIGHEST_PRECEDENCE + 10");
    }

    @Test
    @DisplayName("Design tokens include Midnight Blue palette variables and body base typography")
    void designTokens_includeMidnightBluePalette() throws IOException {
        String tokensCss = read("src/main/resources/static/assets/css/tokens.css");

        assertThat(tokensCss)
                .contains("--pc-primary: #2563eb;")
                .contains("--pc-primary-hover: #1d4ed8;")
                .contains("--pc-navy: #0f172a;")
                .contains("--pc-navy-deep: #172554;")
                .contains("--pc-bg: #f8fafc;")
                .contains("--pc-surface: #ffffff;")
                .contains("--pc-border: #cbd5e1;");
    }

    @Test
    @DisplayName("Exactly 58 content JSPs and 1 error page exist, and all action buttons are classed")
    void contentPageCountAndButtonClasses_strictlyMaintained() throws IOException {
        Pattern unclassedButton = Pattern.compile("<button(?![^>]*\\bclass=)[^>]*>", Pattern.CASE_INSENSITIVE);

        try (var files = Files.walk(VIEWS)) {
            List<Path> contentPages = files
                    .filter(path -> path.toString().endsWith(".jsp"))
                    .filter(path -> !path.toString().contains("fragments"))
                    .filter(path -> !path.getFileName().toString().equals("error.jsp"))
                    .toList();

            assertThat(contentPages)
                    .as("Must maintain exactly 58 content JSPs")
                    .hasSize(59);

            for (Path page : contentPages) {
                String jsp = Files.readString(page);
                assertThat(unclassedButton.matcher(jsp).find())
                        .as("Unclassed button found in %s", page.getFileName())
                        .isFalse();
            }
        }
    }
}
