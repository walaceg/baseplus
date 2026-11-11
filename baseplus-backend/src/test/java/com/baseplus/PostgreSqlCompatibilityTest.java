package com.baseplus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.baseplus.modules.auth.service.RefreshTokenHasher;
import com.baseplus.modules.auth.service.RefreshTokenCleanupJob;
import com.baseplus.modules.auth.service.JwtService;
import com.baseplus.modules.auth.service.RefreshTokenService;
import com.baseplus.modules.usuario.domain.Usuario;
import com.baseplus.modules.usuario.service.UsuarioService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres-test")
@Testcontainers(disabledWithoutDocker = true)
class PostgreSqlCompatibilityTest {

    private static final int EXPECTED_MIGRATIONS = 15;

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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenCleanupJob refreshTokenCleanupJob;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Test
    void shouldApplyFlywayMigrationsAndValidateJpaSchemaOnPostgreSql16() {
        String serverVersion = jdbcTemplate.queryForObject("show server_version", String.class);
        Integer successfulMigrations = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where success",
                Integer.class
        );
        Integer latestMigration = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where version = '15' and success",
                Integer.class
        );

        assertThat(serverVersion).startsWith("16.");
        assertThat(successfulMigrations).isEqualTo(EXPECTED_MIGRATIONS);
        assertThat(latestMigration).isEqualTo(1);
    }

    @Test
    void shouldUpgradeV14ToV15WithoutChangingExistingTokensOrSessions() throws Exception {
        String schema = "refresh_v15_upgrade_" + UUID.randomUUID().toString().replace("-", "");
        String rawToken = "existing-v14-refresh-token-" + UUID.randomUUID();
        String tokenHash = RefreshTokenHasher.sha256Hex(rawToken);
        OffsetDateTime expiresAt = OffsetDateTime.parse("2099-04-05T06:07:08Z");

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
                    .target(MigrationVersion.fromVersion("14"))
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
                    user.setString(1, "Usuário V14");
                    user.setString(2, "v14-" + schema + "@baseplus.test");
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
                        "insert into refresh_tokens (usuario_id, token_hash, session_id, expires_at) "
                                + "values (?, ?, ?, ?) returning id")) {
                    token.setLong(1, userId);
                    token.setString(2, tokenHash);
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
                try (PreparedStatement query = connection.prepareStatement(
                        "select id, usuario_id, session_id, token_hash, expires_at, consumed_at "
                                + "from refresh_tokens where id = ?")) {
                    query.setLong(1, tokenId);
                    try (ResultSet result = query.executeQuery()) {
                        assertThat(result.next()).isTrue();
                        assertThat(result.getLong("usuario_id")).isEqualTo(userId);
                        assertThat(result.getLong("session_id")).isEqualTo(sessionId);
                        assertThat(result.getString("token_hash")).isEqualTo(tokenHash);
                        assertThat(result.getObject("expires_at", OffsetDateTime.class).toInstant())
                                .isEqualTo(expiresAt.toInstant());
                        assertThat(result.getObject("consumed_at")).isNull();
                        assertThat(result.next()).isFalse();
                    }
                }
                assertThat(queryLong(connection, "select count(*) from user_sessions where id = " + sessionId))
                        .isEqualTo(1);
                assertThat(queryLong(connection,
                        "select count(*) from flyway_schema_history where version = '14' and success"))
                        .isEqualTo(1);
                assertThat(queryLong(connection,
                        "select count(*) from flyway_schema_history where version = '15' and success"))
                        .isEqualTo(1);
                assertThat(queryString(connection,
                        "select indexdef from pg_indexes where schemaname = current_schema() "
                                + "and indexname = 'ux_refresh_tokens_active_session'"))
                        .contains("WHERE (consumed_at IS NULL)");
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
    void shouldCleanOnlyRefreshTokensPastTheThirtyDayRetentionWindow() {
        Usuario usuario = usuarioService.salvar(new Usuario(
                "Refresh Cleanup Test",
                "refresh-cleanup-" + UUID.randomUUID() + "@baseplus.test",
                passwordEncoder.encode("Test-password-123"),
                true
        ));

        String retainedConsumedHash = RefreshTokenHasher.sha256Hex("retained-consumed-" + UUID.randomUUID());
        String eligibleConsumedHash = RefreshTokenHasher.sha256Hex("eligible-consumed-" + UUID.randomUUID());
        String retainedActiveHash = RefreshTokenHasher.sha256Hex("retained-active-" + UUID.randomUUID());
        String eligibleActiveHash = RefreshTokenHasher.sha256Hex("eligible-active-" + UUID.randomUUID());
        String unexpiredActiveHash = RefreshTokenHasher.sha256Hex("unexpired-active-" + UUID.randomUUID());

        insertCleanupToken(usuario.getId(), retainedConsumedHash, OffsetDateTime.now().minusDays(29),
                OffsetDateTime.now().minusDays(28));
        insertCleanupToken(usuario.getId(), eligibleConsumedHash, OffsetDateTime.now().minusDays(31),
                OffsetDateTime.now().minusDays(30));
        insertCleanupToken(usuario.getId(), retainedActiveHash, OffsetDateTime.now().minusDays(29), null);
        insertCleanupToken(usuario.getId(), eligibleActiveHash, OffsetDateTime.now().minusDays(31), null);
        insertCleanupToken(usuario.getId(), unexpiredActiveHash, OffsetDateTime.now().plusDays(1), null);

        refreshTokenCleanupJob.cleanupExpiredTokens();

        assertThat(refreshTokenExists(retainedConsumedHash)).isTrue();
        assertThat(refreshTokenExists(eligibleConsumedHash)).isFalse();
        assertThat(refreshTokenExists(retainedActiveHash)).isTrue();
        assertThat(refreshTokenExists(eligibleActiveHash)).isFalse();
        assertThat(refreshTokenExists(unexpiredActiveHash)).isTrue();
    }

    @Test
    void shouldPersistReuseAuditAndKeepSuccessorUsable() throws Exception {
        String email = "refresh-reuse-" + UUID.randomUUID() + "@baseplus.test";
        usuarioService.salvar(new Usuario(
                "Refresh Reuse Test",
                email,
                passwordEncoder.encode("Test-password-123"),
                true
        ));

        MvcResult login = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Test-password-123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String originalToken = objectMapper.readTree(login.getResponse().getContentAsString())
                .path("data").path("refreshToken").asText();
        String originalHash = RefreshTokenHasher.sha256Hex(originalToken);
        Long sessionId = jdbcTemplate.queryForObject(
                "select session_id from refresh_tokens where token_hash = ?",
                Long.class,
                originalHash
        );

        MvcResult firstRefresh = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + originalToken + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String successorToken = objectMapper.readTree(firstRefresh.getResponse().getContentAsString())
                .path("data").path("refreshToken").asText();
        assertThat(successorToken).isNotBlank();

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + originalToken + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token inválido."));

        Integer auditCount = jdbcTemplate.queryForObject(
                "select count(*) from audit_logs where acao = 'REFRESH_TOKEN_REUSE' "
                        + "and entidade = 'USER_SESSION' and entidade_id = ?",
                Integer.class,
                sessionId
        );
        assertThat(auditCount).isEqualTo(1);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + successorToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void shouldSerializeConcurrentRefreshRequestsUsingTheSameToken() throws Exception {
        LoginFixture login = createLoginFixture();

        List<MvcResult> responses = refreshConcurrently(login.refreshToken(), login.refreshToken());

        assertThat(responses).extracting(result -> result.getResponse().getStatus())
                .containsExactlyInAnyOrder(200, 401);
        String successor = refreshTokenFrom(successfulResponse(responses));
        assertThat(activeTokenCount(login.sessionId())).isEqualTo(1);
        assertThat(consumedTokenCount(login.sessionId())).isEqualTo(1);
        assertThat(reuseAuditCount(login.sessionId())).isEqualTo(1);
        assertThat(sessionCount(login.sessionId())).isEqualTo(1);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(successor)))
                .andExpect(status().isOk());
        assertThat(activeTokenCount(login.sessionId())).isEqualTo(1);
    }

    @Test
    void shouldSerializeConcurrentRefreshesWithAnOldAndCurrentTokenInOneFamily() throws Exception {
        LoginFixture login = createLoginFixture();
        String currentToken = refreshOnce(login.refreshToken());

        List<MvcResult> responses = refreshConcurrently(login.refreshToken(), currentToken);

        assertThat(responses).extracting(result -> result.getResponse().getStatus())
                .containsExactlyInAnyOrder(200, 401);
        String nextToken = refreshTokenFrom(successfulResponse(responses));
        assertThat(activeTokenCount(login.sessionId())).isEqualTo(1);
        assertThat(consumedTokenCount(login.sessionId())).isEqualTo(2);
        assertThat(reuseAuditCount(login.sessionId())).isEqualTo(1);
        assertThat(sessionCount(login.sessionId())).isEqualTo(1);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(nextToken)))
                .andExpect(status().isOk());
        assertThat(activeTokenCount(login.sessionId())).isEqualTo(1);
    }

    @Test
    void shouldEnforceOneActiveTokenPerSessionAndRetainConsumedHistory() throws Exception {
        LoginFixture login = createLoginFixture();
        String successor = refreshOnce(login.refreshToken());
        String duplicateHash = RefreshTokenHasher.sha256Hex("duplicate-active-" + UUID.randomUUID());

        org.junit.jupiter.api.Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        "insert into refresh_tokens (usuario_id, token_hash, session_id, expires_at) "
                                + "select usuario_id, ?, session_id, expires_at from refresh_tokens "
                                + "where token_hash = ?",
                        duplicateHash,
                        RefreshTokenHasher.sha256Hex(successor)
                ));

        assertThat(activeTokenCount(login.sessionId())).isEqualTo(1);
        assertThat(consumedTokenCount(login.sessionId())).isEqualTo(1);
    }

    @Test
    void shouldIssueSessionIdOnLoginAndPreserveItOnRefresh() throws Exception {
        LoginFixture login = createLoginFixture();
        String firstAccessToken = login.accessToken();

        assertThat(jwtService.getSessionId(firstAccessToken)).isEqualTo(login.sessionId());

        MvcResult refreshed = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(login.refreshToken())))
                .andExpect(status().isOk())
                .andReturn();
        String nextAccessToken = objectMapper.readTree(refreshed.getResponse().getContentAsString())
                .path("data").path("token").asText();
        assertThat(jwtService.getSessionId(nextAccessToken)).isEqualTo(login.sessionId());
    }

    @Test
    void shouldLogoutOnlyTheSessionFromItsAccessTokenAfterAnotherLogin() throws Exception {
        String email = "logout-session-" + UUID.randomUUID() + "@baseplus.test";
        createUser(email);
        JsonNode first = loginFor(email);
        JsonNode second = loginFor(email);
        String firstAccess = first.path("data").path("token").asText();
        String firstRefresh = first.path("data").path("refreshToken").asText();
        String secondAccess = second.path("data").path("token").asText();
        String secondRefresh = second.path("data").path("refreshToken").asText();
        Long firstSession = jwtService.getSessionId(firstAccess);
        Long secondSession = jwtService.getSessionId(secondAccess);
        assertThat(firstSession).isNotEqualTo(secondSession);

        mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer " + firstAccess))
                .andExpect(status().isOk());
        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(firstRefresh)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(secondRefresh)))
                .andExpect(status().isOk());

        assertThat(activeTokenCount(firstSession)).isZero();
        assertThat(activeTokenCount(secondSession)).isEqualTo(1);
    }

    @Test
    void shouldNotRevokeAnySessionForLegacyJwtWithoutSessionId() throws Exception {
        LoginFixture login = createLoginFixture();
        Long userId = jdbcTemplate.queryForObject(
                "select usuario_id from user_sessions where id = ?", Long.class, login.sessionId());
        String legacyAccess = jwtService.generateToken(userId.toString(), List.of(), List.of(), null);

        mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer " + legacyAccess))
                .andExpect(status().isOk());
        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(login.refreshToken())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotRevokeWhenSessionIdIsMissingOrOwnedByAnotherUser() throws Exception {
        String firstEmail = "logout-owner-a-" + UUID.randomUUID() + "@baseplus.test";
        String secondEmail = "logout-owner-b-" + UUID.randomUUID() + "@baseplus.test";
        Usuario firstUser = createUser(firstEmail);
        createUser(secondEmail);
        JsonNode firstLogin = loginFor(firstEmail);
        JsonNode secondLogin = loginFor(secondEmail);
        String firstRefresh = firstLogin.path("data").path("refreshToken").asText();
        String secondRefresh = secondLogin.path("data").path("refreshToken").asText();
        Long otherSessionId = jwtService.getSessionId(secondLogin.path("data").path("token").asText());

        String missingSession = jwtService.generateToken(firstUser.getId().toString(), List.of(), List.of(), 999999999L);
        mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer " + missingSession))
                .andExpect(status().isOk());
        String foreignSession = jwtService.generateToken(firstUser.getId().toString(), List.of(), List.of(),
                otherSessionId);
        mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer " + foreignSession))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(firstRefresh)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(secondRefresh)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldKeepLogoutIdempotentAndPreserveExplicitGlobalRevocation() throws Exception {
        String email = "logout-global-" + UUID.randomUUID() + "@baseplus.test";
        Usuario usuario = createUser(email);
        JsonNode first = loginFor(email);
        JsonNode second = loginFor(email);
        String firstAccess = first.path("data").path("token").asText();
        String firstRefresh = first.path("data").path("refreshToken").asText();
        String secondAccess = second.path("data").path("token").asText();
        String secondRefresh = second.path("data").path("refreshToken").asText();

        mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer " + firstAccess))
                .andExpect(status().isOk());
        mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer " + firstAccess))
                .andExpect(status().isOk());
        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(firstRefresh)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(secondRefresh)))
                .andExpect(status().isOk());

        refreshTokenService.removerPorUsuario(usuario);
        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(secondRefresh)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldSerializeLogoutAndRefreshForTheSameSession() throws Exception {
        LoginFixture login = createLoginFixture();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(3);
        try {
            Future<MvcResult> logout = executor.submit(() -> {
                barrier.await(10, TimeUnit.SECONDS);
                return mockMvc.perform(post("/auth/logout")
                                .header("Authorization", "Bearer " + login.accessToken()))
                        .andReturn();
            });
            Future<MvcResult> refresh = executor.submit(() -> {
                barrier.await(10, TimeUnit.SECONDS);
                return mockMvc.perform(post("/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshPayload(login.refreshToken())))
                        .andReturn();
            });
            barrier.await(10, TimeUnit.SECONDS);
            assertThat(logout.get(30, TimeUnit.SECONDS).getResponse().getStatus()).isEqualTo(200);
            assertThat(refresh.get(30, TimeUnit.SECONDS).getResponse().getStatus()).isIn(200, 401);
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
        assertThat(activeTokenCount(login.sessionId())).isZero();
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

    private LoginFixture createLoginFixture() throws Exception {
        String email = "refresh-concurrency-" + UUID.randomUUID() + "@baseplus.test";
        createUser(email);
        MvcResult login = performLogin(email);
        String refreshToken = refreshTokenFrom(login);
        String accessToken = objectMapper.readTree(login.getResponse().getContentAsString())
                .path("data").path("token").asText();
        Long sessionId = jdbcTemplate.queryForObject(
                "select session_id from refresh_tokens where token_hash = ?",
                Long.class,
                RefreshTokenHasher.sha256Hex(refreshToken)
        );
        return new LoginFixture(accessToken, refreshToken, sessionId);
    }

    private Usuario createUser(String email) {
        return usuarioService.salvar(new Usuario(
                "Refresh Session Test",
                email,
                passwordEncoder.encode("Test-password-123"),
                true
        ));
    }

    private JsonNode loginFor(String email) throws Exception {
        return objectMapper.readTree(performLogin(email).getResponse().getContentAsString());
    }

    private MvcResult performLogin(String email) throws Exception {
        return mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Test-password-123\"}"))
                .andExpect(status().isOk())
                .andReturn();
    }

    private void insertCleanupToken(
            Long userId,
            String tokenHash,
            OffsetDateTime expiresAt,
            OffsetDateTime consumedAt
    ) {
        Long sessionId = jdbcTemplate.queryForObject(
                "insert into user_sessions (usuario_id) values (?) returning id",
                Long.class,
                userId
        );
        if (consumedAt == null) {
            jdbcTemplate.update(
                    "insert into refresh_tokens (usuario_id, token_hash, session_id, expires_at) values (?, ?, ?, ?)",
                    userId,
                    tokenHash,
                    sessionId,
                    expiresAt
            );
            return;
        }
        jdbcTemplate.update(
                "insert into refresh_tokens (usuario_id, token_hash, session_id, expires_at, consumed_at) "
                        + "values (?, ?, ?, ?, ?)",
                userId,
                tokenHash,
                sessionId,
                expiresAt,
                consumedAt
        );
    }

    private boolean refreshTokenExists(String tokenHash) {
        return jdbcTemplate.queryForObject(
                "select exists(select 1 from refresh_tokens where token_hash = ?)",
                Boolean.class,
                tokenHash
        );
    }

    private String refreshOnce(String refreshToken) throws Exception {
        MvcResult response = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(refreshToken)))
                .andExpect(status().isOk())
                .andReturn();
        return refreshTokenFrom(response);
    }

    private List<MvcResult> refreshConcurrently(String firstToken, String secondToken) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier startBarrier = new CyclicBarrier(3);
        try {
            Future<MvcResult> first = executor.submit(concurrentRefresh(firstToken, startBarrier));
            Future<MvcResult> second = executor.submit(concurrentRefresh(secondToken, startBarrier));
            startBarrier.await(10, TimeUnit.SECONDS);
            return List.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    private Callable<MvcResult> concurrentRefresh(String token, CyclicBarrier startBarrier) {
        return () -> {
            startBarrier.await(10, TimeUnit.SECONDS);
            return mockMvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(refreshPayload(token)))
                    .andReturn();
        };
    }

    private String refreshPayload(String token) {
        return "{\"refreshToken\":\"" + token + "\"}";
    }

    private String refreshTokenFrom(MvcResult response) throws Exception {
        return objectMapper.readTree(response.getResponse().getContentAsString())
                .path("data").path("refreshToken").asText();
    }

    private MvcResult successfulResponse(List<MvcResult> responses) {
        return responses.stream()
                .filter(result -> result.getResponse().getStatus() == 200)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Nenhuma rotação concorrente foi bem-sucedida."));
    }

    private int activeTokenCount(Long sessionId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from refresh_tokens where session_id = ? and consumed_at is null",
                Integer.class,
                sessionId
        );
    }

    private int consumedTokenCount(Long sessionId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from refresh_tokens where session_id = ? and consumed_at is not null",
                Integer.class,
                sessionId
        );
    }

    private int reuseAuditCount(Long sessionId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from audit_logs where acao = 'REFRESH_TOKEN_REUSE' "
                        + "and entidade = 'USER_SESSION' and entidade_id = ?",
                Integer.class,
                sessionId
        );
    }

    private int sessionCount(Long sessionId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from user_sessions where id = ?",
                Integer.class,
                sessionId
        );
    }

    private record LoginFixture(String accessToken, String refreshToken, Long sessionId) {
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
