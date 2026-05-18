package com.baseplus.modules.organization.dto;

public record RoleOrganizationScopeRequest(
        Long organizationUnitId,
        String scopeLevel
) {
}

