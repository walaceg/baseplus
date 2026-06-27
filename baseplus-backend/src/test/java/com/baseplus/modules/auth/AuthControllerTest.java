package com.baseplus.modules.auth;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.baseplus.modules.usuario.domain.Usuario;
import com.baseplus.modules.usuario.service.UsuarioService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldLoginWithPersistedAdminUser() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@baseplus.com",
                                  "password": "Baseplus@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value(notNullValue()))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600))
                .andExpect(jsonPath("$.data.refreshToken").value(notNullValue()))
                .andExpect(jsonPath("$.data.refreshExpiresIn").value(604800))
                .andExpect(jsonPath("$.data.mustChangePassword").value(false))
                .andExpect(jsonPath("$.message").value("Login realizado com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@baseplus.com",
                                  "password": "senha-incorreta"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Credenciais invalidas."));
    }

    @Test
    void shouldTrackInvalidLoginAttemptsAndResetOnSuccessfulLogin() throws Exception {
        Usuario usuario = new Usuario(
                "Usuario Tentativas",
                "tentativas.login@baseplus.com",
                passwordEncoder.encode("Baseplus@456"),
                true
        );
        usuarioService.salvar(usuario);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "tentativas.login@baseplus.com",
                                  "password": "senha-incorreta"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais invalidas."));

        org.hamcrest.MatcherAssert.assertThat(
                usuarioService.buscarPorEmail("tentativas.login@baseplus.com").orElseThrow().getTentativasLoginInvalidas(),
                org.hamcrest.Matchers.is(1)
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "tentativas.login@baseplus.com",
                                  "password": "Baseplus@456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Usuario atualizado = usuarioService.buscarPorEmail("tentativas.login@baseplus.com").orElseThrow();
        org.hamcrest.MatcherAssert.assertThat(atualizado.getTentativasLoginInvalidas(), org.hamcrest.Matchers.is(0));
        org.hamcrest.MatcherAssert.assertThat(atualizado.getUltimoLoginEm(), org.hamcrest.Matchers.notNullValue());
    }

    @Test
    void shouldBlockUserAfterRepeatedInvalidLoginAttempts() throws Exception {
        Usuario usuario = new Usuario(
                "Usuario Bloqueio Automatico",
                "bloqueio.automatico@baseplus.com",
                passwordEncoder.encode("Baseplus@456"),
                true
        );
        usuarioService.salvar(usuario);

        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "bloqueio.automatico@baseplus.com",
                                      "password": "senha-incorreta"
                                    }
                                    """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Credenciais invalidas."));
        }

        Usuario bloqueado = usuarioService.buscarPorEmail("bloqueio.automatico@baseplus.com").orElseThrow();
        org.hamcrest.MatcherAssert.assertThat(bloqueado.getTentativasLoginInvalidas(), org.hamcrest.Matchers.is(5));
        org.hamcrest.MatcherAssert.assertThat(bloqueado.isBloqueado(), org.hamcrest.Matchers.is(true));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "bloqueio.automatico@baseplus.com",
                                  "password": "Baseplus@456"
                                }
                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usu\u00e1rio bloqueado"));
    }

    @Test
    void shouldBlockInactiveAndBlockedUsersOnLogin() throws Exception {
        Usuario inativo = new Usuario(
                "Usuario Inativo",
                "inativo.login@baseplus.com",
                passwordEncoder.encode("Baseplus@456"),
                false
        );
        usuarioService.salvar(inativo);

        Usuario bloqueado = new Usuario(
                "Usuario Bloqueado",
                "bloqueado.login@baseplus.com",
                passwordEncoder.encode("Baseplus@456"),
                true
        );
        bloqueado.setBloqueado(true);
        usuarioService.salvar(bloqueado);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "inativo.login@baseplus.com",
                                  "password": "Baseplus@456"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuário inativo"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "bloqueado.login@baseplus.com",
                                  "password": "Baseplus@456"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuário bloqueado"));
    }

    @Test
    void shouldRequireInitialPasswordChangeBeforeFullAccess() throws Exception {
        Usuario usuario = new Usuario(
                "Usuario Primeiro Acesso",
                "primeiro.acesso@baseplus.com",
                passwordEncoder.encode("Baseplus@456"),
                true
        );
        usuario.setTrocarSenhaPrimeiroAcesso(true);
        usuarioService.salvar(usuario);

        String loginContent = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "primeiro.acesso@baseplus.com",
                                  "password": "Baseplus@456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mustChangePassword").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginContent).path("data").path("token").asText();

        mockMvc.perform(get("/conta")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Troca de senha obrigatoria."));

        mockMvc.perform(post("/auth/change-initial-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senhaAtual": "Baseplus@456",
                                  "novaSenha": "Baseplus@789",
                                  "confirmarNovaSenha": "Baseplus@789"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Senha alterada com sucesso."));

        Usuario atualizado = usuarioService.buscarPorEmail("primeiro.acesso@baseplus.com").orElseThrow();
        org.hamcrest.MatcherAssert.assertThat(atualizado.isTrocarSenhaPrimeiroAcesso(), org.hamcrest.Matchers.is(false));
        org.hamcrest.MatcherAssert.assertThat(atualizado.getTentativasLoginInvalidas(), org.hamcrest.Matchers.is(0));

        mockMvc.perform(get("/conta")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("primeiro.acesso@baseplus.com"));
    }

    @Test
    void shouldValidateInitialPasswordChangeRequest() throws Exception {
        Usuario usuario = new Usuario(
                "Usuario Validacao Senha",
                "validacao.senha@baseplus.com",
                passwordEncoder.encode("Baseplus@456"),
                true
        );
        usuario.setTrocarSenhaPrimeiroAcesso(true);
        usuarioService.salvar(usuario);

        String token = login("validacao.senha@baseplus.com", "Baseplus@456").path("data").path("token").asText();

        mockMvc.perform(post("/auth/change-initial-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senhaAtual": "Baseplus@456",
                                  "novaSenha": "curta",
                                  "confirmarNovaSenha": "curta"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A nova senha deve ter no minimo 8 caracteres."))
                .andExpect(jsonPath("$.errors[0]").value("A nova senha deve ter no minimo 8 caracteres."));

        mockMvc.perform(post("/auth/change-initial-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senhaAtual": "Baseplus@456",
                                  "novaSenha": "Baseplus@789",
                                  "confirmarNovaSenha": "Baseplus@000"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nova senha e confirmacao devem ser iguais."))
                .andExpect(jsonPath("$.errors[0]").value("Nova senha e confirmacao devem ser iguais."));

        mockMvc.perform(post("/auth/change-initial-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senhaAtual": "Baseplus@456",
                                  "novaSenha": "Baseplus@456",
                                  "confirmarNovaSenha": "Baseplus@456"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A nova senha deve ser diferente da senha atual."))
                .andExpect(jsonPath("$.errors[0]").value("A nova senha deve ser diferente da senha atual."));

        mockMvc.perform(post("/auth/change-initial-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senhaAtual": "senha-incorreta",
                                  "novaSenha": "Baseplus@789",
                                  "confirmarNovaSenha": "Baseplus@789"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Senha atual invalida."))
                .andExpect(jsonPath("$.errors[0]").value("A senha atual informada nao confere."));
    }


    @Test
    void shouldBlockProtectedRoutesWithoutToken() throws Exception {
        mockMvc.perform(get("/rota-protegida"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldAllowCorsPreflightFromFrontendOrigin() throws Exception {
        mockMvc.perform(options("/auth/login")
                        .header("Origin", "http://127.0.0.1:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", "http://127.0.0.1:5173"));
    }

    @Test
    void shouldBlockMeWithoutToken() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldBlockLogoutWithoutToken() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldReturnAuthenticatedUser() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(notNullValue()))
                .andExpect(jsonPath("$.data.nome").value("Administrador Base+"))
                .andExpect(jsonPath("$.data.email").value("admin@baseplus.com"))
                .andExpect(jsonPath("$.data.senha").doesNotExist())
                .andExpect(jsonPath("$.message").value("Usuario autenticado."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldReturnAuthenticatedUserAvatarUrl() throws Exception {
        Usuario usuario = new Usuario(
                "Usuario Avatar",
                "usuario.avatar@baseplus.com",
                passwordEncoder.encode("Baseplus@456"),
                true
        );
        usuario.setAvatarUrl("/uploads/avatars/avatar-auth.png");
        usuarioService.salvar(usuario);
        String token = login("usuario.avatar@baseplus.com", "Baseplus@456").path("data").path("token").asText();

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("usuario.avatar@baseplus.com"))
                .andExpect(jsonPath("$.data.avatarUrl").value(startsWith("/uploads/avatars/")))
                .andExpect(jsonPath("$.data.avatarUrl").value("/uploads/avatars/avatar-auth.png"));
    }

    @Test
    void shouldGenerateJwtWithExpectedShape() throws Exception {
        String token = loginAndGetToken();

        org.hamcrest.MatcherAssert.assertThat(token, startsWith("eyJ"));
    }

    @Test
    void shouldIncludeAdminRoleInJwt() throws Exception {
        String token = loginAndGetToken();
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        JsonNode json = objectMapper.readTree(payload);
        List<String> roles = objectMapper.convertValue(json.path("roles"), new TypeReference<>() {
        });

        org.hamcrest.MatcherAssert.assertThat(roles, hasItem("ADMIN"));
    }

    @Test
    void shouldIncludeAdminPermissionInJwt() throws Exception {
        String token = loginAndGetToken();
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        JsonNode json = objectMapper.readTree(payload);
        List<String> permissions = objectMapper.convertValue(json.path("permissions"), new TypeReference<>() {
        });

        org.hamcrest.MatcherAssert.assertThat(permissions, hasItem("ADMIN_ACCESS"));
    }

    @Test
    void shouldRefreshAccessToken() throws Exception {
        String refreshToken = loginAndGetRefreshToken();

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value(notNullValue()))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.message").value("Token renovado com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldRejectInvalidRefreshToken() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "token-invalido"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Refresh token invalido."));
    }

    @Test
    void shouldLogoutAndInvalidateRefreshToken() throws Exception {
        JsonNode login = login();
        String token = login.path("data").path("token").asText();
        String refreshToken = login.path("data").path("refreshToken").asText();

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Logout realizado com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));

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

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("admin@baseplus.com"));
    }

    private String loginAndGetToken() throws Exception {
        JsonNode json = login();
        return json.path("data").path("token").asText();
    }

    private String loginAndGetRefreshToken() throws Exception {
        JsonNode json = login();
        return json.path("data").path("refreshToken").asText();
    }

    private JsonNode login() throws Exception {
        return login("admin@baseplus.com", "Baseplus@123");
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
