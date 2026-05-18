package com.baseplus.core.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.baseplus.modules.usuario.service.UsuarioService;
import com.baseplus.shared.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class InitialPasswordChangeFilter extends OncePerRequestFilter {

    private final UsuarioService usuarioService;
    private final ObjectMapper objectMapper;

    public InitialPasswordChangeFilter(UsuarioService usuarioService, ObjectMapper objectMapper) {
        this.usuarioService = usuarioService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null || isAllowedPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Long usuarioId = getUsuarioId(authentication);
        if (usuarioId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean mustChangePassword = usuarioService.buscarPorId(usuarioId)
                .filter(usuario -> usuario.isAtivo() && !usuario.isBloqueado())
                .map(usuario -> usuario.isTrocarSenhaPrimeiroAcesso())
                .orElse(false);

        if (!mustChangePassword) {
            filterChain.doFilter(request, response);
            return;
        }

        ApiResponse<Void> body = ApiResponse.failure(
                "Troca de senha obrigatoria.",
                List.of("Altere sua senha inicial antes de acessar o sistema.")
        );
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private boolean isAllowedPath(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        return path.equals("/auth/change-initial-password")
                || path.equals("/auth/me")
                || path.equals("/auth/logout")
                || path.equals("/auth/refresh")
                || path.equals("/auth/login")
                || path.equals("/branding")
                || path.startsWith("/uploads/")
                || path.startsWith("/h2-console/")
                || path.equals("/health");
    }

    private Long getUsuarioId(Authentication authentication) {
        try {
            return Long.valueOf(authentication.getPrincipal().toString());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
