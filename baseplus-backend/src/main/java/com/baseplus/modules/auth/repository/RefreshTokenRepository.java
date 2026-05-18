package com.baseplus.modules.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.baseplus.modules.auth.domain.RefreshToken;
import com.baseplus.modules.conta.domain.UserSession;
import com.baseplus.modules.usuario.domain.Usuario;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUsuario(Usuario usuario);

    void deleteBySession(UserSession session);
}
