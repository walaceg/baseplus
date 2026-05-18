package com.baseplus.modules.conta.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.baseplus.modules.conta.domain.UserPreferences;
import com.baseplus.modules.usuario.domain.Usuario;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {

    Optional<UserPreferences> findByUsuario(Usuario usuario);

    void deleteByUsuario(Usuario usuario);
}
