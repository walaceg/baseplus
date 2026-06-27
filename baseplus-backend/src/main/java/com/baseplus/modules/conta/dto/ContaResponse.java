package com.baseplus.modules.conta.dto;

public record ContaResponse(
        Long id,
        String nome,
        String email,
        String avatarUrl
) {
}
