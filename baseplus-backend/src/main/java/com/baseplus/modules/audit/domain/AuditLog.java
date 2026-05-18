package com.baseplus.modules.audit.domain;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String usuario;

    @Column(nullable = false, length = 80)
    private String acao;

    @Column(nullable = false, length = 80)
    private String entidade;

    @Column(name = "entidade_id", nullable = true)
    private Long entidadeId;

    @Column(name = "timestamp_registro", nullable = false, updatable = false)
    private OffsetDateTime timestamp;

    protected AuditLog() {
    }

    public AuditLog(String usuario, String acao, String entidade, Long entidadeId) {
        this.usuario = usuario;
        this.acao = acao;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
    }

    @PrePersist
    void prePersist() {
        if (timestamp == null) {
            timestamp = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getAcao() {
        return acao;
    }

    public String getEntidade() {
        return entidade;
    }

    public Long getEntidadeId() {
        return entidadeId;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}
