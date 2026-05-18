package com.baseplus.modules.usuario.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baseplus.core.exception.BusinessException;
import com.baseplus.modules.auth.domain.Role;
import com.baseplus.modules.auth.repository.RoleRepository;
import com.baseplus.modules.auth.service.RefreshTokenService;
import com.baseplus.modules.audit.service.AuditLogService;
import com.baseplus.modules.conta.repository.UserPreferencesRepository;
import com.baseplus.modules.conta.repository.UserSessionRepository;
import com.baseplus.modules.usuario.domain.Usuario;
import com.baseplus.modules.usuario.dto.CreateUsuarioRequest;
import com.baseplus.modules.usuario.dto.ResetSenhaUsuarioRequest;
import com.baseplus.modules.usuario.dto.UpdateUsuarioRequest;
import com.baseplus.modules.usuario.dto.UsuarioResponse;
import com.baseplus.modules.usuario.repository.UsuarioRepository;
import com.baseplus.shared.dto.PageResponse;

@Service
public class UsuarioAdminService {

    private static final String ADMIN_EMAIL = "admin@baseplus.com";
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final UserSessionRepository userSessionRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final AuditLogService auditLogService;

    public UsuarioAdminService(
            UsuarioRepository usuarioRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService,
            UserSessionRepository userSessionRepository,
            UserPreferencesRepository userPreferencesRepository,
            AuditLogService auditLogService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.userSessionRepository = userSessionRepository;
        this.userPreferencesRepository = userPreferencesRepository;
        this.auditLogService = auditLogService;
    }

    public PageResponse<UsuarioResponse> listar(String search, Boolean ativo, Boolean bloqueado, Boolean primeiroAcesso, Pageable pageable) {
        Pageable resolvedPageable = resolvePageable(pageable);
        Specification<Usuario> specification = Specification.where(matchesSearch(search))
                .and(matchesBoolean("ativo", ativo))
                .and(matchesBoolean("bloqueado", bloqueado))
                .and(matchesBoolean("trocarSenhaPrimeiroAcesso", primeiroAcesso));

        Page<Usuario> page = usuarioRepository.findAll(specification, resolvedPageable);
        List<UsuarioResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.from(page, content);
    }

    public UsuarioResponse buscar(Long id) {
        return toResponse(getUsuario(id));
    }

    @Transactional
    public UsuarioResponse criar(CreateUsuarioRequest request) {
        if (request == null || isBlank(request.nome()) || isBlank(request.email()) || isBlank(request.senha())) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Nome, email e senha sao obrigatorios."));
        }

        String email = normalizarEmail(request.email());
        if (!isEmailValido(email)) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Email invalido."));
        }

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email ja cadastrado.", HttpStatus.CONFLICT, List.of("Ja existe um usuario com este email."));
        }

        Usuario usuario = new Usuario(
                request.nome().trim(),
                email,
                passwordEncoder.encode(request.senha()),
                request.ativo() == null ? true : request.ativo()
        );
        aplicarDadosCorporativos(usuario, request.nomeExibicao(), request.cargo(), request.departamento(), request.telefone(), request.celular(), request.matricula(), request.observacoesInternas());
        usuario.setBloqueado(request.bloqueado() == null ? false : request.bloqueado());
        usuario.setTrocarSenhaPrimeiroAcesso(request.trocarSenhaPrimeiroAcesso() == null ? true : request.trocarSenhaPrimeiroAcesso());

        UsuarioResponse response = toResponse(usuarioRepository.save(usuario));
        auditLogService.registrarAutenticado("CREATE", "USUARIO", response.id());
        return response;
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UpdateUsuarioRequest request) {
        if (request == null || isBlank(request.nome()) || isBlank(request.email()) || request.ativo() == null) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Nome, email e ativo sao obrigatorios."));
        }

        Usuario usuario = getUsuario(id);
        String email = normalizarEmail(request.email());
        if (!isEmailValido(email)) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Email invalido."));
        }

        usuarioRepository.findByEmailIgnoreCase(email)
                .filter(outroUsuario -> !outroUsuario.getId().equals(usuario.getId()))
                .ifPresent(outroUsuario -> {
                    throw new BusinessException("Email ja cadastrado.", HttpStatus.CONFLICT, List.of("Ja existe um usuario com este email."));
                });

        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        aplicarDadosCorporativos(usuario, request.nomeExibicao(), request.cargo(), request.departamento(), request.telefone(), request.celular(), request.matricula(), request.observacoesInternas());
        usuario.setAtivo(request.ativo());
        if (request.bloqueado() != null) {
            usuario.setBloqueado(request.bloqueado());
        }
        if (request.trocarSenhaPrimeiroAcesso() != null) {
            usuario.setTrocarSenhaPrimeiroAcesso(request.trocarSenhaPrimeiroAcesso());
        }
        sincronizarPerfis(usuario, request.roleIds());

        UsuarioResponse response = toResponse(usuarioRepository.save(usuario));
        auditLogService.registrarAutenticado("UPDATE", "USUARIO", response.id());
        return response;
    }

    @Transactional
    public UsuarioResponse resetarSenha(Long id, ResetSenhaUsuarioRequest request) {
        if (request == null || isBlank(request.novaSenhaTemporaria())) {
            throw badRequest("Nova senha temporaria e obrigatoria.");
        }

        if (request.novaSenhaTemporaria().length() < MIN_PASSWORD_LENGTH) {
            throw badRequest("A nova senha temporaria deve ter no minimo 8 caracteres.");
        }

        Usuario usuario = getUsuario(id);
        usuario.setSenha(passwordEncoder.encode(request.novaSenhaTemporaria()));
        usuario.setTrocarSenhaPrimeiroAcesso(Boolean.TRUE.equals(request.obrigarTrocaProximoLogin()));
        usuario.setTentativasLoginInvalidas(0);
        usuario.setBloqueado(false);
        refreshTokenService.removerPorUsuario(usuario);
        userSessionRepository.deleteByUsuario(usuario);

        UsuarioResponse response = toResponse(usuarioRepository.save(usuario));
        auditLogService.registrarAutenticado("RESET_PASSWORD", "USUARIO", response.id());
        return response;
    }

    @Transactional
    public void remover(Long id) {
        Usuario usuario = getUsuario(id);
        Long usuarioAutenticadoId = getUsuarioAutenticadoId();
        if (usuario.getId().equals(usuarioAutenticadoId)) {
            throw new BusinessException("Operacao invalida.", HttpStatus.BAD_REQUEST, List.of("Nao e permitido deletar o proprio usuario autenticado."));
        }

        if (ADMIN_EMAIL.equalsIgnoreCase(usuario.getEmail())) {
            throw new BusinessException("Operacao invalida.", HttpStatus.BAD_REQUEST, List.of("Nao e permitido remover o usuario admin padrao."));
        }

        refreshTokenService.removerPorUsuario(usuario);
        userSessionRepository.deleteByUsuario(usuario);
        userPreferencesRepository.deleteByUsuario(usuario);
        usuarioRepository.delete(usuario);
        auditLogService.registrarAutenticado("DELETE", "USUARIO", usuario.getId());
    }

    private Usuario getUsuario(Long id) {
        if (id == null) {
            throw new BusinessException("Usuario nao encontrado.", HttpStatus.NOT_FOUND, List.of("Usuario nao encontrado."));
        }

        return usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado.", HttpStatus.NOT_FOUND, List.of("Usuario nao encontrado.")));
    }

    private Long getUsuarioAutenticadoId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new BusinessException("Acesso nao autorizado.", HttpStatus.UNAUTHORIZED, List.of("Autenticacao obrigatoria."));
        }

        try {
            return Long.valueOf(authentication.getPrincipal().toString());
        } catch (NumberFormatException exception) {
            throw new BusinessException("Acesso nao autorizado.", HttpStatus.UNAUTHORIZED, List.of("Token invalido."));
        }
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        List<Role> roles = usuario.getRoles()
                .stream()
                .sorted((first, second) -> first.getName().compareToIgnoreCase(second.getName()))
                .toList();

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getNomeExibicao(),
                usuario.getEmail(),
                usuario.getCargo(),
                usuario.getDepartamento(),
                usuario.getTelefone(),
                usuario.getCelular(),
                usuario.getMatricula(),
                usuario.getObservacoesInternas(),
                usuario.isAtivo(),
                usuario.isBloqueado(),
                usuario.isTrocarSenhaPrimeiroAcesso(),
                usuario.getUltimoLoginEm(),
                usuario.getTentativasLoginInvalidas(),
                usuario.getCriadoEm(),
                roles.stream()
                        .map(role -> role.getName())
                        .toList(),
                roles.stream()
                        .map(role -> new UsuarioResponse.RoleSummaryResponse(
                                role.getId(),
                                role.getName(),
                                role.getDescription(),
                                role.isAtivo(),
                                role.isSistema()
                        ))
                        .toList()
        );
    }

    private void sincronizarPerfis(Usuario usuario, List<Long> roleIds) {
        if (roleIds == null) {
            return;
        }

        Set<Long> uniqueRoleIds = new HashSet<>();
        for (Long roleId : roleIds) {
            if (roleId == null) {
                throw new BusinessException("Perfil nao encontrado.", HttpStatus.NOT_FOUND, List.of("Perfil nao encontrado."));
            }

            if (!uniqueRoleIds.add(roleId)) {
                throw new BusinessException("Perfis duplicados.", HttpStatus.BAD_REQUEST, List.of("Nao envie o mesmo perfil mais de uma vez."));
            }
        }

        Set<Role> nextRoles = new HashSet<>();
        for (Long roleId : uniqueRoleIds) {
            nextRoles.add(roleRepository.findById(roleId)
                    .orElseThrow(() -> new BusinessException("Perfil nao encontrado.", HttpStatus.NOT_FOUND, List.of("Perfil nao encontrado."))));
        }

        boolean removingAdmin = usuario.getRoles()
                .stream()
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()))
                && nextRoles.stream().noneMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
        if (removingAdmin && usuarioRepository.countByRoles_NameIgnoreCase("ADMIN") <= 1) {
            throw new BusinessException("Operacao invalida.", HttpStatus.BAD_REQUEST, List.of("Nao e permitido remover o ultimo usuario ADMIN."));
        }

        usuario.getRoles().clear();
        usuario.getRoles().addAll(nextRoles);
    }

    private Pageable resolvePageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, 10, Sort.by(Sort.Order.desc("criadoEm"), Sort.Order.desc("id")));
        }

        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Order.desc("criadoEm"), Sort.Order.desc("id"))
            );
        }

        return pageable;
    }

    private Specification<Usuario> matchesSearch(String search) {
        if (isBlank(search)) {
            return null;
        }

        String term = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nome")), term),
                cb.like(cb.lower(root.get("email")), term),
                cb.like(cb.lower(root.get("cargo")), term),
                cb.like(cb.lower(root.get("departamento")), term)
        );
    }

    private Specification<Usuario> matchesBoolean(String field, Boolean value) {
        if (value == null) {
            return null;
        }

        return (root, query, cb) -> cb.equal(root.get(field), value);
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isEmailValido(String email) {
        return email != null && email.contains("@") && email.indexOf('@') > 0 && email.indexOf('@') < email.length() - 1;
    }

    private void aplicarDadosCorporativos(
            Usuario usuario,
            String nomeExibicao,
            String cargo,
            String departamento,
            String telefone,
            String celular,
            String matricula,
            String observacoesInternas
    ) {
        usuario.setNomeExibicao(normalizarTexto(nomeExibicao));
        usuario.setCargo(normalizarTexto(cargo));
        usuario.setDepartamento(normalizarTexto(departamento));
        usuario.setTelefone(normalizarTexto(telefone));
        usuario.setCelular(normalizarTexto(celular));
        usuario.setMatricula(normalizarTexto(matricula));
        usuario.setObservacoesInternas(normalizarTexto(observacoesInternas));
    }

    private String normalizarTexto(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private BusinessException badRequest(String error) {
        return new BusinessException(error, HttpStatus.BAD_REQUEST, List.of(error));
    }
}
