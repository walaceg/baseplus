package com.baseplus.modules.auth.domain;

import java.time.OffsetDateTime;

import com.baseplus.modules.conta.domain.UserSession;
import com.baseplus.modules.usuario.domain.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private UserSession session;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Column
    private OffsetDateTime consumedAt;

    protected RefreshToken() {
    }

    public RefreshToken(Usuario usuario, UserSession session, String tokenHash, OffsetDateTime expiresAt) {
        this.usuario = usuario;
        this.session = session;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public UserSession getSession() {
        return session;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getConsumedAt() {
        return consumedAt;
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }

    public void consume(OffsetDateTime consumedAt) {
        if (this.consumedAt != null) {
            throw new IllegalStateException("Refresh token já consumido.");
        }
        this.consumedAt = consumedAt;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(OffsetDateTime.now());
    }
}
