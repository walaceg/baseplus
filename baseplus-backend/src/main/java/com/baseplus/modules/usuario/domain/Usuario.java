package com.baseplus.modules.usuario.domain;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import com.baseplus.modules.auth.domain.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 120)
    private String nomeExibicao;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(length = 120)
    private String cargo;

    @Column(length = 120)
    private String departamento;

    @Column(length = 40)
    private String telefone;

    @Column(length = 40)
    private String celular;

    @Column(length = 60)
    private String matricula;

    @Column(length = 1000)
    private String observacoesInternas;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private boolean bloqueado = false;

    @Column(nullable = false)
    private boolean trocarSenhaPrimeiroAcesso = false;

    @Column
    private LocalDateTime ultimoLoginEm;

    @Column(nullable = false)
    private Integer tentativasLoginInvalidas = 0;

    @Column(length = 300)
    private String avatarUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    protected Usuario() {
    }

    public Usuario(String nome, String email, String senha, boolean ativo) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.ativo = ativo;
    }

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = OffsetDateTime.now();
        }
        if (tentativasLoginInvalidas == null) {
            tentativasLoginInvalidas = 0;
        }
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public void setNomeExibicao(String nomeExibicao) {
        this.nomeExibicao = nomeExibicao;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getObservacoesInternas() {
        return observacoesInternas;
    }

    public void setObservacoesInternas(String observacoesInternas) {
        this.observacoesInternas = observacoesInternas;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public boolean isTrocarSenhaPrimeiroAcesso() {
        return trocarSenhaPrimeiroAcesso;
    }

    public void setTrocarSenhaPrimeiroAcesso(boolean trocarSenhaPrimeiroAcesso) {
        this.trocarSenhaPrimeiroAcesso = trocarSenhaPrimeiroAcesso;
    }

    public LocalDateTime getUltimoLoginEm() {
        return ultimoLoginEm;
    }

    public void setUltimoLoginEm(LocalDateTime ultimoLoginEm) {
        this.ultimoLoginEm = ultimoLoginEm;
    }

    public Integer getTentativasLoginInvalidas() {
        return tentativasLoginInvalidas == null ? 0 : tentativasLoginInvalidas;
    }

    public void setTentativasLoginInvalidas(Integer tentativasLoginInvalidas) {
        this.tentativasLoginInvalidas = tentativasLoginInvalidas == null ? 0 : Math.max(0, tentativasLoginInvalidas);
    }

    public void registrarLoginInvalido() {
        setTentativasLoginInvalidas(getTentativasLoginInvalidas() + 1);
    }

    public void registrarLoginBemSucedido() {
        setTentativasLoginInvalidas(0);
        setUltimoLoginEm(LocalDateTime.now());
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }
}
