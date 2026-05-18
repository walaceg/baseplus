package com.baseplus.modules.organization.dto;

import java.time.OffsetDateTime;

public record OrganizationUnitResponse(
        Long id,
        OrganizationUnitTypeSummary type,
        String code,
        String name,
        OrganizationUnitSummary parent,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public record OrganizationUnitTypeSummary(
            Long id,
            String code,
            String name
    ) {
    }

    public record OrganizationUnitSummary(
            Long id,
            String code,
            String name
    ) {
    }
}

