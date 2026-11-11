package com.baseplus.core.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.BadJwtException;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.baseplus.modules.auth.service.JwtService;
import com.baseplus.modules.usuario.service.UsuarioService;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtTokenValidationTest {

    private static final Instant TRANSITION_END = Instant.parse("2026-11-10T00:00:00Z");
    private static final Instant NOW = Instant.parse("2026-11-09T12:00:00Z");
    private static final SecretKey KEY = key("0123456789abcdef0123456789abcdef");

    private JwtDecoder decoder;
    private JwtEncoder encoder;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        decoder = decoder(KEY, Clock.fixed(NOW, ZoneOffset.UTC));
        encoder = encoder(KEY);
        jwtService = new JwtService(encoder, decoder, "https://baseplus.local", "baseplus-api");
    }

    @Test
    void shouldAcceptValidHs256TokenWithIssuerAndAudience() {
        assertThat(decoder.decode(token(KEY, MacAlgorithm.HS256, "https://baseplus.local", "baseplus-api",
                NOW.minusSeconds(60), NOW.plusSeconds(3600)))).isNotNull();
    }

    @Test
    void shouldTreatOnlyMissingSessionClaimAsLegacy() {
        String legacy = legacyToken(KEY, NOW.minusSeconds(60), NOW.plusSeconds(3540));
        String withSession = tokenWithSessionId(KEY, "42", NOW.minusSeconds(60), NOW.plusSeconds(3600));

        assertThat(jwtService.getSessionId(legacy)).isNull();
        assertThat(jwtService.getSessionId(withSession)).isEqualTo(42L);
    }

    @Test
    void shouldRejectPresentMalformedSessionClaims() {
        List<Object> malformedClaims = List.of(
                "", "0", "-1", "9223372036854775808", 42L, List.of("42")
        );

        malformedClaims.forEach(sid -> {
            String malformed = tokenWithSessionId(KEY, sid, NOW.minusSeconds(60), NOW.plusSeconds(3600));
            assertThatThrownBy(() -> jwtService.getSessionId(malformed))
                    .isInstanceOf(BadJwtException.class);
        });
    }

    @Test
    void shouldNotTreatExpiredOrIncorrectlySignedTokenAsLegacy() {
        String expired = tokenWithSessionId(KEY, "42", NOW.minusSeconds(3600), NOW.minusSeconds(120));
        String incorrectlySigned = tokenWithSessionId(
                key("abcdef0123456789abcdef0123456789"), "42", NOW.minusSeconds(60), NOW.plusSeconds(3600));

        assertThatThrownBy(() -> jwtService.getSessionId(expired)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwtService.getSessionId(incorrectlySigned)).isInstanceOf(JwtException.class);
    }

    @Test
    void shouldNotAuthenticateRequestWithSignedMalformedSessionClaim() throws Exception {
        String malformed = tokenWithSessionId(KEY, 42L, NOW.minusSeconds(60), NOW.plusSeconds(3600));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + malformed);
        SecurityContextHolder.clearContext();

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService,
                org.mockito.Mockito.mock(UsuarioService.class));
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectExpiredTokenOutsideConfiguredClockSkew() {
        String expired = token(KEY, MacAlgorithm.HS256, "baseplus", "baseplus-api",
                NOW.minusSeconds(3600), NOW.minusSeconds(120));
        assertThatThrownBy(() -> decoder.decode(expired)).isInstanceOf(JwtException.class);
    }

    @Test
    void shouldAcceptExpirationWithinConfiguredClockSkew() {
        String withinSkew = token(KEY, MacAlgorithm.HS256, "https://baseplus.local", "baseplus-api",
                NOW.minusSeconds(3600), NOW.minusSeconds(15));
        assertThat(decoder.decode(withinSkew)).isNotNull();
    }

    @Test
    void shouldAcceptIssuedAtWithinClockSkew() {
        String token = token(KEY, MacAlgorithm.HS256, "https://baseplus.local", "baseplus-api",
                NOW.plusSeconds(29), NOW.plusSeconds(3600));
        assertThat(decoder.decode(token)).isNotNull();
    }

    @Test
    void shouldAcceptIssuedAtExactlyAtClockSkewLimit() {
        String token = token(KEY, MacAlgorithm.HS256, "https://baseplus.local", "baseplus-api",
                NOW.plusSeconds(30), NOW.plusSeconds(3600));
        assertThat(decoder.decode(token)).isNotNull();
    }

    @Test
    void shouldRejectIssuedAtBeyondClockSkew() {
        String token = token(KEY, MacAlgorithm.HS256, "https://baseplus.local", "baseplus-api",
                NOW.plusSeconds(31), NOW.plusSeconds(3600));
        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectInvalidSignature() {
        String incorrectlySigned = token(key("abcdef0123456789abcdef0123456789"),
                MacAlgorithm.HS256, "baseplus", "baseplus-api", NOW.minusSeconds(60), NOW.plusSeconds(3600));
        assertThatThrownBy(() -> decoder.decode(incorrectlySigned)).isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectAlgorithmOtherThanHs256() {
        String wrongAlgorithm = token(key("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"),
                MacAlgorithm.HS512, "https://baseplus.local", "baseplus-api",
                NOW.minusSeconds(60), NOW.plusSeconds(3600));
        assertThatThrownBy(() -> decoder.decode(wrongAlgorithm)).isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectIncorrectIssuer() {
        String wrongIssuer = token(KEY, MacAlgorithm.HS256, "https://other.local", "baseplus-api",
                NOW.minusSeconds(60), NOW.plusSeconds(3600));
        assertThatThrownBy(() -> decoder.decode(wrongIssuer)).isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectIncorrectAudience() {
        String wrongAudience = token(KEY, MacAlgorithm.HS256, "https://baseplus.local", "other-api",
                NOW.minusSeconds(60), NOW.plusSeconds(3600));
        assertThatThrownBy(() -> decoder.decode(wrongAudience)).isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectSecretShorterThan256Bits() {
        assertThatThrownBy(() -> new JwtConfiguration().jwtSecretKey("too-short"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("256 bits");
    }

    @Test
    void shouldAcceptRecognizedLegacyTokenDuringTransition() {
        String legacy = legacyToken(KEY, NOW.minusSeconds(1800), NOW.plusSeconds(1800));
        assertThat(decoder.decode(legacy).getSubject()).isEqualTo("42");
    }

    @Test
    void shouldRejectLegacyTokenAtTransitionDeadline() {
        Clock atDeadline = Clock.fixed(TRANSITION_END, ZoneOffset.UTC);
        JwtDecoder atDeadlineDecoder = decoder(KEY, atDeadline);
        String legacy = legacyToken(KEY, TRANSITION_END.minusSeconds(60), TRANSITION_END.plusSeconds(1800));

        assertThatThrownBy(() -> atDeadlineDecoder.decode(legacy)).isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectLegacyTokenAfterTransitionDeadline() {
        Clock afterDeadline = Clock.fixed(TRANSITION_END.plusSeconds(60), ZoneOffset.UTC);
        JwtDecoder expiredTransitionDecoder = decoder(KEY, afterDeadline);
        String legacy = legacyToken(KEY, TRANSITION_END.minusSeconds(60), TRANSITION_END.plusSeconds(1800));
        assertThatThrownBy(() -> expiredTransitionDecoder.decode(legacy)).isInstanceOf(JwtException.class);
    }

    @Test
    void shouldAcceptLegacyTokenImmediatelyBeforeTransitionDeadline() {
        Clock beforeDeadline = Clock.fixed(TRANSITION_END.minusSeconds(1), ZoneOffset.UTC);
        JwtDecoder beforeDeadlineDecoder = decoder(KEY, beforeDeadline);
        String legacy = legacyToken(KEY, TRANSITION_END.minusSeconds(3600), TRANSITION_END);

        assertThat(beforeDeadlineDecoder.decode(legacy).getSubject()).isEqualTo("42");
    }

    @Test
    void shouldRejectTokenMissingAudience() {
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("https://baseplus.local").subject("42")
                .issuedAt(NOW.minusSeconds(60)).expiresAt(NOW.plusSeconds(3600))
                .claim("roles", List.of()).claim("permissions", List.of()).build();
        String partialClaims = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(), claims)).getTokenValue();
        assertThatThrownBy(() -> decoder.decode(partialClaims)).isInstanceOf(JwtException.class);
    }

    private String legacyToken(SecretKey signingKey, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder().subject("42").issuedAt(issuedAt).expiresAt(expiresAt)
                .claim("roles", List.of("ADMIN")).claim("permissions", List.of("ADMIN_ACCESS")).build();
        return encoder(signingKey).encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(), claims)).getTokenValue();
    }

    private String tokenWithSessionId(SecretKey signingKey, Object sessionId, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("https://baseplus.local")
                .audience(List.of("baseplus-api")).subject("42").issuedAt(issuedAt).expiresAt(expiresAt)
                .claim("sid", sessionId).claim("roles", List.of()).claim("permissions", List.of()).build();
        return encoder(signingKey).encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(), claims)).getTokenValue();
    }

    private String token(SecretKey signingKey, MacAlgorithm algorithm, String issuer, String audience,
            Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).audience(List.of(audience)).subject("42")
                .issuedAt(issuedAt).expiresAt(expiresAt)
                .claim("roles", List.of("ADMIN")).claim("permissions", List.of("ADMIN_ACCESS")).build();
        return encoder(signingKey).encode(JwtEncoderParameters.from(
                JwsHeader.with(algorithm).type("JWT").build(), claims)).getTokenValue();
    }

    private JwtDecoder decoder(SecretKey secretKey, Clock clock) {
        return JwtConfiguration.createDecoder(secretKey, "https://baseplus.local", "baseplus-api",
                Duration.ofSeconds(30), TRANSITION_END, clock);
    }

    private JwtEncoder encoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    private static SecretKey key(String value) {
        return new SecretKeySpec(value.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
