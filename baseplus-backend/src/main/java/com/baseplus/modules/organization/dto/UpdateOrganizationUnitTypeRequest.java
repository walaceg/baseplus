package com.baseplus.modules.organization.dto;

public record UpdateOrganizationUnitTypeRequest(
        String code,
        String name,
        Boolean active
) {
}

