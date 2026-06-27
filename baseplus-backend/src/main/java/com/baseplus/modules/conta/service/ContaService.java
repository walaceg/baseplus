package com.baseplus.modules.conta.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baseplus.core.exception.BusinessException;
import com.baseplus.core.storage.FileStorageService;
import com.baseplus.modules.auth.service.RefreshTokenService;
import com.baseplus.modules.conta.domain.UserPreferences;
import com.baseplus.modules.conta.domain.UserSession;
import com.baseplus.modules.conta.dto.AvatarResponse;
import com.baseplus.modules.conta.dto.ChangePasswordRequest;
import com.baseplus.modules.conta.dto.ContaResponse;
import com.baseplus.modules.conta.dto.UpdateContaRequest;
import com.baseplus.modules.conta.dto.UpdateUserPreferencesRequest;
import com.baseplus.modules.conta.dto.UserPreferencesResponse;
import com.baseplus.modules.conta.dto.UserSessionResponse;
import com.baseplus.modules.conta.repository.UserPreferencesRepository;
import com.baseplus.modules.conta.repository.UserSessionRepository;
import com.baseplus.modules.usuario.domain.Usuario;
import com.baseplus.modules.usuario.service.UsuarioService;

@Service
public class ContaService {

    private static final String DEFAULT_PRIMARY_COLOR = "#2563eb";
    private static final String DEFAULT_SECONDARY_COLOR = "#1e40af";
    private static final String DEFAULT_THEME_PREFERENCE = "APP_DEFAULT";
    private static final String DEFAULT_VISUAL_PREFERENCE = "APP_DEFAULT";
    private static final String DEFAULT_MENU_PRINCIPAL = "sidebar";
    private static final long MAX_AVATAR_SIZE_BYTES = 5L * 1024 * 1024;
    private static final String AVATAR_UPLOAD_DIR = "avatars";

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final UserPreferencesRepository userPreferencesRepository;
    private final UserSessionRepository userSessionRepository;
    private final RefreshTokenService refreshTokenService;
    private final FileStorageService fileStorageService;

    public ContaService(
            UsuarioService usuarioService,
            PasswordEncoder passwordEncoder,
            UserPreferencesRepository userPreferencesRepository,
            UserSessionRepository userSessionRepository,
            RefreshTokenService refreshTokenService,
            FileStorageService fileStorageService
    ) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.userPreferencesRepository = userPreferencesRepository;
        this.userSessionRepository = userSessionRepository;
        this.refreshTokenService = refreshTokenService;
        this.fileStorageService = fileStorageService;
    }

    public ContaResponse obterConta() {
        Usuario usuario = getUsuarioAutenticado();
        return toResponse(usuario);
    }

    public ContaResponse atualizarConta(UpdateContaRequest request) {
        if (request == null || isBlank(request.nome()) || isBlank(request.email())) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Nome e email sao obrigatorios."));
        }

        String email = request.email().trim().toLowerCase();
        if (!isEmailValido(email)) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Email invalido."));
        }

        Usuario usuario = getUsuarioAutenticado();
        usuarioService.buscarPorEmail(email)
                .filter(outroUsuario -> !outroUsuario.getId().equals(usuario.getId()))
                .ifPresent(outroUsuario -> {
                    throw new BusinessException("Email ja cadastrado.", HttpStatus.CONFLICT, List.of("Ja existe um usuario com este email."));
                });

        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);

        return toResponse(usuarioService.salvar(usuario));
    }

    public void alterarSenha(ChangePasswordRequest request) {
        if (request == null || isBlank(request.senhaAtual()) || isBlank(request.novaSenha())) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Senha atual e nova senha sao obrigatorias."));
        }

        Usuario usuario = getUsuarioAutenticado();
        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
            throw new BusinessException("Senha atual invalida.", HttpStatus.BAD_REQUEST, List.of("A senha atual informada nao confere."));
        }

        if (passwordEncoder.matches(request.novaSenha(), usuario.getSenha())) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("A nova senha deve ser diferente da senha atual."));
        }

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        usuarioService.salvar(usuario);
    }

    public UserPreferencesResponse obterPreferencias() {
        UserPreferences preferences = getOrCreatePreferencias(getUsuarioAutenticado());
        return toPreferencesResponse(preferences);
    }

    public UserPreferencesResponse atualizarPreferencias(UpdateUserPreferencesRequest request) {
        if (request == null) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Ao menos um campo deve ser informado."));
        }

        UserPreferences preferences = getOrCreatePreferencias(getUsuarioAutenticado());
        if (request.tema() != null) {
            if (isBlank(request.tema())) {
                throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Tema nao pode ser vazio."));
            }
            preferences.setTema(resolveThemePreference(request.tema()));
        }

        if (request.idioma() != null) {
            if (isBlank(request.idioma())) {
                throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Idioma nao pode ser vazio."));
            }
            preferences.setIdioma(request.idioma().trim());
        }

        if (request.notificacoes() != null) {
            preferences.setNotificacoes(request.notificacoes());
        }

        if (request.corPrimaria() != null || preferences.getCorPrimaria() == null) {
            preferences.setCorPrimaria(resolveColor(request.corPrimaria(), preferences.getCorPrimaria(), DEFAULT_PRIMARY_COLOR));
        }

        if (request.corSecundaria() != null || preferences.getCorSecundaria() == null) {
            preferences.setCorSecundaria(resolveColor(request.corSecundaria(), preferences.getCorSecundaria(), DEFAULT_SECONDARY_COLOR));
        }

        if (request.preferenciaVisual() != null) {
            preferences.setPreferenciaVisual(resolveDensityPreference(request.preferenciaVisual()));
        } else if (preferences.getPreferenciaVisual() == null) {
            preferences.setPreferenciaVisual(DEFAULT_VISUAL_PREFERENCE);
        }

        if (request.menuPrincipal() != null) {
            preferences.setMenuPrincipal(resolveMenuPrincipal(request.menuPrincipal()));
        } else if (preferences.getMenuPrincipal() == null) {
            preferences.setMenuPrincipal(DEFAULT_MENU_PRINCIPAL);
        }

        return toPreferencesResponse(userPreferencesRepository.save(preferences));
    }

    public AvatarResponse salvarAvatar(MultipartFile file) {
        Usuario usuario = getUsuarioAutenticado();
        String previousAvatarUrl = usuario.getAvatarUrl();
        var storedFile = fileStorageService.saveImage(file, AVATAR_UPLOAD_DIR, MAX_AVATAR_SIZE_BYTES);
        usuario.setAvatarUrl(storedFile.url());

        try {
            Usuario saved = usuarioService.salvar(usuario);
            if (previousAvatarUrl != null && !previousAvatarUrl.equals(storedFile.url())) {
                fileStorageService.deleteByUrl(previousAvatarUrl);
            }
            return new AvatarResponse(saved.getAvatarUrl());
        } catch (RuntimeException exception) {
            fileStorageService.deleteByUrl(storedFile.url());
            throw exception;
        }
    }

    public AvatarResponse removerAvatar() {
        Usuario usuario = getUsuarioAutenticado();
        fileStorageService.deleteByUrl(usuario.getAvatarUrl());
        usuario.setAvatarUrl(null);
        usuarioService.salvar(usuario);
        return new AvatarResponse(null);
    }

    public List<UserSessionResponse> listarSessoes() {
        Usuario usuario = getUsuarioAutenticado();
        return userSessionRepository.findByUsuarioOrderByCriadaEmDesc(usuario)
                .stream()
                .map(this::toSessionResponse)
                .toList();
    }

    @Transactional
    public void removerSessao(Long id) {
        if (id == null) {
            throw new BusinessException("Sessao nao encontrada.", HttpStatus.NOT_FOUND, List.of("Sessao nao encontrada para o usuario autenticado."));
        }

        Usuario usuario = getUsuarioAutenticado();
        UserSession session = userSessionRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new BusinessException("Sessao nao encontrada.", HttpStatus.NOT_FOUND, List.of("Sessao nao encontrada para o usuario autenticado.")));

        refreshTokenService.removerPorSessao(session);
        userSessionRepository.delete(session);
    }

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new BusinessException("Acesso nao autorizado.", HttpStatus.UNAUTHORIZED, List.of("Autenticacao obrigatoria."));
        }

        Long usuarioId = getUsuarioId(authentication);
        return usuarioService.buscarPorId(usuarioId)
                .filter(Usuario::isAtivo)
                .filter(usuario -> !usuario.isBloqueado())
                .orElseThrow(() -> new BusinessException("Acesso nao autorizado.", HttpStatus.UNAUTHORIZED, List.of("Usuario autenticado nao encontrado.")));
    }

    private Long getUsuarioId(Authentication authentication) {
        try {
            return Long.valueOf(authentication.getPrincipal().toString());
        } catch (NumberFormatException exception) {
            throw new BusinessException("Acesso nao autorizado.", HttpStatus.UNAUTHORIZED, List.of("Token invalido."));
        }
    }

    private ContaResponse toResponse(Usuario usuario) {
        return new ContaResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getAvatarUrl());
    }

    private UserPreferences getOrCreatePreferencias(Usuario usuario) {
        return userPreferencesRepository.findByUsuario(usuario)
                .orElseGet(() -> userPreferencesRepository.save(new UserPreferences(
                        usuario,
                        DEFAULT_THEME_PREFERENCE,
                        "pt-BR",
                        true,
                        DEFAULT_PRIMARY_COLOR,
                        DEFAULT_SECONDARY_COLOR,
                        DEFAULT_VISUAL_PREFERENCE,
                        DEFAULT_MENU_PRINCIPAL
                )));
    }

    private UserPreferencesResponse toPreferencesResponse(UserPreferences preferences) {
        return new UserPreferencesResponse(
                resolveThemePreference(preferences.getTema()),
                preferences.getIdioma(),
                preferences.isNotificacoes(),
                preferences.getCorPrimaria(),
                preferences.getCorSecundaria(),
                resolveDensityPreference(preferences.getPreferenciaVisual()),
                preferences.getMenuPrincipal() == null ? DEFAULT_MENU_PRINCIPAL : preferences.getMenuPrincipal()
        );
    }

    private UserSessionResponse toSessionResponse(UserSession session) {
        return new UserSessionResponse(session.getId(), session.getCriadaEm());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isEmailValido(String email) {
        return email.contains("@") && email.indexOf('@') > 0 && email.indexOf('@') < email.length() - 1;
    }

    private String resolveColor(String color, String currentColor, String fallbackColor) {
        if (isBlank(color)) {
            return currentColor == null ? fallbackColor : currentColor;
        }

        String normalized = color.trim().toUpperCase();
        if (!normalized.matches("#([0-9A-F]{3}|[0-9A-F]{6})")) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Cor invalida."));
        }

        return normalized;
    }

    private String resolveThemePreference(String preference) {
        if (isBlank(preference)) {
            return DEFAULT_THEME_PREFERENCE;
        }

        String normalized = preference.trim().toUpperCase();
        if (!"APP_DEFAULT".equals(normalized) && !"LIGHT".equals(normalized) && !"DARK".equals(normalized)) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Tema invalido."));
        }

        return normalized;
    }

    private String resolveDensityPreference(String preference) {
        if (isBlank(preference)) {
            return DEFAULT_VISUAL_PREFERENCE;
        }

        String normalized = preference.trim().toUpperCase();
        if (!"APP_DEFAULT".equals(normalized) && !"REGULAR".equals(normalized) && !"COMPACT".equals(normalized)) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Preferencia visual invalida."));
        }

        return normalized;
    }

    private String resolveMenuPrincipal(String menuPrincipal) {
        String normalized = menuPrincipal.trim().toLowerCase();
        if (!"sidebar".equals(normalized) && !"topbar".equals(normalized)) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Menu principal invalido."));
        }

        return normalized;
    }
}
