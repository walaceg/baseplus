package com.baseplus.core.health;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baseplus.shared.dto.ApiResponse;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> data = Map.of(
                "status", "UP",
                "service", "baseplus-backend",
                "timestamp", OffsetDateTime.now()
        );

        return ResponseEntity.ok(ApiResponse.success(data, "Aplicacao em execucao."));
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
}
