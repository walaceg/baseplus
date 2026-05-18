package com.baseplus.modules.branding.dto;

import com.baseplus.modules.branding.domain.LoginBackgroundMode;

public class UpdateBrandingSettingsRequest {

    private String nomePlataforma;
    private String subtituloInstitucional;
    private String tema;
    private String corPrimaria;
    private String corSecundaria;
    private String densidadeVisual;
    private LoginBackgroundMode loginBackgroundMode;
    private String logoUrl;
    private String compactLogoUrl;
    private String faviconUrl;
    private String loginLogoUrl;
    private String loginBackgroundUrl;
    private Boolean whiteLabelEnabled;
    private String whiteLabelName;
    private String whiteLabelSubtitle;

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

    public Boolean getWhiteLabelEnabled() {
        return whiteLabelEnabled;
    }

    public void setWhiteLabelEnabled(Boolean whiteLabelEnabled) {
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
