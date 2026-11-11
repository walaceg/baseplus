package com.baseplus.modules.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.baseplus.modules.auth.domain.RefreshToken;
import com.baseplus.modules.conta.domain.UserSession;
import com.baseplus.modules.usuario.domain.Usuario;

import jakarta.persistence.LockModeType;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Query("select token.session.id from RefreshToken token where token.tokenHash = :tokenHash")
    Optional<Long> findSessionIdByTokenHash(@Param("tokenHash") String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from RefreshToken token where token.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    long countBySessionAndConsumedAtIsNull(UserSession session);

    @Modifying
    @Query("delete from RefreshToken token where token.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") java.time.OffsetDateTime cutoff);

    void deleteByUsuario(Usuario usuario);

    void deleteBySession(UserSession session);
}
