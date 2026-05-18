package com.baseplus.modules.usuario.dto;

public record ResetSenhaUsuarioRequest(
        String novaSenhaTemporaria,
        Boolean obrigarTrocaProximoLogin
) {
}
