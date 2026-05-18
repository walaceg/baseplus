package com.baseplus.modules.auth.dto;

import java.util.List;

public record LoginResponse(
        String token,
        String tokenType,
        Long expiresIn,
        String refreshToken,
        Long refreshExpiresIn,
        List<String> roles,
        List<String> permissions,
        boolean mustChangePassword
) {
}
