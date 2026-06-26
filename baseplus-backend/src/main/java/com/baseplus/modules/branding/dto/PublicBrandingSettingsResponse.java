package com.baseplus.modules.branding.dto;

import com.baseplus.modules.branding.domain.LoginBackgroundMode;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PublicBrandingSettingsResponse(
        String nomePlataforma,
        String subtituloInstitucional,
        String tema,
        String corPrimaria,
        String corSecundaria,
        String densidadeVisual,
        LoginBackgroundMode loginBackgroundMode,
        String logoUrl,
        String faviconUrl,
        String loginLogoUrl,
        String loginBackgroundUrl,
        Boolean whiteLabelEnabled,
        String whiteLabelName,
        String whiteLabelSubtitle
) {
}
