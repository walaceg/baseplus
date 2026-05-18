package com.baseplus.modules.organization.dto;

public record UpdateOrganizationUnitRequest(
        Long typeId,
        String code,
        String name,
        Long parentId,
        Boolean active
) {
}

