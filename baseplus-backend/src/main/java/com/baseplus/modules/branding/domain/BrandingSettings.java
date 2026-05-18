package com.baseplus.modules.branding.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;

@Entity
@Table(name = "branding_settings")
public class BrandingSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_plataforma", nullable = false, length = 120)
    private String nomePlataforma;

    @Column(name = "subtitulo_institucional", nullable = false, length = 255)
    private String subtituloInstitucional;

    @Column(name = "tema", nullable = false, length = 20)
    private String tema;

    @Column(name = "cor_primaria", nullable = false, length = 20)
    private String corPrimaria;

    @Column(name = "cor_secundaria", nullable = false, length = 20)
    private String corSecundaria;

    @Column(name = "densidade_visual", nullable = false, length = 20)
    private String densidadeVisual;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_background_mode", nullable = false, length = 32)
    private LoginBackgroundMode loginBackgroundMode;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "compact_logo_url", length = 255)
    private String compactLogoUrl;

    @Column(name = "favicon_url", length = 255)
    private String faviconUrl;

    @Column(name = "login_logo_url", length = 255)
    private String loginLogoUrl;

    @Column(name = "login_background_url", length = 255)
    private String loginBackgroundUrl;

    @Column(name = "white_label_enabled", nullable = false)
    private boolean whiteLabelEnabled;

    @Column(name = "white_label_name", length = 120)
    private String whiteLabelName;

    @Column(name = "white_label_subtitle", length = 255)
    private String whiteLabelSubtitle;

    protected BrandingSettings() {
    }

    public BrandingSettings(
            String nomePlataforma,
            String subtituloInstitucional,
            String tema,
            String corPrimaria,
            String corSecundaria,
            String densidadeVisual,
            LoginBackgroundMode loginBackgroundMode,
            String logoUrl,
            String compactLogoUrl,
            String faviconUrl,
            String loginBackgroundUrl
    ) {
        this(
                nomePlataforma,
                subtituloInstitucional,
                tema,
                corPrimaria,
                corSecundaria,
                densidadeVisual,
                loginBackgroundMode,
                logoUrl,
                compactLogoUrl,
                faviconUrl,
                null,
                loginBackgroundUrl,
                false,
                null,
                null
        );
    }

    public BrandingSettings(
            String nomePlataforma,
            String subtituloInstitucional,
            String tema,
            String corPrimaria,
            String corSecundaria,
            String densidadeVisual,
            LoginBackgroundMode loginBackgroundMode,
            String logoUrl,
            String compactLogoUrl,
            String faviconUrl,
            String loginLogoUrl,
            String loginBackgroundUrl,
            boolean whiteLabelEnabled,
            String whiteLabelName,
            String whiteLabelSubtitle
    ) {
        this.nomePlataforma = nomePlataforma;
        this.subtituloInstitucional = subtituloInstitucional;
        this.tema = tema;
        this.corPrimaria = corPrimaria;
        this.corSecundaria = corSecundaria;
        this.densidadeVisual = densidadeVisual;
        this.loginBackgroundMode = loginBackgroundMode;
        this.logoUrl = logoUrl;
        this.compactLogoUrl = compactLogoUrl;
        this.faviconUrl = faviconUrl;
        this.loginLogoUrl = loginLogoUrl;
        this.loginBackgroundUrl = loginBackgroundUrl;
        this.whiteLabelEnabled = whiteLabelEnabled;
        this.whiteLabelName = whiteLabelName;
        this.whiteLabelSubtitle = whiteLabelSubtitle;
    }

    public Long getId() {
        return id;
    }

    public String getNomePlataforma() {
        return nomePlataforma;
    }

    public void setNomePlataforma(String nomePlataforma) {
        this.nomePlataforma = nomePlataforma;
    }

    public String getSubtituloInstitucional() {
        return subtituloInstitucional;
    }

    public void setSubtituloInstitucional(String subtituloInstitucional) {
        this.subtituloInstitucional = subtituloInstitucional;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
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

    public String getDensidadeVisual() {
        return densidadeVisual;
    }

    public void setDensidadeVisual(String densidadeVisual) {
        this.densidadeVisual = densidadeVisual;
    }

    public LoginBackgroundMode getLoginBackgroundMode() {
        return loginBackgroundMode;
    }

    public void setLoginBackgroundMode(LoginBackgroundMode loginBackgroundMode) {
        this.loginBackgroundMode = loginBackgroundMode;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getCompactLogoUrl() {
        return compactLogoUrl;
    }

    public void setCompactLogoUrl(String compactLogoUrl) {
        this.compactLogoUrl = compactLogoUrl;
    }

    public String getFaviconUrl() {
        return faviconUrl;
    }

    public void setFaviconUrl(String faviconUrl) {
        this.faviconUrl = faviconUrl;
    }

    public String getLoginLogoUrl() {
        return loginLogoUrl;
    }

    public void setLoginLogoUrl(String loginLogoUrl) {
        this.loginLogoUrl = loginLogoUrl;
    }

    public String getLoginBackgroundUrl() {
        return loginBackgroundUrl;
    }

    public void setLoginBackgroundUrl(String loginBackgroundUrl) {
        this.loginBackgroundUrl = loginBackgroundUrl;
    }

    public boolean isWhiteLabelEnabled() {
        return whiteLabelEnabled;
    }

    public void setWhiteLabelEnabled(boolean whiteLabelEnabled) {
        this.whiteLabelEnabled = whiteLabelEnabled;
    }

    public String getWhiteLabelName() {
        return whiteLabelName;
    }

    public void setWhiteLabelName(String whiteLabelName) {
        this.whiteLabelName = whiteLabelName;
    }

    public String getWhiteLabelSubtitle() {
        return whiteLabelSubtitle;
    }

    public void setWhiteLabelSubtitle(String whiteLabelSubtitle) {
        this.whiteLabelSubtitle = whiteLabelSubtitle;
    }
}
