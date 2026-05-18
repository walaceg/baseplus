package com.baseplus.modules.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baseplus.modules.auth.dto.AuthUserResponse;
import com.baseplus.modules.auth.dto.ChangeInitialPasswordRequest;
import com.baseplus.modules.auth.dto.LoginRequest;
import com.baseplus.modules.auth.dto.LoginResponse;
import com.baseplus.modules.auth.dto.RefreshTokenRequest;
import com.baseplus.modules.auth.dto.RefreshTokenResponse;
import com.baseplus.modules.auth.service.AuthService;
import com.baseplus.shared.dto.ApiResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login realizado com sucesso."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(@RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token renovado com sucesso."));
    }

    @PostMapping("/change-initial-password")
    public ResponseEntity<ApiResponse<Void>> changeInitialPassword(@RequestBody ChangeInitialPasswordRequest request) {
        authService.changeInitialPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Senha alterada com sucesso."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.success(null, "Logout realizado com sucesso."));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthUserResponse>> me() {
        AuthUserResponse response = authService.me();
        return ResponseEntity.ok(ApiResponse.success(response, "Usuario autenticado."));
    }
}
