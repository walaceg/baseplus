package com.baseplus.modules.auth.dto;

import java.util.List;

import com.baseplus.modules.organization.dto.RoleOrganizationScopeRequest;

public record CreateRoleRequest(
        String name,
        String description,
        Boolean ativo,
        String type,
        List<Long> permissionIds,
        List<RoleOrganizationScopeRequest> organizationScopes
) {
}
