package com.baseplus.modules.usuario.dto;

import java.util.List;

public record UpdateUsuarioRequest(
        String nome,
        String nomeExibicao,
        String email,
        String cargo,
        String departamento,
        String telefone,
        String celular,
        String matricula,
        String observacoesInternas,
        Boolean ativo,
        Boolean bloqueado,
        Boolean trocarSenhaPrimeiroAcesso,
        List<Long> roleIds
) {
}
