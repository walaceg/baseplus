package com.baseplus.modules.auth;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Test
    void shouldBlockRolesWithoutToken() throws Exception {
        mockMvc.perform(get("/roles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldBlockRolesForNonAdmin() throws Exception {
        usuarioService.salvar(new Usuario(
                "Usuario Sem Admin",
                "sem.admin@baseplus.com",
                passwordEncoder.encode("Baseplus@123"),
                true
        ));
        String token = loginAndGetToken("sem.admin@baseplus.com", "Baseplus@123");

        mockMvc.perform(get("/roles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso negado."));
    }

    @Test
    void shouldListRolesForAdmin() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");

        mockMvc.perform(get("/roles")
                        .header("Authorization", "Bearer " + token)
                        .param("search", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].name").value("ADMIN"))
                .andExpect(jsonPath("$.data.content[0].ativo").value(true))
                .andExpect(jsonPath("$.data.content[0].sistema").value(true))
                .andExpect(jsonPath("$.data.content[0].criadoEm").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].atualizadoEm").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].permissions").value(notNullValue()))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.message").value("Roles carregadas."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldSearchRolesByNameOrDescription() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        criarRole(token, "support");

        mockMvc.perform(get("/roles")
                        .header("Authorization", "Bearer " + token)
                        .param("search", "support"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("SUPPORT"));
    }

    @Test
    void shouldCreateRoleWithPermissions() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long permissionId = getAdminPermission().getId();

        mockMvc.perform(post("/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "manager",
                                  "description": "Gestao operacional",
                                  "ativo": true,
                                  "permissionIds": [%d]
                                }
                                """.formatted(permissionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(notNullValue()))
                .andExpect(jsonPath("$.data.name").value("MANAGER"))
                .andExpect(jsonPath("$.data.description").value("Gestao operacional"))
                .andExpect(jsonPath("$.data.ativo").value(true))
                .andExpect(jsonPath("$.data.sistema").value(false))
                .andExpect(jsonPath("$.data.criadoEm").value(notNullValue()))
                .andExpect(jsonPath("$.data.atualizadoEm").value(notNullValue()))
                .andExpect(jsonPath("$.data.permissions[0].id").value(permissionId))
                .andExpect(jsonPath("$.data.permissions[0].name").value("ADMIN_ACCESS"))
                .andExpect(jsonPath("$.message").value("Role criada com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldRejectDuplicatedRoleName() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");

        mockMvc.perform(post("/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "ADMIN",
                                  "description": "Duplicada",
                                  "permissionIds": []
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Role ja cadastrada."));
    }

    @Test
    void shouldGetAndUpdateRole() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long roleId = criarRole(token, "support");
        Long permissionId = getAdminPermission().getId();

        mockMvc.perform(get("/roles/{id}", roleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(roleId))
                .andExpect(jsonPath("$.message").value("Role carregada."));

        mockMvc.perform(put("/roles/{id}", roleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "support_lead",
                                  "description": "Suporte lider",
                                  "ativo": false,
                                  "permissionIds": [%d]
                                }
                                """.formatted(permissionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(roleId))
                .andExpect(jsonPath("$.data.name").value("SUPPORT_LEAD"))
                .andExpect(jsonPath("$.data.description").value("Suporte lider"))
                .andExpect(jsonPath("$.data.ativo").value(false))
                .andExpect(jsonPath("$.data.permissions[0].name").value("ADMIN_ACCESS"))
                .andExpect(jsonPath("$.message").value("Role atualizada com sucesso."));
    }

    @Test
    void shouldFilterRolesByStatusAndType() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long roleId = criarRole(token, "inactive_custom");

        mockMvc.perform(patch("/roles/{id}/status", roleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ativo": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ativo").value(false))
                .andExpect(jsonPath("$.message").value("Role desativada com sucesso."));

        mockMvc.perform(get("/roles")
                        .header("Authorization", "Bearer " + token)
                        .param("ativo", "false")
                        .param("sistema", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("INACTIVE_CUSTOM"))
                .andExpect(jsonPath("$.data.content[0].ativo").value(false))
                .andExpect(jsonPath("$.data.content[0].sistema").value(false));
    }

    @Test
    void shouldAssociateAndRemovePermissionFromRole() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long roleId = criarRole(token, "permission_ops");
        Long permissionId = getAdminPermission().getId();

        mockMvc.perform(post("/roles/{id}/permissions/{permissionId}", roleId, permissionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.permissions[0].id").value(permissionId))
                .andExpect(jsonPath("$.message").value("Permissao associada com sucesso."));

        mockMvc.perform(delete("/roles/{id}/permissions/{permissionId}", roleId, permissionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.permissions").value(empty()))
                .andExpect(jsonPath("$.message").value("Permissao removida com sucesso."));
    }

    @Test
    void shouldManageUsuariosLinkedToRole() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long roleId = criarRole(token, "user_ops");
        Usuario usuario = usuarioService.salvar(new Usuario(
                "Usuario Vinculavel",
                "usuario.vinculavel@baseplus.com",
                passwordEncoder.encode("Baseplus@123"),
                true
        ));

        mockMvc.perform(get("/roles/{id}/usuarios", roleId)
                        .header("Authorization", "Bearer " + token)
                        .param("vinculado", "false")
                        .param("search", "vinculavel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(usuario.getId()))
                .andExpect(jsonPath("$.data.content[0].nome").value("Usuario Vinculavel"))
                .andExpect(jsonPath("$.data.content[0].email").value("usuario.vinculavel@baseplus.com"))
                .andExpect(jsonPath("$.data.content[0].ativo").value(true))
                .andExpect(jsonPath("$.data.content[0].bloqueado").value(false))
                .andExpect(jsonPath("$.data.content[0].avatarUrl").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].roles").doesNotExist())
                .andExpect(jsonPath("$.message").value("Usuarios do perfil carregados."));

        mockMvc.perform(post("/roles/{id}/usuarios/{usuarioId}", roleId, usuario.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(usuario.getId()))
                .andExpect(jsonPath("$.data.roles[0]").value("USER_OPS"))
                .andExpect(jsonPath("$.message").value("Usuario vinculado ao perfil com sucesso."));

        mockMvc.perform(post("/roles/{id}/usuarios/{usuarioId}", roleId, usuario.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuario ja vinculado."));

        mockMvc.perform(get("/roles/{id}/usuarios", roleId)
                        .header("Authorization", "Bearer " + token)
                        .param("vinculado", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(usuario.getId()));

        mockMvc.perform(delete("/roles/{id}/usuarios/{usuarioId}", roleId, usuario.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario removido do perfil com sucesso."));

        mockMvc.perform(delete("/roles/{id}/usuarios/{usuarioId}", roleId, usuario.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuario nao vinculado."));
    }

    @Test
    void shouldRejectRemovingLastAdminUserFromAdminRole() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        Usuario admin = usuarioService.buscarPorEmail("admin@baseplus.com").orElseThrow();

        mockMvc.perform(delete("/roles/{id}/usuarios/{usuarioId}", adminRole.getId(), admin.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Operacao invalida."))
                .andExpect(jsonPath("$.errors[0]").value("Nao e permitido remover o ultimo usuario ADMIN."));
    }

    @Test
    void shouldRejectUnknownPermissionOnCreate() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");

        mockMvc.perform(post("/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "finance",
                                  "description": "Financeiro",
                                  "permissionIds": [999999]
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Permissao nao encontrada."));
    }

    @Test
    void shouldDeleteRole() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long roleId = criarRole(token, "temporary");

        mockMvc.perform(delete("/roles/{id}", roleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Role removida com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));

        mockMvc.perform(get("/roles/{id}", roleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Role nao encontrada."));
    }

    @Test
    void shouldRejectDeletingAdminRole() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();

        mockMvc.perform(delete("/roles/{id}", adminRole.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Operacao invalida."));
    }

    @Test
    void shouldRejectDeletingRoleLinkedToUsuarios() throws Exception {
        String token = loginAndGetToken("admin@baseplus.com", "Baseplus@123");
        Long roleId = criarRole(token, "linked_role");
        Role role = roleRepository.findById(roleId).orElseThrow();
        Usuario usuario = new Usuario(
                "Usuario Perfil Vinculado",
                "perfil.vinculado@baseplus.com",
                passwordEncoder.encode("Baseplus@123"),
                true
        );
        usuario.addRole(role);
        usuarioService.salvar(usuario);

        mockMvc.perform(delete("/roles/{id}", roleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Perfil em uso."))
                .andExpect(jsonPath("$.errors[0]").value("Nao e permitido remover um perfil vinculado a usuarios."));
    }

    private Long criarRole(String token, String name) throws Exception {
        String content = mockMvc.perform(post("/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "Role de teste",
                                  "permissionIds": []
                                }
                                """.formatted(name)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(content).path("data").path("id").asLong();
    }

    private Permission getAdminPermission() {
        return permissionRepository.findByName("ADMIN_ACCESS").orElseThrow();
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
