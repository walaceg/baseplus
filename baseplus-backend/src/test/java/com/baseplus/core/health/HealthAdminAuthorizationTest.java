package com.baseplus.core.health;

import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.baseplus.modules.auth.service.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class HealthAdminAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldBlockAdminHealthWithoutToken() throws Exception {
        mockMvc.perform(get("/health/admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldReturnForbiddenWhenTokenHasNoAdminRole() throws Exception {
        String tokenWithoutRoles = jwtService.generateToken("1");

        mockMvc.perform(get("/health/admin")
                        .header("Authorization", "Bearer " + tokenWithoutRoles))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso negado."))
                .andExpect(jsonPath("$.errors[0]").value("Permissao insuficiente."));
    }

    @Test
    void shouldAllowAdminHealthWithAdminRole() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/health/admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.scope").value("ADMIN"))
                .andExpect(jsonPath("$.message").value("Health administrativo."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldBlockPermissionHealthWithoutToken() throws Exception {
        mockMvc.perform(get("/health/permission"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldReturnForbiddenWhenTokenHasNoAdminAccessPermission() throws Exception {
        String tokenWithoutPermissions = jwtService.generateToken("1", java.util.List.of("ADMIN"));

        mockMvc.perform(get("/health/permission")
                        .header("Authorization", "Bearer " + tokenWithoutPermissions))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso negado."))
                .andExpect(jsonPath("$.errors[0]").value("Permissao insuficiente."));
    }

    @Test
    void shouldAllowPermissionHealthWithAdminAccessPermission() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/health/permission")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.permission").value("ADMIN_ACCESS"))
                .andExpect(jsonPath("$.message").value("Health por permissao."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    private String loginAndGetToken() throws Exception {
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

        JsonNode json = objectMapper.readTree(content);
        return json.path("data").path("token").asText();
    }
}
