package com.baseplus.modules.branding;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.mock.web.MockMultipartFile;

import com.baseplus.modules.branding.domain.BrandingSettings;
import com.baseplus.modules.branding.domain.LoginBackgroundMode;
import com.baseplus.modules.branding.repository.BrandingSettingsRepository;
import com.baseplus.modules.auth.domain.Permission;
import com.baseplus.modules.auth.domain.Role;
import com.baseplus.modules.auth.repository.PermissionRepository;
import com.baseplus.modules.auth.repository.RoleRepository;
import com.baseplus.modules.usuario.domain.Usuario;
import com.baseplus.modules.usuario.service.UsuarioService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BrandingSettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BrandingSettingsRepository brandingSettingsRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldReturnDefaultBrandingWhenDatabaseIsEmpty() throws Exception {
        brandingSettingsRepository.deleteAll();
        String token = loginAndGetToken();

        mockMvc.perform(get("/branding")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nomePlataforma").value("Base+"))
                .andExpect(jsonPath("$.data.subtituloInstitucional").value("Plataforma Base+"))
                .andExpect(jsonPath("$.data.tema").value("light"))
                .andExpect(jsonPath("$.data.corPrimaria").value("#2563eb"))
                .andExpect(jsonPath("$.data.corSecundaria").value("#1e40af"))
                .andExpect(jsonPath("$.data.densidadeVisual").value("regular"))
                .andExpect(jsonPath("$.data.loginBackgroundMode").value("DEFAULT"))
                .andExpect(jsonPath("$.data.compactLogoUrl").doesNotExist())
                .andExpect(jsonPath("$.data.faviconUrl").doesNotExist())
                .andExpect(jsonPath("$.message").value("Branding carregado."))
                .andExpect(jsonPath("$.errors").value(empty()));

        assertEquals(1L, brandingSettingsRepository.count());
    }

    @Test
    void shouldReturnPublicBrandingWithoutAuthenticationAndHideAdministrativeFields() throws Exception {
        brandingSettingsRepository.deleteAll();
        brandingSettingsRepository.save(new BrandingSettings(
                "Base+ Publica",
                "Login publico",
                "dark",
                "#123456",
                "#654321",
                "compact",
                LoginBackgroundMode.INSTITUTIONAL_GRADIENT,
                "/uploads/branding/logo/public-logo.png",
                "/uploads/branding/compact-logo/private-compact.png",
                "/uploads/branding/favicon/public.ico",
                "/uploads/branding/login-logo/public-login.png",
                "/uploads/branding/login-background/public-background.jpg",
                true,
                "Portal Cliente",
                "Acesso externo"
        ));

        mockMvc.perform(get("/branding/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nomePlataforma").value("Base+ Publica"))
                .andExpect(jsonPath("$.data.subtituloInstitucional").value("Login publico"))
                .andExpect(jsonPath("$.data.tema").value("dark"))
                .andExpect(jsonPath("$.data.corPrimaria").value("#123456"))
                .andExpect(jsonPath("$.data.corSecundaria").value("#654321"))
                .andExpect(jsonPath("$.data.densidadeVisual").value("compact"))
                .andExpect(jsonPath("$.data.loginBackgroundMode").value("INSTITUTIONAL_GRADIENT"))
                .andExpect(jsonPath("$.data.logoUrl").value("/uploads/branding/logo/public-logo.png"))
                .andExpect(jsonPath("$.data.faviconUrl").value("/uploads/branding/favicon/public.ico"))
                .andExpect(jsonPath("$.data.loginLogoUrl").value("/uploads/branding/login-logo/public-login.png"))
                .andExpect(jsonPath("$.data.loginBackgroundUrl").value("/uploads/branding/login-background/public-background.jpg"))
                .andExpect(jsonPath("$.data.whiteLabelEnabled").value(true))
                .andExpect(jsonPath("$.data.whiteLabelName").value("Portal Cliente"))
                .andExpect(jsonPath("$.data.whiteLabelSubtitle").value("Acesso externo"))
                .andExpect(jsonPath("$.data.compactLogoUrl").doesNotExist())
                .andExpect(jsonPath("$.data.id").doesNotExist())
                .andExpect(jsonPath("$.message").value("Branding publico carregado."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldUpdateBrandingWithJsonAndKeepSingleRecord() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(put("/branding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                                {
                                  "nomePlataforma": "Base+ Pro",
                                  "tema": "dark",
                                  "corPrimaria": "#112233",
                                  "loginBackgroundMode": "INSTITUTIONAL_GRADIENT",
                                  "logoUrl": "/uploads/branding/logo/logo-baseplus.png",
                                  "compactLogoUrl": "/uploads/branding/compact-logo/logo-baseplus-mark.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nomePlataforma").value("Base+ Pro"))
                .andExpect(jsonPath("$.data.subtituloInstitucional").value("Plataforma Base+"))
                .andExpect(jsonPath("$.data.tema").value("dark"))
                .andExpect(jsonPath("$.data.corPrimaria").value("#112233"))
                .andExpect(jsonPath("$.data.corSecundaria").value("#1e40af"))
                .andExpect(jsonPath("$.data.densidadeVisual").value("regular"))
                .andExpect(jsonPath("$.data.loginBackgroundMode").value("INSTITUTIONAL_GRADIENT"))
                .andExpect(jsonPath("$.data.logoUrl").value("/uploads/branding/logo/logo-baseplus.png"))
                .andExpect(jsonPath("$.data.compactLogoUrl").value("/uploads/branding/compact-logo/logo-baseplus-mark.png"))
                .andExpect(jsonPath("$.data.faviconUrl").doesNotExist())
                .andExpect(jsonPath("$.message").value("Branding atualizado com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));

        JsonNode response = objectMapper.readTree(mockMvc.perform(get("/branding")
                        .header("Authorization", "Bearer " + token))
                .andReturn()
                .getResponse()
                .getContentAsString());

        assertEquals("Base+ Pro", response.path("data").path("nomePlataforma").asText());
        assertEquals(1L, brandingSettingsRepository.count());
    }

    @Test
    void shouldReturnExistingBrandingWithoutCreatingAnotherRecord() throws Exception {
        String token = loginAndGetToken();
        brandingSettingsRepository.deleteAll();
        brandingSettingsRepository.save(new BrandingSettings(
                "Base+ One",
                "Plataforma Base+ One",
                "dark",
                "#123456",
                "#654321",
                "compact",
                LoginBackgroundMode.NEUTRAL_SURFACE,
                "/uploads/branding/logo/baseplus-one.svg",
                "/uploads/branding/compact-logo/baseplus-one-mark.svg",
                "/uploads/branding/favicon/baseplus-one.ico",
                "/uploads/branding/login-background/baseplus-one.jpg"
        ));

        mockMvc.perform(get("/branding")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").doesNotExist())
                .andExpect(jsonPath("$.data.nomePlataforma").value("Base+ One"))
                .andExpect(jsonPath("$.data.subtituloInstitucional").value("Plataforma Base+ One"))
                .andExpect(jsonPath("$.data.logoUrl").value("/uploads/branding/logo/baseplus-one.svg"))
                .andExpect(jsonPath("$.data.compactLogoUrl").value("/uploads/branding/compact-logo/baseplus-one-mark.svg"))
                .andExpect(jsonPath("$.data.faviconUrl").value("/uploads/branding/favicon/baseplus-one.ico"))
                .andExpect(jsonPath("$.message").value("Branding carregado."))
                .andExpect(jsonPath("$.errors").value(empty()));

        assertEquals(1L, brandingSettingsRepository.count());
    }

    @Test
    void shouldPreserveCompactLogoUrlWhenPutOmitsIt() throws Exception {
        brandingSettingsRepository.deleteAll();
        brandingSettingsRepository.save(new BrandingSettings(
                "Base+ Preserve",
                "Plataforma Base+ Preserve",
                "light",
                "#123456",
                "#654321",
                "regular",
                LoginBackgroundMode.DEFAULT,
                "/uploads/branding/logo/preserve-logo.png",
                "/uploads/branding/compact-logo/preserve-compact.svg",
                "/uploads/branding/favicon/preserve.ico",
                "/uploads/branding/login-background/preserve.jpg"
        ));

        String token = loginAndGetToken();

        mockMvc.perform(put("/branding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                                {
                                  "nomePlataforma": "Base+ Preserve 2",
                                  "tema": "dark"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nomePlataforma").value("Base+ Preserve 2"))
                .andExpect(jsonPath("$.data.compactLogoUrl").value("/uploads/branding/compact-logo/preserve-compact.svg"))
                .andExpect(jsonPath("$.message").value("Branding atualizado com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldNormalizeLegacyBrandingAssetUrlsOnGet() throws Exception {
        String token = loginAndGetToken();
        brandingSettingsRepository.deleteAll();
        brandingSettingsRepository.save(new BrandingSettings(
                "Base+ Legacy",
                "Plataforma Base+ Legacy",
                "light",
                "#123456",
                "#654321",
                "regular",
                LoginBackgroundMode.DEFAULT,
                "/branding/logo/legacy-logo.png",
                "/branding/compact-logo/legacy-compact-logo.svg",
                "/branding/favicon/legacy-favicon.ico",
                "/branding/login-background/legacy-background.jpg"
        ));

        mockMvc.perform(get("/branding")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.logoUrl").value("/uploads/branding/logo/legacy-logo.png"))
                .andExpect(jsonPath("$.data.compactLogoUrl").value("/uploads/branding/compact-logo/legacy-compact-logo.svg"))
                .andExpect(jsonPath("$.data.faviconUrl").value("/uploads/branding/favicon/legacy-favicon.ico"))
                .andExpect(jsonPath("$.data.loginBackgroundUrl").value("/uploads/branding/login-background/legacy-background.jpg"))
                .andExpect(jsonPath("$.message").value("Branding carregado."))
                .andExpect(jsonPath("$.errors").value(empty()));

        assertEquals(1L, brandingSettingsRepository.count());
    }

    @Test
    void shouldUploadBrandingLogoAndReturnUpdatedSettings() throws Exception {
        String token = loginAndGetToken();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "logo-baseplus.png",
                MediaType.IMAGE_PNG_VALUE,
                validPng()
        );

        mockMvc.perform(multipart("/branding/logo")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.logoUrl").value(startsWith("/uploads/branding/logo/")))
                .andExpect(jsonPath("$.message").value("Logo institucional atualizada com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldUploadBrandingCompactLogoAndReturnUpdatedSettings() throws Exception {
        String token = loginAndGetToken();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "compact-logo.png",
                MediaType.IMAGE_PNG_VALUE,
                validPng()
        );

        mockMvc.perform(multipart("/branding/compact-logo")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.compactLogoUrl").value(startsWith("/uploads/branding/compact-logo/")))
                .andExpect(jsonPath("$.message").value("Logo reduzida atualizada com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldUploadBrandingFaviconAndReturnUpdatedSettings() throws Exception {
        String token = loginAndGetToken();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "favicon.ico",
                "image/x-icon",
                validIcon()
        );

        mockMvc.perform(multipart("/branding/favicon")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.faviconUrl").value(startsWith("/uploads/branding/favicon/")))
                .andExpect(jsonPath("$.message").value("Favicon atualizado com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldRejectInvalidBrandingLogoContentType() throws Exception {
        String token = loginAndGetToken();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "logo.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "arquivo".getBytes()
        );

        mockMvc.perform(multipart("/branding/logo")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Arquivo invalido."));
    }

    @Test
    void shouldRejectEmptyBrandingLogoUpload() throws Exception {
        String token = loginAndGetToken();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "logo.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/branding/logo")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Arquivo invalido."));
    }

    @Test
    void shouldRejectSvgBrandingLogoUpload() throws Exception {
        String token = loginAndGetToken();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "logo.svg",
                "image/svg+xml",
                "<svg xmlns='http://www.w3.org/2000/svg'></svg>".getBytes()
        );

        mockMvc.perform(multipart("/branding/logo")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Arquivo invalido."));
    }

    @Test
    void shouldUploadBrandingLoginBackgroundAndReturnUpdatedSettings() throws Exception {
        String token = loginAndGetToken();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "background-login.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                validJpeg()
        );

        mockMvc.perform(multipart("/branding/login-background")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.loginBackgroundUrl").value(startsWith("/uploads/branding/login-background/")))
                .andExpect(jsonPath("$.message").value("Background institucional atualizado com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldAuthorizeBrandingWithGranularPermissions() throws Exception {
        String viewToken = createTokenWithPermissions("branding.view@baseplus.com", "BRANDING_VIEW");
        String editToken = createTokenWithPermissions("branding.edit@baseplus.com", "BRANDING_EDIT");
        String uploadToken = createTokenWithPermissions("branding.upload@baseplus.com", "BRANDING_UPLOAD_ASSETS");

        mockMvc.perform(get("/branding"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));

        mockMvc.perform(get("/branding")
                        .header("Authorization", "Bearer " + editToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Acesso negado."));

        mockMvc.perform(get("/branding")
                        .header("Authorization", "Bearer " + viewToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Branding carregado."));

        mockMvc.perform(put("/branding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + viewToken)
                        .content("""
                                {
                                  "nomePlataforma": "Base+ View"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Acesso negado."));

        mockMvc.perform(put("/branding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + editToken)
                        .content("""
                                {
                                  "nomePlataforma": "Base+ Edit",
                                  "subtituloInstitucional": "Branding granular"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nomePlataforma").value("Base+ Edit"))
                .andExpect(jsonPath("$.message").value("Branding atualizado com sucesso."));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "granular-logo.png",
                MediaType.IMAGE_PNG_VALUE,
                validPng()
        );

        mockMvc.perform(multipart("/branding/logo")
                        .file(file)
                        .header("Authorization", "Bearer " + editToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Acesso negado."));

        mockMvc.perform(multipart("/branding/logo")
                        .file(file)
                        .header("Authorization", "Bearer " + uploadToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.logoUrl").value(startsWith("/uploads/branding/logo/")));
    }

    private String loginAndGetToken() throws Exception {
        String content = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/login")
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

        return objectMapper.readTree(content).path("data").path("token").asText();
    }

    private String createTokenWithPermissions(String email, String... permissionNames) throws Exception {
        Role role = roleRepository.save(new Role("ROLE_" + email.substring(0, email.indexOf('@')).replace('.', '_').toUpperCase(), "Perfil granular de branding."));
        for (String permissionName : permissionNames) {
            Permission permission = permissionRepository.findByName(permissionName)
                    .orElseGet(() -> permissionRepository.save(new Permission(permissionName, permissionName)));
            role.addPermission(permission);
        }
        role = roleRepository.save(role);

        Usuario usuario = new Usuario(
                "Usuario Branding",
                email,
                passwordEncoder.encode("Baseplus@456"),
                true
        );
        usuario.setTrocarSenhaPrimeiroAcesso(false);
        usuario.addRole(role);
        usuarioService.salvar(usuario);

        String content = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Baseplus@456"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(content).path("data").path("token").asText();
    }

    private byte[] validPng() {
        return new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    }

    private byte[] validJpeg() {
        return new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
    }

    private byte[] validIcon() {
        return new byte[] {0x00, 0x00, 0x01, 0x00};
    }
}
