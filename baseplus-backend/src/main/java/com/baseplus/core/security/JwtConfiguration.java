package com.baseplus.core.security;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class JwtConfiguration {

    static final long ACCESS_TOKEN_LIFETIME_SECONDS = 3600L;
    private static final Pattern NUMERIC_SUBJECT = Pattern.compile("[1-9][0-9]*");

    @Bean
    SecretKey jwtSecretKey(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET deve possuir pelo menos 256 bits.");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    Clock jwtClock() {
        return Clock.systemUTC();
    }

    @Bean
    JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.audience}") String audience,
            @Value("${jwt.clock-skew}") Duration clockSkew,
            @Value("${jwt.legacy.accept-until}") Instant legacyAcceptUntil,
            Clock jwtClock
    ) {
        return createDecoder(jwtSecretKey, issuer, audience, clockSkew, legacyAcceptUntil, jwtClock);
    }

    static JwtDecoder createDecoder(
            SecretKey key,
            String issuer,
            String audience,
            Duration clockSkew,
            Instant legacyAcceptUntil,
            Clock clock
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
                .build();
        JwtTimestampValidator timestamps = new JwtTimestampValidator(clockSkew);
        timestamps.setClock(clock);
        decoder.setJwtValidator(token -> {
            OAuth2TokenValidatorResult timestampResult = timestamps.validate(token);
            if (timestampResult.hasErrors()) {
                return timestampResult;
            }
            return validateClaims(token, issuer, audience, clockSkew, legacyAcceptUntil, clock.instant());
        });
        return decoder;
    }

    private static OAuth2TokenValidatorResult validateClaims(
            Jwt token,
            String issuer,
            String audience,
            Duration clockSkew,
            Instant legacyAcceptUntil,
            Instant now
    ) {
        if (token.getExpiresAt() == null || token.getIssuedAt() == null || token.getSubject() == null
                || !token.getExpiresAt().isAfter(token.getIssuedAt())
                || token.getIssuedAt().isAfter(now.plus(clockSkew))) {
            return invalidToken();
        }

        Map<String, Object> claims = token.getClaims();
        boolean hasIssuer = claims.containsKey("iss");
        boolean hasAudience = claims.containsKey("aud");

        if (hasIssuer && hasAudience) {
            if (!issuer.equals(String.valueOf(token.getClaims().get("iss")))
                    || token.getAudience() == null
                    || !token.getAudience().contains(audience)) {
                return invalidToken();
            }
            return OAuth2TokenValidatorResult.success();
        }

        if (hasIssuer || hasAudience || !"HS256".equals(token.getHeaders().get("alg"))
                || !"JWT".equals(token.getHeaders().get("typ"))
                || !NUMERIC_SUBJECT.matcher(token.getSubject()).matches()
                || !(claims.get("roles") instanceof List<?> roles)
                || !(claims.get("permissions") instanceof List<?> permissions)
                || !roles.stream().allMatch(String.class::isInstance)
                || !permissions.stream().allMatch(String.class::isInstance)
                || !token.getIssuedAt().isBefore(legacyAcceptUntil)
                || !now.isBefore(legacyAcceptUntil)
                || token.getExpiresAt().isAfter(token.getIssuedAt().plusSeconds(ACCESS_TOKEN_LIFETIME_SECONDS))) {
            return invalidToken();
        }

        return OAuth2TokenValidatorResult.success();
    }

    private static OAuth2TokenValidatorResult invalidToken() {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
    }
}
