package com.photoconnect.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnabledIfEnvironmentVariable(named = "RUN_SQLSERVER_INTEGRATION_TESTS", matches = "true")
class NativeOidcSqlServerIntegrationTest {

    @Test
    void v029DatabaseUpgradesToV030AndEnforcesIdentityUniqueness() throws Exception {
        String url = required("DB_URL");
        String username = required("DB_USERNAME");
        String password = required("DB_PASSWORD");

        Flyway.configure()
                .dataSource(url, username, password)
                .locations("classpath:db/migration")
                .target(MigrationVersion.fromVersion("29"))
                .cleanDisabled(true)
                .load()
                .migrate();

        Flyway upgraded = Flyway.configure()
                .dataSource(url, username, password)
                .locations("classpath:db/migration")
                .cleanDisabled(true)
                .load();
        upgraded.migrate();
        upgraded.validate();

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            assertThat(scalar(connection, """
                    SELECT COUNT(*) FROM sys.key_constraints
                    WHERE [name] = 'UQ_external_auth_user_provider'
                      AND [parent_object_id] = OBJECT_ID('dbo.external_auth_identities')
                    """)).isEqualTo(1L);
            assertThat(scalar(connection, """
                    SELECT COUNT(*) FROM dbo.flyway_schema_history
                    WHERE version = '030' AND success = 1
                    """)).isEqualTo(1L);

            connection.setAutoCommit(false);
            long userId = insertDisposableUser(connection);
            insertIdentity(connection, userId, "subject-one");
            assertThatThrownBy(() -> insertIdentity(connection, userId, "subject-two"))
                    .isInstanceOf(SQLException.class);
            connection.rollback();
        }
    }

    private long insertDisposableUser(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO dbo.users(email, password, full_name, role, status)
                VALUES (?, ?, ?, 'CUSTOMER', 'ACTIVE')
                """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, "oidc-integration-" + UUID.randomUUID() + "@example.invalid");
            statement.setString(2, "$2a$10$integrationOnlyHashNotARealCredential000000000000000000");
            statement.setString(3, "OIDC Integration");
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                assertThat(keys.next()).isTrue();
                return keys.getLong(1);
            }
        }
    }

    private void insertIdentity(Connection connection, long userId, String subject) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO dbo.external_auth_identities(user_id, provider, provider_subject)
                VALUES (?, 'GOOGLE', ?)
                """)) {
            statement.setLong(1, userId);
            statement.setString(2, subject);
            statement.executeUpdate();
        }
    }

    private long scalar(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            assertThat(result.next()).isTrue();
            return result.getLong(1);
        }
    }

    private String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalStateException(name + " is required");
        return value;
    }
}
