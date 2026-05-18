package com.baseplus.modules.auth.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.baseplus.modules.organization.dto.RoleOrganizationScopeResponse;

public record RoleResponse(
        Long id,
        String name,
        String description,
        String type,
        boolean ativo,
        boolean sistema,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm,
        List<PermissionResponse> permissions,
        List<RoleOrganizationScopeResponse> organizationScopes
) {

    public record PermissionResponse(
            Long id,
            String name,
            String description
    ) {
    }
}
