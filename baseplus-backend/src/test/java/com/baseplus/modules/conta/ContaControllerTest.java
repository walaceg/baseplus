package com.baseplus.modules.conta;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.baseplus.modules.usuario.domain.Usuario;
import com.baseplus.modules.conta.repository.UserPreferencesRepository;
import com.baseplus.modules.usuario.service.UsuarioService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ContaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    @Test
    void shouldBlockContaWithoutToken() throws Exception {
        mockMvc.perform(get("/conta"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldReturnAuthenticatedConta() throws Exception {
        Usuario usuario = usuarioService.buscarPorEmail("admin@baseplus.com").orElseThrow();
        usuario.setAvatarUrl("/uploads/avatars/avatar-conta.png");
        usuarioService.salvar(usuario);
        String token = loginAndGetToken();

        mockMvc.perform(get("/conta")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(notNullValue()))
                .andExpect(jsonPath("$.data.nome").value("Administrador Base+"))
                .andExpect(jsonPath("$.data.email").value("admin@baseplus.com"))
                .andExpect(jsonPath("$.data.avatarUrl").value(startsWith("/uploads/avatars/")))
                .andExpect(jsonPath("$.data.avatarUrl").value("/uploads/avatars/avatar-conta.png"))
                .andExpect(jsonPath("$.data.senha").doesNotExist())
                .andExpect(jsonPath("$.message").value("Conta autenticada."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldUpdateAuthenticatedConta() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(put("/conta")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Admin Atualizado",
                                  "email": "admin.atualizado@baseplus.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(notNullValue()))
                .andExpect(jsonPath("$.data.nome").value("Admin Atualizado"))
                .andExpect(jsonPath("$.data.email").value("admin.atualizado@baseplus.com"))
                .andExpect(jsonPath("$.data.senha").doesNotExist())
                .andExpect(jsonPath("$.message").value("Conta atualizada com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));

        mockMvc.perform(get("/conta")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("admin.atualizado@baseplus.com"));
    }

    @Test
    void shouldRejectDuplicatedEmail() throws Exception {
        usuarioService.salvar(new Usuario(
                "Usuario Duplicado",
                "duplicado@baseplus.com",
                passwordEncoder.encode("Baseplus@123"),
                true
        ));
        String token = loginAndGetToken();

        mockMvc.perform(put("/conta")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Administrador Base+",
                                  "email": "duplicado@baseplus.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email ja cadastrado."));
    }

    @Test
    void shouldBlockPasswordChangeWithoutToken() throws Exception {
        mockMvc.perform(post("/conta/senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senhaAtual": "Baseplus@123",
                                  "novaSenha": "Baseplus@456"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldChangePassword() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/conta/senha")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senhaAtual": "Baseplus@123",
                                  "novaSenha": "Baseplus@456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Senha alterada com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@baseplus.com",
                                  "password": "Baseplus@456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldRejectInvalidCurrentPassword() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/conta/senha")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senhaAtual": "senha-incorreta",
                                  "novaSenha": "Baseplus@456"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Senha atual invalida."));
    }

    @Test
    void shouldRejectSameNewPassword() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/conta/senha")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senhaAtual": "Baseplus@123",
                                  "novaSenha": "Baseplus@123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Dados invalidos."));
    }

    @Test
    void shouldBlockPreferencesWithoutToken() throws Exception {
        mockMvc.perform(get("/conta/preferencias"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldCreateAndReturnDefaultPreferences() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/conta/preferencias")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tema").value("APP_DEFAULT"))
                .andExpect(jsonPath("$.data.idioma").value("pt-BR"))
                .andExpect(jsonPath("$.data.notificacoes").value(true))
                .andExpect(jsonPath("$.data.corPrimaria").value("#2563eb"))
                .andExpect(jsonPath("$.data.corSecundaria").value("#1e40af"))
                .andExpect(jsonPath("$.data.preferenciaVisual").value("APP_DEFAULT"))
                .andExpect(jsonPath("$.data.menuPrincipal").value("sidebar"))
                .andExpect(jsonPath("$.message").value("Preferencias carregadas."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldUpdatePreferences() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(put("/conta/preferencias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tema": "dark",
                                  "idioma": "en-US",
                                  "notificacoes": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tema").value("DARK"))
                .andExpect(jsonPath("$.data.idioma").value("en-US"))
                .andExpect(jsonPath("$.data.notificacoes").value(false))
                .andExpect(jsonPath("$.data.corPrimaria").value("#2563eb"))
                .andExpect(jsonPath("$.data.corSecundaria").value("#1e40af"))
                .andExpect(jsonPath("$.data.preferenciaVisual").value("APP_DEFAULT"))
                .andExpect(jsonPath("$.data.menuPrincipal").value("sidebar"))
                .andExpect(jsonPath("$.message").value("Preferencias atualizadas com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));

        mockMvc.perform(get("/conta/preferencias")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tema").value("DARK"))
                .andExpect(jsonPath("$.data.idioma").value("en-US"))
                .andExpect(jsonPath("$.data.notificacoes").value(false))
                .andExpect(jsonPath("$.data.corPrimaria").value("#2563eb"))
                .andExpect(jsonPath("$.data.corSecundaria").value("#1e40af"))
                .andExpect(jsonPath("$.data.preferenciaVisual").value("APP_DEFAULT"))
                .andExpect(jsonPath("$.data.menuPrincipal").value("sidebar"));
    }

    @Test
    void shouldAcceptLegacyPreferenceValues() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(put("/conta/preferencias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tema": "light",
                                  "preferenciaVisual": "compact"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tema").value("LIGHT"))
                .andExpect(jsonPath("$.data.preferenciaVisual").value("COMPACT"))
                .andExpect(jsonPath("$.message").value("Preferencias atualizadas com sucesso."));
    }

    @Test
    void shouldRejectInvalidPreferences() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(put("/conta/preferencias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tema": "dark",
                                  "idioma": "",
                                  "notificacoes": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Dados invalidos."));
    }

    @Test
    void shouldAllowPartialPreferencesUpdate() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(put("/conta/preferencias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "corPrimaria": "#ff0000",
                                  "preferenciaVisual": "compact"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tema").value("APP_DEFAULT"))
                .andExpect(jsonPath("$.data.idioma").value("pt-BR"))
                .andExpect(jsonPath("$.data.notificacoes").value(true))
                .andExpect(jsonPath("$.data.corPrimaria").value("#FF0000"))
                .andExpect(jsonPath("$.data.corSecundaria").value("#1e40af"))
                .andExpect(jsonPath("$.data.preferenciaVisual").value("COMPACT"))
                .andExpect(jsonPath("$.data.menuPrincipal").value("sidebar"))
                .andExpect(jsonPath("$.message").value("Preferencias atualizadas com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldAllowPartialMenuPrincipalUpdate() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(put("/conta/preferencias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "menuPrincipal": "topbar"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tema").value("APP_DEFAULT"))
                .andExpect(jsonPath("$.data.idioma").value("pt-BR"))
                .andExpect(jsonPath("$.data.notificacoes").value(true))
                .andExpect(jsonPath("$.data.corPrimaria").value("#2563eb"))
                .andExpect(jsonPath("$.data.corSecundaria").value("#1e40af"))
                .andExpect(jsonPath("$.data.preferenciaVisual").value("APP_DEFAULT"))
                .andExpect(jsonPath("$.data.menuPrincipal").value("topbar"))
                .andExpect(jsonPath("$.message").value("Preferencias atualizadas com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));

        Usuario usuario = usuarioService.buscarPorEmail("admin@baseplus.com").orElseThrow();
        assertEquals("topbar", userPreferencesRepository.findByUsuario(usuario).orElseThrow().getMenuPrincipal());
    }

    @Test
    void shouldBlockAvatarUploadWithoutToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                MediaType.IMAGE_PNG_VALUE,
                validPng()
        );

        mockMvc.perform(multipart("/conta/foto").file(file))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldUploadAndDeleteAvatar() throws Exception {
        String token = loginAndGetToken();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                MediaType.IMAGE_PNG_VALUE,
                validPng()
        );

        String uploadContent = mockMvc.perform(multipart("/conta/foto")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.avatarUrl").value(startsWith("/uploads/")))
                .andExpect(jsonPath("$.message").value("Avatar atualizado com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String avatarUrl = objectMapper.readTree(uploadContent).path("data").path("avatarUrl").asText();
        Path uploadedFile = Paths.get("uploads")
                .resolve(avatarUrl.substring("/uploads/".length()))
                .toAbsolutePath()
                .normalize();
        assertTrue(Files.exists(uploadedFile));
        mockMvc.perform(get(avatarUrl))
                .andExpect(status().isOk());

        try {
            mockMvc.perform(delete("/conta/foto")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.avatarUrl").doesNotExist())
                    .andExpect(jsonPath("$.message").value("Avatar removido com sucesso."))
                    .andExpect(jsonPath("$.errors").value(empty()));

            assertFalse(Files.exists(uploadedFile));
            mockMvc.perform(get(avatarUrl))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Recurso nao encontrado."));
        } finally {
            Files.deleteIfExists(uploadedFile);
        }
    }

    @Test
    void shouldRejectInvalidAvatarContentType() throws Exception {
        String token = loginAndGetToken();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "arquivo".getBytes()
        );

        mockMvc.perform(multipart("/conta/foto")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Arquivo invalido."));
    }

    @Test
    void shouldRejectSvgAvatarUpload() throws Exception {
        String token = loginAndGetToken();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.svg",
                "image/svg+xml",
                "<svg xmlns='http://www.w3.org/2000/svg'></svg>".getBytes()
        );

        mockMvc.perform(multipart("/conta/foto")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Arquivo invalido."));
    }

    @Test
    void shouldRejectSvgPayloadDisguisedAsPng() throws Exception {
        String token = loginAndGetToken();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                MediaType.IMAGE_PNG_VALUE,
                "<svg xmlns='http://www.w3.org/2000/svg'></svg>".getBytes()
        );

        mockMvc.perform(multipart("/conta/foto")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Arquivo invalido."));
    }

    @Test
    void shouldNotServeStoredSvgFilesFromUploads() throws Exception {
        Path storedSvg = Paths.get("uploads", "security-test", "legacy.svg").toAbsolutePath().normalize();
        Files.createDirectories(storedSvg.getParent());
        Files.writeString(storedSvg, "<svg xmlns='http://www.w3.org/2000/svg'></svg>");

        try {
            mockMvc.perform(get("/uploads/security-test/legacy.svg"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Recurso nao encontrado."));
        } finally {
            Files.deleteIfExists(storedSvg);
        }
    }

    @Test
    void shouldBlockSessionsWithoutToken() throws Exception {
        mockMvc.perform(get("/conta/sessoes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldListUserSessions() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/conta/sessoes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(notNullValue()))
                .andExpect(jsonPath("$.data[0].criadaEm").value(notNullValue()))
                .andExpect(jsonPath("$.message").value("Sessoes carregadas."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldRemoveUserSessionAndLinkedRefreshToken() throws Exception {
        JsonNode login = login();
        String token = login.path("data").path("token").asText();
        String refreshToken = login.path("data").path("refreshToken").asText();

        String sessionsContent = mockMvc.perform(get("/conta/sessoes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long sessionId = objectMapper.readTree(sessionsContent).path("data").path(0).path("id").asLong();

        mockMvc.perform(delete("/conta/sessoes/{id}", sessionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Sessao removida com sucesso."))
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
    }

    @Test
    void shouldReturnNotFoundWhenRemovingUnknownSession() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(delete("/conta/sessoes/{id}", 999999L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Sessao nao encontrada."));
    }

    private String loginAndGetToken() throws Exception {
        JsonNode json = login();
        return json.path("data").path("token").asText();
    }

    private JsonNode login() throws Exception {
        String content = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@baseplus.com",
                                  "password": "Baseplus@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(content);
    }

    private byte[] validPng() {
        return new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    }
}
