package com.baseplus.modules.auth;

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

import com.baseplus.modules.auth.domain.Permission;
import com.baseplus.modules.auth.repository.PermissionRepository;
import com.baseplus.modules.usuario.domain.Usuario;
import com.baseplus.modules.usuario.service.UsuarioService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PermissionControllerTest {

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
    void shouldBlockPermissionsWithoutToken() throws Exception {
        mockMvc.perform(get("/permissions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldBlockPermissionsForNonAdmin() throws Exception {
        usuarioService.salvar(new Usuario(
                "Usuario Sem Admin",
                "sem.admin.permission@baseplus.com",
                passwordEncoder.encode("Baseplus@123"),
                true
        ));
        String token = loginAndGetToken("sem.admin.permission@baseplus.com", "Baseplus@123");

        mockMvc.perform(get("/permissions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso negado."));
    }

    @Test
    void shouldListPermissionsForAdmin() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");

        mockMvc.perform(get("/permissions")
                        .header("Authorization", "Bearer " + token)
                        .param("search", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].name").value("ADMIN_ACCESS"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.message").value("Permissions carregadas."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldSearchPermissionsByNameOrDescription() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        criarPermission(token, "reports_read");

        mockMvc.perform(get("/permissions")
                        .header("Authorization", "Bearer " + token)
                        .param("search", "reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("REPORTS_READ"));
    }

    @Test
    void shouldCreatePermissionUppercase() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");

        mockMvc.perform(post("/permissions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "users_manage",
                                  "description": "Gerenciar usuarios"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(notNullValue()))
                .andExpect(jsonPath("$.data.name").value("USERS_MANAGE"))
                .andExpect(jsonPath("$.data.description").value("Gerenciar usuarios"))
                .andExpect(jsonPath("$.message").value("Permission criada com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldRejectDuplicatedPermissionName() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");

        mockMvc.perform(post("/permissions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "admin_access",
                                  "description": "Duplicada"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Permission ja cadastrada."));
    }

    @Test
    void shouldGetAndUpdatePermission() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long permissionId = criarPermission(token, "reports_read");

        mockMvc.perform(get("/permissions/{id}", permissionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(permissionId))
                .andExpect(jsonPath("$.message").value("Permission carregada."));

        mockMvc.perform(put("/permissions/{id}", permissionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "reports_export",
                                  "description": "Exportar relatorios"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(permissionId))
                .andExpect(jsonPath("$.data.name").value("REPORTS_EXPORT"))
                .andExpect(jsonPath("$.data.description").value("Exportar relatorios"))
                .andExpect(jsonPath("$.message").value("Permission atualizada com sucesso."));
    }

    @Test
    void shouldDeletePermission() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long permissionId = criarPermission(token, "temporary_access");

        mockMvc.perform(delete("/permissions/{id}", permissionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Permission removida com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));

        mockMvc.perform(get("/permissions/{id}", permissionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Permission nao encontrada."));
    }

    @Test
    void shouldDeletePermissionLinkedToRole() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long permissionId = criarPermission(token, "linked_access");
        Long roleId = criarRole(token, permissionId);

        mockMvc.perform(delete("/permissions/{id}", permissionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/roles/{id}", roleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.permissions").value(empty()));
    }

    @Test
    void shouldRejectDeletingAdminAccessPermission() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Permission adminPermission = permissionRepository.findByName("ADMIN_ACCESS").orElseThrow();

        mockMvc.perform(delete("/permissions/{id}", adminPermission.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Operacao invalida."));
    }

    private Long criarPermission(String token, String name) throws Exception {
        String content = mockMvc.perform(post("/permissions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "Permission de teste"
                                }
                                """.formatted(name)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(content).path("data").path("id").asLong();
    }

    private Long criarRole(String token, Long permissionId) throws Exception {
        String content = mockMvc.perform(post("/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "linked_role",
                                  "description": "Role vinculada",
                                  "permissionIds": [%d]
                                }
                                """.formatted(permissionId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(content).path("data").path("id").asLong();
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
