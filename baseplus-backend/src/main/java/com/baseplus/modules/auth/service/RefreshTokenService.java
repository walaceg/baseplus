package com.baseplus.modules.auth.service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baseplus.core.exception.BusinessException;
import com.baseplus.modules.auth.domain.RefreshToken;
import com.baseplus.modules.auth.repository.RefreshTokenRepository;
import com.baseplus.modules.conta.domain.UserSession;
import com.baseplus.modules.usuario.domain.Usuario;

@Service
public class RefreshTokenService {

    private static final long EXPIRATION_SECONDS = 604800L;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken criar(Usuario usuario, UserSession session) {
        byte[] tokenBytes = new byte[48];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        RefreshToken refreshToken = new RefreshToken(
                usuario,
                session,
                token,
                OffsetDateTime.now().plusSeconds(EXPIRATION_SECONDS)
        );

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken validar(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw invalidRefreshToken("Refresh token e obrigatorio.");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token.trim())
                .orElseThrow(() -> invalidRefreshToken("Refresh token invalido."));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw invalidRefreshToken("Refresh token expirado.");
        }

        return refreshToken;
    }

    @Transactional
    public void removerPorUsuario(Usuario usuario) {
        refreshTokenRepository.deleteByUsuario(usuario);
    }

    @Transactional
    public void removerPorSessao(UserSession session) {
        refreshTokenRepository.deleteBySession(session);
    }

    public long getExpirationSeconds() {
        return EXPIRATION_SECONDS;
    }

    private BusinessException invalidRefreshToken(String error) {
        return new BusinessException("Refresh token invalido.", HttpStatus.UNAUTHORIZED, java.util.List.of(error));
    }
}
