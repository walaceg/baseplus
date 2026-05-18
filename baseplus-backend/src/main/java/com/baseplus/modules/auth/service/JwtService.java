package com.baseplus.modules.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long EXPIRATION_SECONDS = 3600L;

    private final ObjectMapper objectMapper;
    private final String secret;

    public JwtService(ObjectMapper objectMapper, @Value("${jwt.secret}") String secret) {
        this.objectMapper = objectMapper;
        this.secret = secret;
    }

    public String generateToken(String subject) {
        return generateToken(subject, Collections.emptyList(), Collections.emptyList());
    }

    public String generateToken(String subject, List<String> roles) {
        return generateToken(subject, roles, Collections.emptyList());
    }

    public String generateToken(String subject, List<String> roles, List<String> permissions) {
        Instant now = Instant.now();

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", subject);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plusSeconds(EXPIRATION_SECONDS).getEpochSecond());
        payload.put("roles", roles == null ? Collections.emptyList() : roles);
        payload.put("permissions", permissions == null ? Collections.emptyList() : permissions);

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String signature = sign(encodedHeader + "." + encodedPayload);

        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    public boolean isTokenValid(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }

            String expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!constantTimeEquals(expectedSignature, parts[2])) {
                return false;
            }

            Map<?, ?> payload = objectMapper.readValue(decode(parts[1]), Map.class);
            Object expiration = payload.get("exp");
            if (!(expiration instanceof Number number)) {
                return false;
            }

            return Instant.now().getEpochSecond() < number.longValue();
        } catch (Exception exception) {
            return false;
        }
    }

    public String getSubject(String token) {
        try {
            String[] parts = token.split("\\.");
            Map<?, ?> payload = objectMapper.readValue(decode(parts[1]), Map.class);
            Object subject = payload.get("sub");
            return subject == null ? null : subject.toString();
        } catch (Exception exception) {
            return null;
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
            String[] parts = token.split("\\.");
            Map<?, ?> payload = objectMapper.readValue(decode(parts[1]), Map.class);
            Object value = payload.get(claim);
            if (!(value instanceof List<?> values)) {
                return Collections.emptyList();
            }

            return values.stream()
                    .map(Object::toString)
                    .toList();
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }

    public long getExpirationSeconds() {
        return EXPIRATION_SECONDS;
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return encode(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel gerar o token JWT.", exception);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(key);
            return encode(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel assinar o token JWT.", exception);
        }
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    private boolean constantTimeEquals(String first, String second) {
        return MessageDigestHolder.equals(first.getBytes(StandardCharsets.UTF_8), second.getBytes(StandardCharsets.UTF_8));
    }

    private static class MessageDigestHolder {

        static boolean equals(byte[] first, byte[] second) {
            return java.security.MessageDigest.isEqual(first, second);
        }
    }
}
