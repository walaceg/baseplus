package com.baseplus.modules.usuario.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.baseplus.modules.usuario.domain.Usuario;
import com.baseplus.modules.usuario.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }

        return usuarioRepository.findByEmailIgnoreCase(email.trim());
    }

    public Optional<Usuario> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return usuarioRepository.findById(id);
    }

    public Usuario salvar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public boolean existePorEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        return usuarioRepository.existsByEmailIgnoreCase(email.trim());
    }
}
