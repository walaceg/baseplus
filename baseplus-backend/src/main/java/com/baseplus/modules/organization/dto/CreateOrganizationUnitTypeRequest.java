package com.baseplus.modules.organization.dto;

public record CreateOrganizationUnitTypeRequest(
        String code,
        String name,
        Boolean active
) {
}

