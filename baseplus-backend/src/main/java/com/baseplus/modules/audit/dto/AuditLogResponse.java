package com.baseplus.modules.audit.dto;

import java.time.OffsetDateTime;

public record AuditLogResponse(
        String usuario,
        String acao,
        String entidade,
        Long entidadeId,
        OffsetDateTime timestamp
) {
}
