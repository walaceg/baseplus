package com.baseplus.modules.conta.domain;

import java.time.OffsetDateTime;

import com.baseplus.modules.usuario.domain.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime criadaEm;

    protected UserSession() {
    }

    public UserSession(Usuario usuario) {
        this.usuario = usuario;
    }

    @PrePersist
    void prePersist() {
        if (criadaEm == null) {
            criadaEm = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public OffsetDateTime getCriadaEm() {
        return criadaEm;
    }
}
