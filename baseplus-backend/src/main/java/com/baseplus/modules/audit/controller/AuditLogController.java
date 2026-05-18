package com.baseplus.modules.audit.controller;

import java.time.OffsetDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.baseplus.modules.audit.dto.AuditLogResponse;
import com.baseplus.modules.audit.service.AuditLogService;
import com.baseplus.shared.dto.ApiResponse;
import com.baseplus.shared.dto.PageResponse;

@RestController
@RequestMapping("/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @PreAuthorize("@authorizationService.hasPermission('AUDIT_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) OffsetDateTime dataInicial,
            @RequestParam(required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) OffsetDateTime dataFinal,
            @PageableDefault(size = 10, sort = {"timestamp", "id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<AuditLogResponse> response = auditLogService.listar(search, acao, entidade, usuario, dataInicial, dataFinal, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Auditoria carregada."));
    }
}
