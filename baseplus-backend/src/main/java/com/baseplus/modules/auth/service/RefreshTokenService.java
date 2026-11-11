package com.baseplus.modules.auth.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baseplus.core.exception.BusinessException;
import com.baseplus.modules.auth.domain.RefreshToken;
import com.baseplus.modules.auth.repository.RefreshTokenRepository;
import com.baseplus.modules.audit.service.AuditLogService;
import com.baseplus.modules.conta.domain.UserSession;
import com.baseplus.modules.conta.repository.UserSessionRepository;
import com.baseplus.modules.usuario.domain.Usuario;

@Service
public class RefreshTokenService {

    private static final long EXPIRATION_SECONDS = 604800L;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserSessionRepository userSessionRepository;
    private final AuditLogService auditLogService;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserSessionRepository userSessionRepository,
            AuditLogService auditLogService
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userSessionRepository = userSessionRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public String criar(Usuario usuario, UserSession session) {
        byte[] tokenBytes = new byte[48];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(EXPIRATION_SECONDS);

        refreshTokenRepository.save(new RefreshToken(
                usuario,
                session,
                RefreshTokenHasher.sha256Hex(token),
                expiresAt
        ));
        return token;
    }

    @Transactional
    public RotationAttempt rotate(String rawToken) {
        if (rawToken == null || rawToken.trim().isEmpty()) {
            throw invalidRefreshToken("Refresh token é obrigatório.");
        }

        String presentedHash = RefreshTokenHasher.sha256Hex(rawToken.trim());
        Long sessionId = refreshTokenRepository.findSessionIdByTokenHash(presentedHash)
                .orElseThrow(() -> invalidRefreshToken("Refresh token inválido."));

        UserSession session = userSessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> invalidRefreshToken("Refresh token inválido."));
        RefreshToken current = refreshTokenRepository.findByTokenHashForUpdate(presentedHash)
                .orElseThrow(() -> invalidRefreshToken("Refresh token inválido."));

        if (!current.getSession().getId().equals(session.getId())) {
            throw invalidRefreshToken("Refresh token inválido.");
        }

        if (current.isConsumed()) {
            auditLogService.registrar("SECURITY", "REFRESH_TOKEN_REUSE", "USER_SESSION", session.getId());
            return new RotationAttempt(null, true);
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (!current.getExpiresAt().isAfter(now)) {
            throw invalidRefreshToken("Refresh token expirado.");
        }

        Usuario usuario = current.getUsuario();
        if (!usuario.isAtivo() || usuario.isBloqueado()) {
            throw invalidRefreshToken("Refresh token inválido.");
        }

        current.consume(now);
        refreshTokenRepository.saveAndFlush(current);

        byte[] tokenBytes = new byte[48];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String nextRawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        refreshTokenRepository.saveAndFlush(new RefreshToken(
                usuario,
                session,
                RefreshTokenHasher.sha256Hex(nextRawToken),
                current.getExpiresAt()
        ));

        long expiresIn = Math.max(0, Duration.between(now, current.getExpiresAt()).getSeconds());
        return new RotationAttempt(new Rotation(usuario, session.getId(), nextRawToken, expiresIn), false);
    }

    @Transactional
    public void removerPorUsuario(Usuario usuario) {
        userSessionRepository.findAllByUsuarioForUpdate(usuario);
        refreshTokenRepository.deleteByUsuario(usuario);
    }

    @Transactional
    public void removerPorSessao(UserSession session) {
        if (session == null || session.getId() == null) {
            return;
        }
        userSessionRepository.findByIdForUpdate(session.getId())
                .ifPresent(refreshTokenRepository::deleteBySession);
    }

    @Transactional
    public boolean removerPorSessao(Long sessionId, Usuario usuario) {
        if (sessionId == null || usuario == null || usuario.getId() == null) {
            return false;
        }
        return userSessionRepository.findByIdAndUsuarioForUpdate(sessionId, usuario)
                .map(session -> {
                    refreshTokenRepository.deleteBySession(session);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public int purgeExpiredBefore(OffsetDateTime cutoff) {
        return refreshTokenRepository.deleteExpiredBefore(cutoff);
    }

    public long getExpirationSeconds() {
        return EXPIRATION_SECONDS;
    }

    private BusinessException invalidRefreshToken(String error) {
        return new BusinessException("Refresh token inválido.", HttpStatus.UNAUTHORIZED, java.util.List.of(error));
    }

    public record Rotation(Usuario usuario, Long sessionId, String refreshToken, long refreshExpiresIn) {
    }

    public record RotationAttempt(Rotation rotation, boolean reuseDetected) {
    }
}
