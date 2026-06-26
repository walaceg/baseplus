package com.baseplus;

import static org.assertj.core.api.Assertions.assertThat;

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

@SpringBootTest
@ActiveProfiles("postgres-test")
@Testcontainers(disabledWithoutDocker = true)
class PostgreSqlCompatibilityTest {

    private static final int EXPECTED_MIGRATIONS = 13;

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
                "select count(*) from flyway_schema_history where version = '13' and success",
                Integer.class
        );

        assertThat(serverVersion).startsWith("16.");
        assertThat(successfulMigrations).isEqualTo(EXPECTED_MIGRATIONS);
        assertThat(latestMigration).isEqualTo(1);
    }
}
