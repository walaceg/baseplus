package com.baseplus.core.security;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.baseplus.modules.auth.domain.Permission;
import com.baseplus.modules.auth.domain.Role;
import com.baseplus.modules.auth.service.JwtService;
import com.baseplus.modules.usuario.domain.Usuario;
import com.baseplus.modules.usuario.service.UsuarioService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioService usuarioService) {
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            if (jwtService.isTokenValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String subject = jwtService.getSubject(token);
                getActiveUsuario(subject).ifPresent(usuario -> {
                    var authorities = new ArrayList<SimpleGrantedAuthority>();
                    authorities.addAll(usuario.getRoles()
                            .stream()
                            .filter(Role::isAtivo)
                            .map(Role::getName)
                            .filter(role -> role != null && !role.isBlank())
                            .distinct()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .toList());
                    authorities.addAll(usuario.getRoles()
                            .stream()
                            .filter(Role::isAtivo)
                            .flatMap(role -> role.getPermissions().stream())
                            .map(Permission::getName)
                            .filter(permission -> permission != null && !permission.isBlank())
                            .distinct()
                            .map(SimpleGrantedAuthority::new)
                            .toList());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            subject,
                            null,
                            authorities
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            }
        }

        filterChain.doFilter(request, response);
    }

    private java.util.Optional<Usuario> getActiveUsuario(String subject) {
        try {
            return usuarioService.buscarPorId(Long.valueOf(subject))
                    .filter(Usuario::isAtivo)
                    .filter(usuario -> !usuario.isBloqueado());
        } catch (NumberFormatException exception) {
            return java.util.Optional.empty();
        }
    }
}
