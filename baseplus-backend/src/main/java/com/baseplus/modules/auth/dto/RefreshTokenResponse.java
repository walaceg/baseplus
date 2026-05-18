package com.baseplus.modules.auth.dto;

public record RefreshTokenResponse(
        String token,
        String tokenType,
        Long expiresIn
) {
}
