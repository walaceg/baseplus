package com.baseplus.modules.audit;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldBlockAuditLogsWithoutToken() throws Exception {
        mockMvc.perform(get("/audit-logs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldReturnMappedAuditLogsForAdmin() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/audit-logs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].usuario").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].acao").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].entidade").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].timestamp").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].id").doesNotExist())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.message").value("Auditoria carregada."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldLogCreateRoleWithoutReturningEntity() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "audit_test_role",
                                  "description": "Role de teste",
                                  "permissionIds": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/audit-logs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].usuario").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].acao").value("CREATE"))
                .andExpect(jsonPath("$.data.content[0].entidade").value("ROLE"))
                .andExpect(jsonPath("$.data.content[0].entidadeId").value(notNullValue()));
    }

    @Test
    void shouldFilterAuditLogsByActionEntityAndUser() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "audit_filter_role",
                                  "description": "Role de filtro",
                                  "permissionIds": []
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/audit-logs")
                        .header("Authorization", "Bearer " + token)
                        .param("acao", "CREATE")
                        .param("entidade", "ROLE")
                        .param("usuario", "admin@baseplus.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].acao").value("CREATE"))
                .andExpect(jsonPath("$.data.content[0].entidade").value("ROLE"))
                .andExpect(jsonPath("$.data.content[0].usuario").value("admin@baseplus.com"))
                .andExpect(jsonPath("$.message").value("Auditoria carregada."));
    }

    @Test
    void shouldSearchAuditLogsByCombinedSearch() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "audit_search_role",
                                  "description": "Role de busca",
                                  "permissionIds": []
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/audit-logs")
                        .header("Authorization", "Bearer " + token)
                        .param("search", "role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].entidade").value("ROLE"));
    }

    @Test
    void shouldFilterAuditLogsByDateRange() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/audit-logs")
                        .header("Authorization", "Bearer " + token)
                        .param("dataInicial", OffsetDateTime.now().plusMinutes(1).toString())
                        .param("dataFinal", OffsetDateTime.now().plusMinutes(2).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.message").value("Auditoria carregada."));

        mockMvc.perform(get("/audit-logs")
                        .header("Authorization", "Bearer " + token)
                        .param("dataInicial", OffsetDateTime.now().minusMinutes(5).toString())
                        .param("dataFinal", OffsetDateTime.now().plusMinutes(5).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].usuario").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].timestamp").value(notNullValue()))
                .andExpect(jsonPath("$.message").value("Auditoria carregada."));
    }

    private String loginAndGetToken() throws Exception {
        String content = mockMvc.perform(post("/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
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
