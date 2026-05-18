package com.baseplus.modules.auth.dto;

public record ChangeInitialPasswordRequest(
        String senhaAtual,
        String novaSenha,
        String confirmarNovaSenha
) {
}
