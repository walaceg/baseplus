package com.baseplus.modules.conta.domain;

import com.baseplus.modules.usuario.domain.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_preferences")
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, length = 40)
    private String tema;

    @Column(nullable = false, length = 20)
    private String idioma;

    @Column(nullable = false)
    private boolean notificacoes;

    @Column(nullable = false, length = 20)
    private String corPrimaria;

    @Column(nullable = false, length = 20)
    private String corSecundaria;

    @Column(nullable = false, length = 20)
    private String preferenciaVisual;

    @Column(name = "menu_principal", nullable = false, length = 20)
    private String menuPrincipal;

    protected UserPreferences() {
    }

    public UserPreferences(
            Usuario usuario,
            String tema,
            String idioma,
            boolean notificacoes,
            String corPrimaria,
            String corSecundaria,
            String preferenciaVisual,
            String menuPrincipal
    ) {
        this.usuario = usuario;
        this.tema = tema;
        this.idioma = idioma;
        this.notificacoes = notificacoes;
        this.corPrimaria = corPrimaria;
        this.corSecundaria = corSecundaria;
        this.preferenciaVisual = preferenciaVisual;
        this.menuPrincipal = menuPrincipal;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public boolean isNotificacoes() {
        return notificacoes;
    }

    public void setNotificacoes(boolean notificacoes) {
        this.notificacoes = notificacoes;
    }

    public String getCorPrimaria() {
        return corPrimaria;
    }

    public void setCorPrimaria(String corPrimaria) {
        this.corPrimaria = corPrimaria;
    }

    public String getCorSecundaria() {
        return corSecundaria;
    }

    public void setCorSecundaria(String corSecundaria) {
        this.corSecundaria = corSecundaria;
    }

    public String getPreferenciaVisual() {
        return preferenciaVisual;
    }

    public void setPreferenciaVisual(String preferenciaVisual) {
        this.preferenciaVisual = preferenciaVisual;
    }

    public String getMenuPrincipal() {
        return menuPrincipal;
    }

    public void setMenuPrincipal(String menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
    }
}
