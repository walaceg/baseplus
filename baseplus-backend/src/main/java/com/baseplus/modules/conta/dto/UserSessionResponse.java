package com.baseplus.modules.conta.dto;

import java.time.OffsetDateTime;

public record UserSessionResponse(
        Long id,
        OffsetDateTime criadaEm
) {
}
