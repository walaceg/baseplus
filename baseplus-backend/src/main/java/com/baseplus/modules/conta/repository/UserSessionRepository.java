package com.baseplus.modules.conta.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.baseplus.modules.conta.domain.UserSession;
import com.baseplus.modules.usuario.domain.Usuario;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    List<UserSession> findByUsuarioOrderByCriadaEmDesc(Usuario usuario);

    Optional<UserSession> findByIdAndUsuario(Long id, Usuario usuario);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from UserSession session where session.id = :id")
    Optional<UserSession> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from UserSession session where session.id = :id and session.usuario = :usuario")
    Optional<UserSession> findByIdAndUsuarioForUpdate(@Param("id") Long id, @Param("usuario") Usuario usuario);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from UserSession session where session.usuario = :usuario order by session.id")
    List<UserSession> findAllByUsuarioForUpdate(@Param("usuario") Usuario usuario);

    void deleteByUsuario(Usuario usuario);
}
