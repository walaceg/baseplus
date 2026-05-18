package com.baseplus.modules.auth.dto;

public record RoleUserResponse(
        Long id,
        String nome,
        String nomeExibicao,
        String email,
        boolean ativo,
        boolean bloqueado,
        String avatarUrl
) {
}
