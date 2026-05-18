package com.baseplus.modules.conta.dto;

public record ChangePasswordRequest(
        String senhaAtual,
        String novaSenha
) {
}
