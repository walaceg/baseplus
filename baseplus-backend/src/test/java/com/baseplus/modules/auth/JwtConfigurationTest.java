package com.baseplus.modules.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class JwtConfigurationTest {

    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Test
    void shouldRequireJwtSecretOutsideDevelopmentProfile() throws IOException {
        assertThat(getProperty("application.yml", "jwt.secret")).isEqualTo("${JWT_SECRET}");
    }

    @Test
    void shouldScopeDevelopmentJwtFallbackToDevelopmentProfile() throws IOException {
        assertThat(getProperty("application-dev.yml", "jwt.secret"))
                .isEqualTo("${JWT_SECRET:baseplus-dev-secret-change-before-production-2026}");
    }

    @Test
    void shouldConfigureJwtIssuerAudienceSkewAndLegacyDeadline() throws IOException {
        assertThat(getProperty("application.yml", "jwt.issuer")).isEqualTo("${JWT_ISSUER:https://baseplus.local}");
        assertThat(getProperty("application.yml", "jwt.audience")).isEqualTo("${JWT_AUDIENCE:baseplus-api}");
        assertThat(getProperty("application.yml", "jwt.clock-skew")).isEqualTo("${JWT_CLOCK_SKEW:PT30S}");
        assertThat(getProperty("application.yml", "jwt.legacy.accept-until"))
                .isEqualTo("${JWT_LEGACY_ACCEPT_UNTIL:2026-11-10T00:00:00Z}");
    }

    private Object getProperty(String resourceName, String propertyName) throws IOException {
        List<PropertySource<?>> propertySources = loader.load(
                resourceName,
                new ClassPathResource(resourceName)
        );
        return propertySources.stream()
                .map(propertySource -> propertySource.getProperty(propertyName))
                .filter(value -> value != null)
                .findFirst()
                .orElse(null);
    }
}
