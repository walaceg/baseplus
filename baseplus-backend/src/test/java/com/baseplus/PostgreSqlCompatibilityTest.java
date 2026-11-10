package com.baseplus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.baseplus.modules.auth.service.RefreshTokenHasher;

@SpringBootTest
@ActiveProfiles("postgres-test")
@Testcontainers(disabledWithoutDocker = true)
class PostgreSqlCompatibilityTest {

    private static final int EXPECTED_MIGRATIONS = 14;

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("baseplus_test")
            .withUsername("baseplus")
            .withPassword("baseplus");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldApplyFlywayMigrationsAndValidateJpaSchemaOnPostgreSql16() {
        String serverVersion = jdbcTemplate.queryForObject("show server_version", String.class);
        Integer successfulMigrations = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where success",
                Integer.class
        );
        Integer latestMigration = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where version = '14' and success",
                Integer.class
        );

        assertThat(serverVersion).startsWith("16.");
        assertThat(successfulMigrations).isEqualTo(EXPECTED_MIGRATIONS);
        assertThat(latestMigration).isEqualTo(1);
    }

    @Test
    void shouldMigrateExistingRefreshTokensToHashesWithoutLosingSessions() throws Exception {
        String schema = "refresh_hash_" + UUID.randomUUID().toString().replace("-", "");
        String rawToken = "existing-refresh-token-before-hash-migration";
        String expectedHash = RefreshTokenHasher.sha256Hex(rawToken);
        OffsetDateTime expiresAt = OffsetDateTime.parse("2099-02-03T04:05:06Z");

        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("create schema " + schema);
        }

        try {
            Flyway.configure()
                    .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                    .schemas(schema)
                    .defaultSchema(schema)
                    .target(MigrationVersion.fromVersion("13"))
                    .load()
                    .migrate();

            long userId;
            long sessionId;
            long tokenId;
            try (Connection connection = DriverManager.getConnection(
                    POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
                setSchema(connection, schema);
                try (PreparedStatement user = connection.prepareStatement(
                        "insert into usuarios (nome, email, senha) values (?, ?, ?) returning id")) {
                    user.setString(1, "Usuário Migração");
                    user.setString(2, "migration-" + schema + "@baseplus.test");
                    user.setString(3, "senha-hash");
                    try (ResultSet result = user.executeQuery()) {
                        result.next();
                        userId = result.getLong(1);
                    }
                }
                try (PreparedStatement session = connection.prepareStatement(
                        "insert into user_sessions (usuario_id) values (?) returning id")) {
                    session.setLong(1, userId);
                    try (ResultSet result = session.executeQuery()) {
                        result.next();
                        sessionId = result.getLong(1);
                    }
                }
                try (PreparedStatement token = connection.prepareStatement(
                        "insert into refresh_tokens (usuario_id, token, session_id, expires_at) "
                                + "values (?, ?, ?, ?) returning id")) {
                    token.setLong(1, userId);
                    token.setString(2, rawToken);
                    token.setLong(3, sessionId);
                    token.setObject(4, expiresAt);
                    try (ResultSet result = token.executeQuery()) {
                        result.next();
                        tokenId = result.getLong(1);
                    }
                }
            }

            Flyway.configure()
                    .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                    .schemas(schema)
                    .defaultSchema(schema)
                    .load()
                    .migrate();

            try (Connection connection = DriverManager.getConnection(
                    POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
                setSchema(connection, schema);
                try (Statement statement = connection.createStatement();
                     ResultSet result = statement.executeQuery(
                             "select id, usuario_id, session_id, expires_at, token_hash from refresh_tokens")) {
                    assertThat(result.next()).isTrue();
                    assertThat(result.getLong("id")).isEqualTo(tokenId);
                    assertThat(result.getLong("usuario_id")).isEqualTo(userId);
                    assertThat(result.getLong("session_id")).isEqualTo(sessionId);
                    assertThat(result.getObject("expires_at", OffsetDateTime.class).toInstant())
                            .isEqualTo(expiresAt.toInstant());
                    assertThat(result.getString("token_hash")).isEqualTo(expectedHash).isNotEqualTo(rawToken);
                    assertThat(result.next()).isFalse();
                }
                assertThat(queryLong(connection, "select count(*) from user_sessions")).isEqualTo(1);
                assertThat(queryLong(connection, "select count(*) from refresh_tokens")).isEqualTo(1);
                assertThat(queryLong(connection,
                        "select count(*) from information_schema.columns "
                                + "where table_schema = current_schema() and table_name = 'refresh_tokens' "
                                + "and column_name = 'token'"))
                        .isZero();
                assertThat(queryLong(connection,
                        "select count(*) from pg_indexes where schemaname = current_schema() "
                                + "and indexname = 'ux_refresh_tokens_token_hash'"))
                        .isEqualTo(1);

                long secondSessionId;
                try (PreparedStatement session = connection.prepareStatement(
                        "insert into user_sessions (usuario_id) values (?) returning id")) {
                    session.setLong(1, userId);
                    try (ResultSet result = session.executeQuery()) {
                        result.next();
                        secondSessionId = result.getLong(1);
                    }
                }
                try (PreparedStatement duplicate = connection.prepareStatement(
                        "insert into refresh_tokens (usuario_id, token_hash, session_id, expires_at) "
                                + "values (?, ?, ?, current_timestamp + interval '7 days')")) {
                    duplicate.setLong(1, userId);
                    duplicate.setString(2, expectedHash);
                    duplicate.setLong(3, secondSessionId);
                    assertThrows(SQLException.class, duplicate::executeUpdate);
                }
            }
        } finally {
            try (Connection connection = DriverManager.getConnection(
                    POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
                 Statement statement = connection.createStatement()) {
                statement.execute("drop schema if exists " + schema + " cascade");
            }
        }
    }

    @Test
    void shouldRollbackV14CompletelyWhenHashMigrationFails() throws Exception {
        String schema = "refresh_rollback_" + UUID.randomUUID().toString().replace("-", "");
        String rawToken = "token-preserved-after-rollback";

        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("create schema " + schema);
        }

        try {
            Flyway.configure()
                    .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                    .schemas(schema)
                    .defaultSchema(schema)
                    .target(MigrationVersion.fromVersion("13"))
                    .load()
                    .migrate();

            try (Connection connection = DriverManager.getConnection(
                    POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
                setSchema(connection, schema);
                long userId;
                try (PreparedStatement user = connection.prepareStatement(
                        "insert into usuarios (nome, email, senha) values (?, ?, ?) returning id")) {
                    user.setString(1, "Usuário Rollback");
                    user.setString(2, "rollback-" + schema + "@baseplus.test");
                    user.setString(3, "senha-hash");
                    try (ResultSet result = user.executeQuery()) {
                        result.next();
                        userId = result.getLong(1);
                    }
                }
                long sessionId;
                try (PreparedStatement session = connection.prepareStatement(
                        "insert into user_sessions (usuario_id) values (?) returning id")) {
                    session.setLong(1, userId);
                    try (ResultSet result = session.executeQuery()) {
                        result.next();
                        sessionId = result.getLong(1);
                    }
                }
                try (PreparedStatement token = connection.prepareStatement(
                        "insert into refresh_tokens (usuario_id, token, session_id, expires_at) "
                                + "values (?, ?, ?, current_timestamp + interval '7 days')")) {
                    token.setLong(1, userId);
                    token.setString(2, rawToken);
                    token.setLong(3, sessionId);
                    token.executeUpdate();
                }
                try (Statement statement = connection.createStatement()) {
                    statement.execute("create function " + schema + ".reject_refresh_hash() returns trigger "
                            + "language plpgsql as $$ begin raise exception 'forced migration failure'; end; $$");
                    statement.execute("create trigger reject_refresh_hash before update "
                            + "on refresh_tokens for each row execute function " + schema
                            + ".reject_refresh_hash()");
                }
            }

            Flyway failingFlyway = Flyway.configure()
                    .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                    .schemas(schema)
                    .defaultSchema(schema)
                    .load();
            assertThrows(FlywayException.class, failingFlyway::migrate);

            try (Connection connection = DriverManager.getConnection(
                    POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
                setSchema(connection, schema);
                assertThat(queryLong(connection,
                        "select count(*) from information_schema.columns where table_schema = current_schema() "
                                + "and table_name = 'refresh_tokens' and column_name = 'token_hash'"))
                        .isZero();
                assertThat(queryLong(connection,
                        "select count(*) from information_schema.columns where table_schema = current_schema() "
                                + "and table_name = 'refresh_tokens' and column_name = 'token'"))
                        .isEqualTo(1);
                assertThat(queryString(connection, "select token from refresh_tokens")).isEqualTo(rawToken);
                assertThat(queryLong(connection,
                        "select count(*) from flyway_schema_history where version = '13' and success"))
                        .isEqualTo(1);
                assertThat(queryLong(connection,
                        "select count(*) from flyway_schema_history where version = '14' and success"))
                        .isZero();
            }
        } finally {
            try (Connection connection = DriverManager.getConnection(
                    POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
                 Statement statement = connection.createStatement()) {
                statement.execute("drop schema if exists " + schema + " cascade");
            }
        }
    }

    private static void setSchema(Connection connection, String schema) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("set search_path to " + schema);
        }
    }

    private static long queryLong(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getLong(1);
        }
    }

    private static String queryString(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getString(1);
        }
    }
}
