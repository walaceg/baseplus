package com.baseplus.modules.usuario.dto;

public record CreateUsuarioRequest(
        String nome,
        String nomeExibicao,
        String email,
        String senha,
        String cargo,
        String departamento,
        String telefone,
        String celular,
        String matricula,
        String observacoesInternas,
        Boolean ativo,
        Boolean bloqueado,
        Boolean trocarSenhaPrimeiroAcesso
) {
}
