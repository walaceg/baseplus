package com.baseplus.modules.auth.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baseplus.core.exception.BusinessException;
import com.baseplus.modules.auth.domain.Permission;
import com.baseplus.modules.auth.domain.Role;
import com.baseplus.modules.auth.dto.AuthUserResponse;
import com.baseplus.modules.auth.dto.ChangeInitialPasswordRequest;
import com.baseplus.modules.auth.dto.LoginRequest;
import com.baseplus.modules.auth.dto.LoginResponse;
import com.baseplus.modules.auth.dto.RefreshTokenRequest;
import com.baseplus.modules.auth.dto.RefreshTokenResponse;
import com.baseplus.modules.audit.service.AuditLogService;
import com.baseplus.modules.conta.domain.UserSession;
import com.baseplus.modules.conta.repository.UserSessionRepository;
import com.baseplus.modules.usuario.domain.Usuario;
import com.baseplus.modules.usuario.service.UsuarioService;

@Service
public class AuthService {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_INVALID_LOGIN_ATTEMPTS = 5;

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;
    private final RefreshTokenService refreshTokenService;
    private final UserSessionRepository userSessionRepository;
    private final AuditLogService auditLogService;

    public AuthService(
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            UsuarioService usuarioService,
            RefreshTokenService refreshTokenService,
            UserSessionRepository userSessionRepository,
            AuditLogService auditLogService
    ) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.usuarioService = usuarioService;
        this.refreshTokenService = refreshTokenService;
        this.userSessionRepository = userSessionRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public LoginResponse login(LoginRequest request) {
        if (request == null || isBlank(request.email()) || isBlank(request.password())) {
            throw new BusinessException("Credenciais invalidas.", HttpStatus.UNAUTHORIZED, java.util.List.of("Email e senha sao obrigatorios."));
        }

        Usuario usuario = usuarioService.buscarPorEmail(request.email())
                .orElseThrow(() -> invalidCredentials("Email ou senha incorretos."));

        if (!usuario.isAtivo()) {
            throw new BusinessException("Usuário inativo", HttpStatus.UNAUTHORIZED, java.util.List.of("Usuário inativo"));
        }

        if (usuario.isBloqueado()) {
            throw new BusinessException("Usuário bloqueado", HttpStatus.UNAUTHORIZED, java.util.List.of("Usuário bloqueado"));
        }

        if (!passwordEncoder.matches(request.password(), usuario.getSenha())) {
            usuario.registrarLoginInvalido();
            if (usuario.getTentativasLoginInvalidas() >= MAX_INVALID_LOGIN_ATTEMPTS) {
                usuario.setBloqueado(true);
            }
            usuarioService.salvar(usuario);
            throw invalidCredentials("Email ou senha incorretos.");
        }

        usuario.registrarLoginBemSucedido();
        usuarioService.salvar(usuario);

        List<String> roles = usuario.getRoles()
                .stream()
                .filter(Role::isAtivo)
                .map(Role::getName)
                .sorted()
                .toList();

        List<String> permissions = usuario.getRoles()
                .stream()
                .filter(Role::isAtivo)
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .sorted()
                .toList();

        String token = jwtService.generateToken(usuario.getId().toString(), roles, permissions);
        UserSession session = userSessionRepository.save(new UserSession(usuario));
        var refreshToken = refreshTokenService.criar(usuario, session);
        auditLogService.registrar(
                formatarUsuario(usuario),
                "LOGIN",
                "AUTH",
                usuario.getId()
        );

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                refreshToken.getToken(),
                refreshTokenService.getExpirationSeconds(),
                roles,
                permissions,
                usuario.isTrocarSenhaPrimeiroAcesso()
        );
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public void changeInitialPassword(ChangeInitialPasswordRequest request) {
        if (request == null) {
            throw badRequest("Dados da troca de senha sao obrigatorios.");
        }

        if (isBlank(request.senhaAtual())) {
            throw badRequest("Senha atual e obrigatoria.");
        }

        if (isBlank(request.novaSenha())) {
            throw badRequest("Nova senha e obrigatoria.");
        }

        if (isBlank(request.confirmarNovaSenha())) {
            throw badRequest("Confirmacao da nova senha e obrigatoria.");
        }

        Usuario usuario = getAuthenticatedUserAllowingInitialPasswordChange();
        if (!usuario.isTrocarSenhaPrimeiroAcesso()) {
            throw badRequest("Troca de senha inicial nao requerida.");
        }

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
            usuario.registrarLoginInvalido();
            usuarioService.salvar(usuario);
            throw new BusinessException("Senha atual invalida.", HttpStatus.BAD_REQUEST, java.util.List.of("A senha atual informada nao confere."));
        }

        if (!request.novaSenha().equals(request.confirmarNovaSenha())) {
            throw badRequest("Nova senha e confirmacao devem ser iguais.");
        }

        if (request.novaSenha().length() < MIN_PASSWORD_LENGTH) {
            throw badRequest("A nova senha deve ter no minimo 8 caracteres.");
        }

        if (passwordEncoder.matches(request.novaSenha(), usuario.getSenha())) {
            throw badRequest("A nova senha deve ser diferente da senha atual.");
        }

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        usuario.setTrocarSenhaPrimeiroAcesso(false);
        usuario.setTentativasLoginInvalidas(0);
        usuarioService.salvar(usuario);
    }

    @Transactional
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        var refreshToken = refreshTokenService.validar(request == null ? null : request.refreshToken());
        Usuario usuario = refreshToken.getUsuario();

        if (!usuario.isAtivo()) {
            throw new BusinessException("Refresh token invalido.", HttpStatus.UNAUTHORIZED, java.util.List.of("Usuario inativo."));
        }
        if (usuario.isBloqueado()) {
            throw new BusinessException("Refresh token invalido.", HttpStatus.UNAUTHORIZED, java.util.List.of("Usuário bloqueado"));
        }

        String token = gerarAccessToken(usuario);
        return new RefreshTokenResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }

    @Transactional
    public void logout() {
        Usuario usuario = getAuthenticatedUser();
        refreshTokenService.removerPorUsuario(usuario);
        auditLogService.registrar(
                formatarUsuario(usuario),
                "LOGOUT",
                "AUTH",
                usuario.getId()
        );
    }

    public AuthUserResponse me() {
        Usuario usuario = getAuthenticatedUser();
        return new AuthUserResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getAvatarUrl(),
                getActiveRoles(usuario),
                getActivePermissions(usuario),
                usuario.isTrocarSenhaPrimeiroAcesso()
        );
    }

    private Usuario getAuthenticatedUser() {
        return getAuthenticatedUserAllowingInitialPasswordChange();
    }

    private Usuario getAuthenticatedUserAllowingInitialPasswordChange() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new BusinessException("Acesso nao autorizado.", HttpStatus.UNAUTHORIZED, java.util.List.of("Autenticacao obrigatoria."));
        }

        return usuarioService.buscarPorId(getAuthenticatedUserId(authentication))
                .filter(Usuario::isAtivo)
                .filter(usuario -> !usuario.isBloqueado())
                .orElseThrow(() -> new BusinessException("Acesso nao autorizado.", HttpStatus.UNAUTHORIZED, java.util.List.of("Usuario autenticado nao encontrado.")));
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        try {
            return Long.valueOf(authentication.getPrincipal().toString());
        } catch (NumberFormatException exception) {
            throw new BusinessException("Acesso nao autorizado.", HttpStatus.UNAUTHORIZED, java.util.List.of("Token invalido."));
        }
    }

    private BusinessException invalidCredentials(String error) {
        return new BusinessException("Credenciais invalidas.", HttpStatus.UNAUTHORIZED, java.util.List.of(error));
    }

    private BusinessException badRequest(String error) {
        return new BusinessException(error, HttpStatus.BAD_REQUEST, java.util.List.of(error));
    }

    private String gerarAccessToken(Usuario usuario) {
        List<String> roles = getActiveRoles(usuario);
        List<String> permissions = getActivePermissions(usuario);

        return jwtService.generateToken(usuario.getId().toString(), roles, permissions);
    }

    private List<String> getActiveRoles(Usuario usuario) {
        return usuario.getRoles()
                .stream()
                .filter(Role::isAtivo)
                .map(Role::getName)
                .sorted()
                .toList();
    }

    private List<String> getActivePermissions(Usuario usuario) {
        return usuario.getRoles()
                .stream()
                .filter(Role::isAtivo)
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .sorted()
                .toList();
    }

    private String formatarUsuario(Usuario usuario) {
        if (usuario == null) {
            return "SYSTEM";
        }

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            return usuario.getNome();
        }

        return usuario.getEmail().trim().toLowerCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
