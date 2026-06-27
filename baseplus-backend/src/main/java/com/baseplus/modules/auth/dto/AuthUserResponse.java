package com.baseplus.modules.auth.dto;

import java.util.List;

public record AuthUserResponse(
        Long id,
        String nome,
        String email,
        String avatarUrl,
        List<String> roles,
        List<String> permissions,
        boolean mustChangePassword
) {
}
