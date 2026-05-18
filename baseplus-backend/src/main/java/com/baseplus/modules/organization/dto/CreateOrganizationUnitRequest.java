package com.baseplus.modules.organization.dto;

public record CreateOrganizationUnitRequest(
        Long typeId,
        String code,
        String name,
        Long parentId,
        Boolean active
) {
}

