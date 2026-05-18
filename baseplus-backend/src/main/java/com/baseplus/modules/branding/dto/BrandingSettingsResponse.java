package com.baseplus.modules.branding.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.baseplus.modules.branding.domain.LoginBackgroundMode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BrandingSettingsResponse(
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
        Boolean whiteLabelEnabled,
        String whiteLabelName,
        String whiteLabelSubtitle
) {
}
