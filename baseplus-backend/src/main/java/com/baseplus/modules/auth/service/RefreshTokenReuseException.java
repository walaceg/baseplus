package com.baseplus.modules.auth.service;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.baseplus.core.exception.BusinessException;

public class RefreshTokenReuseException extends BusinessException {

    public RefreshTokenReuseException() {
        super("Refresh token inválido.", HttpStatus.UNAUTHORIZED, List.of("Refresh token inválido."));
    }
}
