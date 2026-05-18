package com.baseplus.modules.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
