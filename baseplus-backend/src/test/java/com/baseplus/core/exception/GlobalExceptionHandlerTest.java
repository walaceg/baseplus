package com.baseplus.core.exception;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnBusinessExceptionResponse() throws Exception {
        mockMvc.perform(get("/test/business-error"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.message").value("Regra de negocio invalida."))
                .andExpect(jsonPath("$.errors").value(contains("Campo obrigatorio.")));
    }

    @Test
    void shouldReturnUnexpectedExceptionResponse() throws Exception {
        mockMvc.perform(get("/test/unexpected-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.message").value("Erro interno do servidor."))
                .andExpect(jsonPath("$.errors").value(contains("Ocorreu um erro inesperado.")));
    }

    @RestController
    public static class TestController {

        @GetMapping("/test/business-error")
        public void businessError() {
            throw new BusinessException(
                    "Regra de negocio invalida.",
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    java.util.List.of("Campo obrigatorio.")
            );
        }

        @GetMapping("/test/unexpected-error")
        public void unexpectedError() {
            throw new IllegalStateException("Erro tecnico.");
        }
    }
}
