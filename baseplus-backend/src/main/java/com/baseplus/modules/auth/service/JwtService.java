package com.baseplus.modules.auth.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final long EXPIRATION_SECONDS = 3600L;

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final String issuer;
    private final String audience;

    public JwtService(
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.audience}") String audience
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.issuer = issuer;
        this.audience = audience;
    }

    public String generateToken(String subject) {
        return generateToken(subject, Collections.emptyList(), Collections.emptyList());
    }

    public String generateToken(String subject, List<String> roles) {
        return generateToken(subject, roles, Collections.emptyList());
    }

    public String generateToken(String subject, List<String> roles, List<String> permissions) {
        return generateToken(subject, roles, permissions, null);
    }

    public String generateToken(String subject, List<String> roles, List<String> permissions, Long sessionId) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(issuer)
                .audience(List.of(audience))
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(EXPIRATION_SECONDS))
                .claim("roles", roles == null ? Collections.emptyList() : roles)
                .claim("permissions", permissions == null ? Collections.emptyList() : permissions);
        if (sessionId != null) {
            claimsBuilder.claim("sid", sessionId.toString());
        }
        JwtClaimsSet claims = claimsBuilder.build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public boolean isTokenValid(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public String getSubject(String token) {
        try {
            return jwtDecoder.decode(token).getSubject();
        } catch (RuntimeException exception) {
            return null;
        }
    }

    public Long getSessionId(String token) {
        var claims = jwtDecoder.decode(token).getClaims();
        if (!claims.containsKey("sid")) {
            return null;
        }

        Object claim = claims.get("sid");
        if (!(claim instanceof String value) || !value.matches("[1-9][0-9]*")) {
            throw new BadJwtException("Invalid session identifier claim.");
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new BadJwtException("Invalid session identifier claim.");
        }
    }

    public List<String> getRoles(String token) {
        return getStringListClaim(token, "roles");
    }

    public List<String> getPermissions(String token) {
        return getStringListClaim(token, "permissions");
    }

    private List<String> getStringListClaim(String token, String claim) {
        try {
            List<String> values = jwtDecoder.decode(token).getClaimAsStringList(claim);
            return values == null ? Collections.emptyList() : values;
        } catch (RuntimeException exception) {
            return Collections.emptyList();
        }
    }

    public long getExpirationSeconds() {
        return EXPIRATION_SECONDS;
    }
}
