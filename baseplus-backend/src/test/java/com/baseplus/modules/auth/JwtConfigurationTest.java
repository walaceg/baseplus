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
