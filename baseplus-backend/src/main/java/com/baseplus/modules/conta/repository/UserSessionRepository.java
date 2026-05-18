package com.baseplus.modules.conta.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.baseplus.modules.conta.domain.UserSession;
import com.baseplus.modules.usuario.domain.Usuario;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    List<UserSession> findByUsuarioOrderByCriadaEmDesc(Usuario usuario);

    Optional<UserSession> findByIdAndUsuario(Long id, Usuario usuario);

    void deleteByUsuario(Usuario usuario);
}
