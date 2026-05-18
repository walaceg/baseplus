package com.baseplus.modules.organization.dto;

import java.time.OffsetDateTime;

public record OrganizationUnitTypeResponse(
        Long id,
        String code,
        String name,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

