package com.baseplus.core.health;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.baseplus.modules.auth.service.JwtService;
import com.baseplus.modules.usuario.service.UsuarioService;

@WebMvcTest(HealthController.class)
@AutoConfigureMockMvc(addFilters = false)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldReturnHealthResponse() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("baseplus-backend"))
                .andExpect(jsonPath("$.data.timestamp").value(notNullValue()))
                .andExpect(jsonPath("$.message").value("Aplicacao em execucao."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldReturnReadyWhenDatabaseIsAvailable() throws Exception {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);

        mockMvc.perform(get("/health/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("baseplus-backend"))
                .andExpect(jsonPath("$.data.database").value("UP"))
                .andExpect(jsonPath("$.data.timestamp").value(notNullValue()))
                .andExpect(jsonPath("$.message").value("Aplicacao pronta para receber trafego."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldReturnServiceUnavailableWhenDatabaseIsUnavailable() throws Exception {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new QueryTimeoutException("database unavailable"));

        mockMvc.perform(get("/health/ready"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.status").value("DOWN"))
                .andExpect(jsonPath("$.data.service").value("baseplus-backend"))
                .andExpect(jsonPath("$.data.database").value("DOWN"))
                .andExpect(jsonPath("$.data.timestamp").value(notNullValue()))
                .andExpect(jsonPath("$.message").value("Aplicacao indisponivel para receber trafego."))
                .andExpect(jsonPath("$.errors[0]").value("Banco de dados indisponivel."));
    }
}
