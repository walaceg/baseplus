package com.baseplus.modules.conta.dto;

public record UpdateUserPreferencesRequest(
        String tema,
        String idioma,
        Boolean notificacoes,
        String corPrimaria,
        String corSecundaria,
        String preferenciaVisual,
        String menuPrincipal
) {
}
