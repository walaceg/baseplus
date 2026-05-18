package com.baseplus.modules.conta.dto;

public record UserPreferencesResponse(
        String tema,
        String idioma,
        boolean notificacoes,
        String corPrimaria,
        String corSecundaria,
        String preferenciaVisual,
        String menuPrincipal
) {
}
