package com.baseplus.core.health;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baseplus.shared.dto.ApiResponse;

@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> data = Map.of(
                "status", "UP",
                "service", "baseplus-backend",
                "timestamp", OffsetDateTime.now()
        );

        return ResponseEntity.ok(ApiResponse.success(data, "Aplicacao em execucao."));
    }

    @GetMapping("/health/ready")
    public ResponseEntity<ApiResponse<Map<String, Object>>> readiness() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            if (!Integer.valueOf(1).equals(result)) {
                return databaseUnavailableResponse();
            }

            Map<String, Object> data = Map.of(
                    "status", "UP",
                    "service", "baseplus-backend",
                    "database", "UP",
                    "timestamp", OffsetDateTime.now()
            );

            return ResponseEntity.ok(ApiResponse.success(data, "Aplicacao pronta para receber trafego."));
        } catch (DataAccessException exception) {
            return databaseUnavailableResponse();
        }
    }

    @GetMapping("/health/admin")
    @PreAuthorize("@authorizationService.hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminHealth() {
        Map<String, Object> data = Map.of(
                "status", "UP",
                "scope", "ADMIN"
        );

        return ResponseEntity.ok(ApiResponse.success(data, "Health administrativo."));
    }

    @GetMapping("/health/permission")
    @PreAuthorize("@authorizationService.hasPermission('ADMIN_ACCESS')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> permissionHealth() {
        Map<String, Object> data = Map.of(
                "status", "UP",
                "permission", "ADMIN_ACCESS"
        );

        return ResponseEntity.ok(ApiResponse.success(data, "Health por permissao."));
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> databaseUnavailableResponse() {
        Map<String, Object> data = Map.of(
                "status", "DOWN",
                "service", "baseplus-backend",
                "database", "DOWN",
                "timestamp", OffsetDateTime.now()
        );

        ApiResponse<Map<String, Object>> body = new ApiResponse<>(
                false,
                data,
                "Aplicacao indisponivel para receber trafego.",
                List.of("Banco de dados indisponivel.")
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}
