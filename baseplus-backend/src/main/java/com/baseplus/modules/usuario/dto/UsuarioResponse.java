package com.baseplus.modules.usuario.dto;

import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.util.List;

public record UsuarioResponse(
        Long id,
        String nome,
        String nomeExibicao,
        String email,
        String cargo,
        String departamento,
        String telefone,
        String celular,
        String matricula,
        String observacoesInternas,
        boolean ativo,
        boolean bloqueado,
        boolean trocarSenhaPrimeiroAcesso,
        LocalDateTime ultimoLoginEm,
        int tentativasLoginInvalidas,
        OffsetDateTime criadoEm,
        List<String> roles,
        List<RoleSummaryResponse> roleDetails
) {
    public record RoleSummaryResponse(
            Long id,
            String name,
            String description,
            boolean ativo,
            boolean sistema
    ) {
    }
}
