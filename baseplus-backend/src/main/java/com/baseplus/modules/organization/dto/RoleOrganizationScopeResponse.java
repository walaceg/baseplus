package com.baseplus.modules.organization.dto;

public record RoleOrganizationScopeResponse(
        Long id,
        Long organizationUnitId,
        String organizationUnitCode,
        String organizationUnitName,
        Long organizationUnitTypeId,
        String organizationUnitTypeCode,
        String organizationUnitTypeName,
        String scopeLevel
) {
}

