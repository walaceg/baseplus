package com.baseplus.modules.auth.dto;

public record CreatePermissionRequest(
        String name,
        String description
) {
}
