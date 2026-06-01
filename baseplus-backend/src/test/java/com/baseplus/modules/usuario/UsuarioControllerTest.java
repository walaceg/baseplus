package com.baseplus.modules.usuario;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.baseplus.modules.usuario.domain.Usuario;
import com.baseplus.modules.auth.repository.PermissionRepository;
import com.baseplus.modules.usuario.service.UsuarioService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissionRepository permissionRepository;

    @Test
    void shouldBlockUsuariosWithoutToken() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldBlockUsuariosForNonAdmin() throws Exception {
        usuarioService.salvar(new Usuario(
                "Usuario Comum",
                "comum@baseplus.com",
                passwordEncoder.encode("Baseplus@123"),
                true
        ));
        String token = loginAndGetToken("comum@baseplus.com", "Baseplus@123");

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso negado."));
    }

    @Test
    void shouldListUsuariosForAdmin() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + token)
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].nome").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].email").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].ativo").value(true))
                .andExpect(jsonPath("$.data.content[0].bloqueado").value(false))
                .andExpect(jsonPath("$.data.content[0].trocarSenhaPrimeiroAcesso").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].tentativasLoginInvalidas").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].senha").doesNotExist())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(notNullValue()))
                .andExpect(jsonPath("$.message").value("Usuarios carregados."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldSearchUsuariosByNameOrEmail() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        criarUsuario(token, "busca.usuario@baseplus.com");

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + token)
                        .param("search", "busca.usuario")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].email").value("busca.usuario@baseplus.com"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void shouldSearchUsuariosByCargoOrDepartamento() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");

        mockMvc.perform(post("/usuarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario Corporativo",
                                  "email": "corporativo.busca@baseplus.com",
                                  "senha": "Baseplus@456",
                                  "cargo": "Coordenador Financeiro",
                                  "departamento": "Controladoria"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + token)
                        .param("search", "controladoria")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].email").value("corporativo.busca@baseplus.com"))
                .andExpect(jsonPath("$.data.content[0].cargo").value("Coordenador Financeiro"))
                .andExpect(jsonPath("$.data.content[0].departamento").value("Controladoria"));
    }

    @Test
    void shouldCreateUsuarioWithEncodedPassword() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");

        mockMvc.perform(post("/usuarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Novo Usuario",
                                  "nomeExibicao": "Novo",
                                  "email": "novo@baseplus.com",
                                  "senha": "Baseplus@456",
                                  "cargo": "Analista",
                                  "departamento": "Operacoes",
                                  "telefone": "1133334444",
                                  "celular": "11999998888",
                                  "matricula": "BP-001",
                                  "observacoesInternas": "Usuario criado por teste",
                                  "ativo": true,
                                  "bloqueado": false,
                                  "trocarSenhaPrimeiroAcesso": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(notNullValue()))
                .andExpect(jsonPath("$.data.nome").value("Novo Usuario"))
                .andExpect(jsonPath("$.data.nomeExibicao").value("Novo"))
                .andExpect(jsonPath("$.data.email").value("novo@baseplus.com"))
                .andExpect(jsonPath("$.data.cargo").value("Analista"))
                .andExpect(jsonPath("$.data.departamento").value("Operacoes"))
                .andExpect(jsonPath("$.data.telefone").value("1133334444"))
                .andExpect(jsonPath("$.data.celular").value("11999998888"))
                .andExpect(jsonPath("$.data.matricula").value("BP-001"))
                .andExpect(jsonPath("$.data.observacoesInternas").value("Usuario criado por teste"))
                .andExpect(jsonPath("$.data.ativo").value(true))
                .andExpect(jsonPath("$.data.bloqueado").value(false))
                .andExpect(jsonPath("$.data.trocarSenhaPrimeiroAcesso").value(true))
                .andExpect(jsonPath("$.data.ultimoLoginEm").doesNotExist())
                .andExpect(jsonPath("$.data.tentativasLoginInvalidas").value(0))
                .andExpect(jsonPath("$.data.senha").doesNotExist())
                .andExpect(jsonPath("$.message").value("Usuario criado com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));

        Usuario usuario = usuarioService.buscarPorEmail("novo@baseplus.com").orElseThrow();
        org.hamcrest.MatcherAssert.assertThat(passwordEncoder.matches("Baseplus@456", usuario.getSenha()), org.hamcrest.Matchers.is(true));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "novo@baseplus.com",
                                  "password": "Baseplus@456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldRejectDuplicatedEmailOnCreate() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");

        mockMvc.perform(post("/usuarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Admin Duplicado",
                                  "email": "admin@baseplus.com",
                                  "senha": "Baseplus@456"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email ja cadastrado."));
    }

    @Test
    void shouldGetAndUpdateUsuario() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long usuarioId = criarUsuario(token, "editar@baseplus.com");

        mockMvc.perform(get("/usuarios/{id}", usuarioId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(usuarioId))
                .andExpect(jsonPath("$.message").value("Usuario carregado."));

        mockMvc.perform(put("/usuarios/{id}", usuarioId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario Editado",
                                  "nomeExibicao": "Editado",
                                  "email": "editado@baseplus.com",
                                  "cargo": "Gerente",
                                  "departamento": "Tecnologia",
                                  "telefone": "1144443333",
                                  "celular": "11888889999",
                                  "matricula": "BP-002",
                                  "observacoesInternas": "Observacao atualizada",
                                  "ativo": false,
                                  "bloqueado": true,
                                  "trocarSenhaPrimeiroAcesso": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(usuarioId))
                .andExpect(jsonPath("$.data.nome").value("Usuario Editado"))
                .andExpect(jsonPath("$.data.nomeExibicao").value("Editado"))
                .andExpect(jsonPath("$.data.email").value("editado@baseplus.com"))
                .andExpect(jsonPath("$.data.cargo").value("Gerente"))
                .andExpect(jsonPath("$.data.departamento").value("Tecnologia"))
                .andExpect(jsonPath("$.data.telefone").value("1144443333"))
                .andExpect(jsonPath("$.data.celular").value("11888889999"))
                .andExpect(jsonPath("$.data.matricula").value("BP-002"))
                .andExpect(jsonPath("$.data.observacoesInternas").value("Observacao atualizada"))
                .andExpect(jsonPath("$.data.ativo").value(false))
                .andExpect(jsonPath("$.data.bloqueado").value(true))
                .andExpect(jsonPath("$.data.trocarSenhaPrimeiroAcesso").value(false))
                .andExpect(jsonPath("$.message").value("Usuario atualizado com sucesso."));
    }

    @Test
    void shouldUpdateUsuarioRolesFromRoleIds() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long usuarioId = criarUsuario(token, "perfis.usuario@baseplus.com");
        Long financeiroId = criarRole(token, "financeiro_usuario");
        Long suporteId = criarRole(token, "suporte_usuario");

        mockMvc.perform(put("/usuarios/{id}", usuarioId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario com Perfis",
                                  "email": "perfis.usuario@baseplus.com",
                                  "ativo": true,
                                  "trocarSenhaPrimeiroAcesso": true,
                                  "roleIds": [%d, %d]
                                }
                                """.formatted(financeiroId, suporteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[0]").value("FINANCEIRO_USUARIO"))
                .andExpect(jsonPath("$.data.roles[1]").value("SUPORTE_USUARIO"))
                .andExpect(jsonPath("$.data.roleDetails[0].id").value(financeiroId))
                .andExpect(jsonPath("$.data.roleDetails[0].description").value("Role de usuario"))
                .andExpect(jsonPath("$.data.roleDetails[0].sistema").value(false));

        mockMvc.perform(put("/usuarios/{id}", usuarioId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario com Perfis",
                                  "email": "perfis.usuario@baseplus.com",
                                  "ativo": true,
                                  "trocarSenhaPrimeiroAcesso": true,
                                  "roleIds": [%d]
                                }
                                """.formatted(suporteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[0]").value("SUPORTE_USUARIO"))
                .andExpect(jsonPath("$.data.roles[1]").doesNotExist())
                .andExpect(jsonPath("$.data.roleDetails[0].id").value(suporteId));

        mockMvc.perform(put("/usuarios/{id}", usuarioId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario com Perfis",
                                  "email": "perfis.usuario@baseplus.com",
                                  "ativo": true,
                                  "trocarSenhaPrimeiroAcesso": true,
                                  "roleIds": [%d, %d]
                                }
                                """.formatted(suporteId, suporteId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Perfis duplicados."));
    }

    @Test
    void shouldRejectExistingAccessTokenAfterUsuarioIsBlocked() throws Exception {
        String adminToken = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long usuarioId = criarUsuario(adminToken, "bloqueio.token@baseplus.com");
        Long roleId = criarRoleComPermissao(adminToken, "usuarios_bloqueio", "USERS_VIEW");
        vincularRole(adminToken, usuarioId, "bloqueio.token@baseplus.com", roleId);
        String userToken = loginAndGetToken("bloqueio.token@baseplus.com", "Baseplus@456");

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(put("/usuarios/{id}", usuarioId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario Bloqueado",
                                  "email": "bloqueio.token@baseplus.com",
                                  "ativo": true,
                                  "bloqueado": true,
                                  "trocarSenhaPrimeiroAcesso": false,
                                  "roleIds": [%d]
                                }
                                """.formatted(roleId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldApplyPermissionRemovalToExistingAccessToken() throws Exception {
        String adminToken = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long usuarioId = criarUsuario(adminToken, "permissao.token@baseplus.com");
        Long permissionId = permissionRepository.findByName("USERS_VIEW").orElseThrow().getId();
        Long roleId = criarRoleComPermissao(adminToken, "usuarios_visualizacao", "USERS_VIEW");
        vincularRole(adminToken, usuarioId, "permissao.token@baseplus.com", roleId);
        String userToken = loginAndGetToken("permissao.token@baseplus.com", "Baseplus@456");

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/roles/{id}/permissions/{permissionId}", roleId, permissionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Acesso negado."));
    }

    @Test
    void shouldResetUsuarioPasswordAdministratively() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long usuarioId = criarUsuario(token, "resetar.senha@baseplus.com");
        Usuario usuario = usuarioService.buscarPorEmail("resetar.senha@baseplus.com").orElseThrow();
        usuario.setBloqueado(true);
        usuario.setTentativasLoginInvalidas(3);
        usuarioService.salvar(usuario);

        mockMvc.perform(post("/usuarios/{id}/resetar-senha", usuarioId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "novaSenhaTemporaria": "Baseplus@789",
                                  "obrigarTrocaProximoLogin": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(usuarioId))
                .andExpect(jsonPath("$.data.bloqueado").value(false))
                .andExpect(jsonPath("$.data.trocarSenhaPrimeiroAcesso").value(true))
                .andExpect(jsonPath("$.data.tentativasLoginInvalidas").value(0))
                .andExpect(jsonPath("$.data.senha").doesNotExist())
                .andExpect(jsonPath("$.message").value("Senha redefinida com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));

        Usuario atualizado = usuarioService.buscarPorEmail("resetar.senha@baseplus.com").orElseThrow();
        org.hamcrest.MatcherAssert.assertThat(passwordEncoder.matches("Baseplus@789", atualizado.getSenha()), org.hamcrest.Matchers.is(true));
        org.hamcrest.MatcherAssert.assertThat(atualizado.isBloqueado(), org.hamcrest.Matchers.is(false));
        org.hamcrest.MatcherAssert.assertThat(atualizado.getTentativasLoginInvalidas(), org.hamcrest.Matchers.is(0));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "resetar.senha@baseplus.com",
                                  "password": "Baseplus@789"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mustChangePassword").value(true));
    }

    @Test
    void shouldValidateAdministrativePasswordResetRequest() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long usuarioId = criarUsuario(token, "resetar.validacao@baseplus.com");

        mockMvc.perform(post("/usuarios/{id}/resetar-senha", usuarioId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "novaSenhaTemporaria": "curta",
                                  "obrigarTrocaProximoLogin": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("A nova senha temporaria deve ter no minimo 8 caracteres."))
                .andExpect(jsonPath("$.errors[0]").value("A nova senha temporaria deve ter no minimo 8 caracteres."));

        mockMvc.perform(post("/usuarios/{id}/resetar-senha", 999999L)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "novaSenhaTemporaria": "Baseplus@789",
                                  "obrigarTrocaProximoLogin": true
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuario nao encontrado."));
    }

    @Test
    void shouldDeleteUsuarioAndInvalidateLinkedRefreshToken() throws Exception {
        String adminToken = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long usuarioId = criarUsuario(adminToken, "remover@baseplus.com");
        JsonNode login = login("remover@baseplus.com", "Baseplus@456");
        String refreshToken = login.path("data").path("refreshToken").asText();

        mockMvc.perform(delete("/usuarios/{id}", usuarioId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Usuario removido com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));

        mockMvc.perform(get("/usuarios/{id}", usuarioId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuario nao encontrado."));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Refresh token invalido."));
    }

    @Test
    void shouldRejectDeletingAuthenticatedUsuario() throws Exception {
        JsonNode login = login("admin@baseplus.com", "Baseplus@123");
        String token = login.path("data").path("token").asText();
        Long adminId = usuarioService.buscarPorEmail("admin@baseplus.com").orElseThrow().getId();

        mockMvc.perform(delete("/usuarios/{id}", adminId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Operacao invalida."));
    }

    private Long criarUsuario(String token, String email) throws Exception {
        String content = mockMvc.perform(post("/usuarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario Teste",
                                  "email": "%s",
                                  "senha": "Baseplus@456"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(content).path("data").path("id").asLong();
    }

    private Long criarRole(String token, String name) throws Exception {
        String content = mockMvc.perform(post("/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "Role de usuario",
                                  "ativo": true
                                }
                                """.formatted(name)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(content).path("data").path("id").asLong();
    }

    private Long criarRoleComPermissao(String token, String name, String permissionName) throws Exception {
        Long permissionId = permissionRepository.findByName(permissionName).orElseThrow().getId();
        String content = mockMvc.perform(post("/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "Role de autorizacao",
                                  "ativo": true,
                                  "permissionIds": [%d]
                                }
                                """.formatted(name, permissionId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(content).path("data").path("id").asLong();
    }

    private void vincularRole(String token, Long usuarioId, String email, Long roleId) throws Exception {
        mockMvc.perform(put("/usuarios/{id}", usuarioId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario Autorizado",
                                  "email": "%s",
                                  "ativo": true,
                                  "bloqueado": false,
                                  "trocarSenhaPrimeiroAcesso": false,
                                  "roleIds": [%d]
                                }
                                """.formatted(email, roleId)))
                .andExpect(status().isOk());
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        JsonNode json = login(email, password);
        return json.path("data").path("token").asText();
    }

    private JsonNode login(String email, String password) throws Exception {
        String content = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(content);
    }
}
