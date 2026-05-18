package com.baseplus.modules.auth.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baseplus.core.exception.BusinessException;
import com.baseplus.modules.auth.domain.Permission;
import com.baseplus.modules.auth.domain.Role;
import com.baseplus.modules.auth.domain.RoleType;
import com.baseplus.modules.auth.dto.CreateRoleRequest;
import com.baseplus.modules.auth.dto.RoleUserResponse;
import com.baseplus.modules.auth.dto.RoleResponse;
import com.baseplus.modules.auth.dto.UpdateRoleRequest;
import com.baseplus.modules.auth.dto.UpdateRoleStatusRequest;
import com.baseplus.modules.auth.repository.PermissionRepository;
import com.baseplus.modules.auth.repository.RoleRepository;
import com.baseplus.modules.audit.service.AuditLogService;
import com.baseplus.modules.organization.domain.OrganizationScopeLevel;
import com.baseplus.modules.organization.domain.OrganizationUnit;
import com.baseplus.modules.organization.domain.RoleOrganizationScope;
import com.baseplus.modules.organization.dto.RoleOrganizationScopeRequest;
import com.baseplus.modules.organization.dto.RoleOrganizationScopeResponse;
import com.baseplus.modules.organization.repository.RoleOrganizationScopeRepository;
import com.baseplus.modules.organization.service.OrganizationUnitService;
import com.baseplus.modules.usuario.repository.UsuarioRepository;
import com.baseplus.modules.usuario.domain.Usuario;
import com.baseplus.modules.usuario.dto.UsuarioResponse;
import com.baseplus.shared.dto.PageResponse;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditLogService auditLogService;
    private final OrganizationUnitService organizationUnitService;
    private final RoleOrganizationScopeRepository roleOrganizationScopeRepository;

    public RoleService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            UsuarioRepository usuarioRepository,
            AuditLogService auditLogService,
            OrganizationUnitService organizationUnitService,
            RoleOrganizationScopeRepository roleOrganizationScopeRepository
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditLogService = auditLogService;
        this.organizationUnitService = organizationUnitService;
        this.roleOrganizationScopeRepository = roleOrganizationScopeRepository;
    }

    public PageResponse<RoleResponse> listar(String search, Boolean ativo, Boolean sistema, Pageable pageable) {
        Pageable resolvedPageable = resolvePageable(pageable);
        Specification<Role> specification = Specification.where(matchesSearch(search))
                .and(matchesBoolean("ativo", ativo))
                .and(matchesBoolean("sistema", sistema));

        Page<Role> page = roleRepository.findAll(specification, resolvedPageable);
        List<RoleResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.from(page, content);
    }

    public RoleResponse buscar(Long id) {
        return toResponse(getRole(id));
    }

    public PageResponse<RoleUserResponse> listarUsuarios(Long id, String search, Boolean vinculado, Pageable pageable) {
        Role role = getRole(id);
        Pageable resolvedPageable = resolveUsuarioPageable(pageable);
        boolean onlyLinked = vinculado == null || vinculado;
        String normalizedSearch = normalizeSearch(search);

        Page<Usuario> page = onlyLinked
                ? usuarioRepository.findByRoleId(role.getId(), normalizedSearch, resolvedPageable)
                : usuarioRepository.findWithoutRoleId(role.getId(), normalizedSearch, resolvedPageable);
        List<RoleUserResponse> content = page.getContent()
                .stream()
                .map(this::toRoleUserResponse)
                .toList();

        return PageResponse.from(page, content);
    }

    @Transactional
    public RoleResponse criar(CreateRoleRequest request) {
        if (request == null || isBlank(request.name())) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Nome da role e obrigatorio."));
        }

        String name = normalizarNome(request.name());
        if (roleRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Role ja cadastrada.", HttpStatus.CONFLICT, List.of("Ja existe uma role com este nome."));
        }

        Role role = new Role(name, normalizarDescricao(request.description()));
        role.setAtivo(request.ativo() == null ? true : request.ativo());
        role.setSistema(false);
        role.setType(resolveRoleType(request.type(), false));
        role.setPermissions(role.getType() == RoleType.ORGANIZATIONAL ? Set.of() : getPermissions(request.permissionIds()));

        Role saved = roleRepository.save(role);
        syncOrganizationScopes(saved, request.organizationScopes());
        RoleResponse response = toResponse(saved);
        auditLogService.registrarAutenticado("CREATE", "ROLE", response.id());
        return response;
    }

    @Transactional
    public RoleResponse atualizar(Long id, UpdateRoleRequest request) {
        if (request == null || isBlank(request.name())) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Nome da role e obrigatorio."));
        }

        Role role = getRole(id);
        String name = normalizarNome(request.name());
        if (roleRepository.existsByNameIgnoreCaseAndIdNot(name, role.getId())) {
            throw new BusinessException("Role ja cadastrada.", HttpStatus.CONFLICT, List.of("Ja existe uma role com este nome."));
        }

        role.setName(name);
        role.setDescription(normalizarDescricao(request.description()));
        if (request.ativo() != null) {
            role.setAtivo(request.ativo());
        }
        role.setType(resolveRoleType(request.type(), role.isSistema()));
        role.setPermissions(role.getType() == RoleType.ORGANIZATIONAL ? Set.of() : getPermissions(request.permissionIds()));
        syncOrganizationScopes(role, request.organizationScopes());

        RoleResponse response = toResponse(roleRepository.save(role));
        auditLogService.registrarAutenticado("UPDATE", "ROLE", response.id());
        return response;
    }

    @Transactional
    public RoleResponse atualizarStatus(Long id, UpdateRoleStatusRequest request) {
        if (request == null || request.ativo() == null) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Status ativo e obrigatorio."));
        }

        Role role = getRole(id);
        role.setAtivo(request.ativo());

        RoleResponse response = toResponse(roleRepository.save(role));
        auditLogService.registrarAutenticado(request.ativo() ? "ACTIVATE" : "DEACTIVATE", "ROLE", response.id());
        return response;
    }

    @Transactional
    public RoleResponse associarPermissao(Long id, Long permissionId) {
        Role role = getRole(id);
        ensureFunctionalRole(role);
        Permission permission = getPermission(permissionId);
        role.addPermission(permission);

        RoleResponse response = toResponse(roleRepository.save(role));
        auditLogService.registrarAutenticado("ADD_PERMISSION", "ROLE", response.id());
        return response;
    }

    @Transactional
    public RoleResponse removerPermissao(Long id, Long permissionId) {
        Role role = getRole(id);
        ensureFunctionalRole(role);
        Permission permission = getPermission(permissionId);
        role.getPermissions().remove(permission);

        RoleResponse response = toResponse(roleRepository.save(role));
        auditLogService.registrarAutenticado("REMOVE_PERMISSION", "ROLE", response.id());
        return response;
    }

    @Transactional
    public UsuarioResponse associarUsuario(Long id, Long usuarioId) {
        Role role = getRole(id);
        Usuario usuario = getUsuario(usuarioId);
        if (usuario.getRoles().contains(role)) {
            throw new BusinessException("Usuario ja vinculado.", HttpStatus.CONFLICT, List.of("Usuario ja esta vinculado a este perfil."));
        }

        usuario.addRole(role);
        UsuarioResponse response = toUsuarioResponse(usuarioRepository.save(usuario));
        auditLogService.registrarAutenticado("ADD_USER", "ROLE", role.getId());
        return response;
    }

    @Transactional
    public UsuarioResponse removerUsuario(Long id, Long usuarioId) {
        Role role = getRole(id);
        Usuario usuario = getUsuario(usuarioId);
        if (!usuario.getRoles().contains(role)) {
            throw new BusinessException("Usuario nao vinculado.", HttpStatus.BAD_REQUEST, List.of("Usuario nao esta vinculado a este perfil."));
        }

        if ("ADMIN".equalsIgnoreCase(role.getName()) && usuarioRepository.countByRoles_NameIgnoreCase("ADMIN") <= 1) {
            throw new BusinessException("Operacao invalida.", HttpStatus.BAD_REQUEST, List.of("Nao e permitido remover o ultimo usuario ADMIN."));
        }

        usuario.removeRole(role);
        UsuarioResponse response = toUsuarioResponse(usuarioRepository.save(usuario));
        auditLogService.registrarAutenticado("REMOVE_USER", "ROLE", role.getId());
        return response;
    }

    @Transactional
    public void remover(Long id) {
        Role role = getRole(id);
        if (role.isSistema()) {
            throw new BusinessException("Operacao invalida.", HttpStatus.BAD_REQUEST, List.of("Nao e permitido remover um perfil de sistema."));
        }

        if (usuarioRepository.existsByRoles_Id(role.getId())) {
            throw new BusinessException("Perfil em uso.", HttpStatus.CONFLICT, List.of("Nao e permitido remover um perfil vinculado a usuarios."));
        }

        roleRepository.delete(role);
        auditLogService.registrarAutenticado("DELETE", "ROLE", role.getId());
    }

    @Transactional
    public RoleResponse associarEscopoOrganizacional(Long id, RoleOrganizationScopeRequest request) {
        Role role = getRole(id);
        ensureOrganizationalRole(role);
        if (request == null || request.organizationUnitId() == null) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Unidade organizacional e obrigatoria."));
        }

        OrganizationUnit unit = organizationUnitService.getUnit(request.organizationUnitId());
        OrganizationScopeLevel level = resolveScopeLevel(request.scopeLevel());
        RoleOrganizationScope scope = roleOrganizationScopeRepository.findByRole_IdAndOrganizationUnit_Id(role.getId(), unit.getId())
                .orElseGet(() -> new RoleOrganizationScope(role, unit, level));
        scope.setScopeLevel(level);
        roleOrganizationScopeRepository.save(scope);

        auditLogService.registrarAutenticado("ADD_ORGANIZATION_SCOPE", "ROLE", role.getId());
        return toResponse(role);
    }

    @Transactional
    public RoleResponse removerEscopoOrganizacional(Long id, Long scopeId) {
        Role role = getRole(id);
        ensureOrganizationalRole(role);
        RoleOrganizationScope scope = roleOrganizationScopeRepository.findById(scopeId)
                .filter(existing -> existing.getRole().getId().equals(role.getId()))
                .orElseThrow(() -> new BusinessException("Escopo nao encontrado.", HttpStatus.NOT_FOUND, List.of("Escopo organizacional nao encontrado.")));
        roleOrganizationScopeRepository.delete(scope);

        auditLogService.registrarAutenticado("REMOVE_ORGANIZATION_SCOPE", "ROLE", role.getId());
        return toResponse(role);
    }

    private Role getRole(Long id) {
        if (id == null) {
            throw new BusinessException("Role nao encontrada.", HttpStatus.NOT_FOUND, List.of("Role nao encontrada."));
        }

        return roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Role nao encontrada.", HttpStatus.NOT_FOUND, List.of("Role nao encontrada.")));
    }

    private Set<Permission> getPermissions(List<Long> permissionIds) {
        Set<Permission> permissions = new HashSet<>();
        if (permissionIds == null || permissionIds.isEmpty()) {
            return permissions;
        }

        for (Long permissionId : permissionIds) {
            if (permissionId == null) {
                throw new BusinessException("Permissao nao encontrada.", HttpStatus.NOT_FOUND, List.of("Permissao nao encontrada."));
            }

            permissions.add(getPermission(permissionId));
        }

        return permissions;
    }

    private void syncOrganizationScopes(Role role, List<RoleOrganizationScopeRequest> requests) {
        roleOrganizationScopeRepository.deleteByRole_Id(role.getId());
        if (role.getType() != RoleType.ORGANIZATIONAL || requests == null || requests.isEmpty()) {
            return;
        }

        Set<Long> seenUnits = new HashSet<>();
        for (RoleOrganizationScopeRequest request : requests) {
            if (request == null || request.organizationUnitId() == null) {
                throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Unidade organizacional e obrigatoria."));
            }
            if (!seenUnits.add(request.organizationUnitId())) {
                throw new BusinessException("Escopo duplicado.", HttpStatus.CONFLICT, List.of("Nao repita a mesma unidade organizacional no perfil."));
            }

            OrganizationUnit unit = organizationUnitService.getUnit(request.organizationUnitId());
            roleOrganizationScopeRepository.save(new RoleOrganizationScope(role, unit, resolveScopeLevel(request.scopeLevel())));
        }
    }

    private RoleType resolveRoleType(String type, boolean systemRole) {
        if (systemRole) {
            return RoleType.SYSTEM;
        }
        if (isBlank(type)) {
            return RoleType.FUNCTIONAL;
        }
        try {
            return RoleType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Tipo de perfil invalido.", HttpStatus.BAD_REQUEST, List.of("Use FUNCTIONAL ou ORGANIZATIONAL."));
        }
    }

    private OrganizationScopeLevel resolveScopeLevel(String scopeLevel) {
        if (isBlank(scopeLevel)) {
            return OrganizationScopeLevel.VIEW;
        }
        try {
            return OrganizationScopeLevel.valueOf(scopeLevel.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Nivel de escopo invalido.", HttpStatus.BAD_REQUEST, List.of("Use VIEW, EDIT ou ADMIN."));
        }
    }

    private void ensureFunctionalRole(Role role) {
        if (role.getType() == RoleType.ORGANIZATIONAL) {
            throw new BusinessException("Operacao invalida.", HttpStatus.BAD_REQUEST, List.of("Perfil organizacional nao recebe permissoes funcionais."));
        }
    }

    private void ensureOrganizationalRole(Role role) {
        if (role.getType() != RoleType.ORGANIZATIONAL) {
            throw new BusinessException("Operacao invalida.", HttpStatus.BAD_REQUEST, List.of("Apenas perfil organizacional recebe escopos."));
        }
    }

    private Permission getPermission(Long permissionId) {
        if (permissionId == null) {
            throw new BusinessException("Permissao nao encontrada.", HttpStatus.NOT_FOUND, List.of("Permissao nao encontrada."));
        }

        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BusinessException("Permissao nao encontrada.", HttpStatus.NOT_FOUND, List.of("Permissao nao encontrada.")));
    }

    private Usuario getUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new BusinessException("Usuario nao encontrado.", HttpStatus.NOT_FOUND, List.of("Usuario nao encontrado."));
        }

        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado.", HttpStatus.NOT_FOUND, List.of("Usuario nao encontrado.")));
    }

    private RoleResponse toResponse(Role role) {
        List<RoleResponse.PermissionResponse> permissions = role.getPermissions()
                .stream()
                .sorted((first, second) -> first.getName().compareToIgnoreCase(second.getName()))
                .map(permission -> new RoleResponse.PermissionResponse(
                        permission.getId(),
                        permission.getName(),
                        permission.getDescription()
                ))
                .toList();
        List<RoleOrganizationScopeResponse> organizationScopes = roleOrganizationScopeRepository
                .findByRole_IdOrderByOrganizationUnit_Type_CodeAscOrganizationUnit_CodeAsc(role.getId())
                .stream()
                .map(this::toOrganizationScopeResponse)
                .toList();

        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getType().name(),
                role.isAtivo(),
                role.isSistema(),
                role.getCriadoEm(),
                role.getAtualizadoEm(),
                permissions,
                organizationScopes
        );
    }

    private RoleOrganizationScopeResponse toOrganizationScopeResponse(RoleOrganizationScope scope) {
        OrganizationUnit unit = scope.getOrganizationUnit();
        return new RoleOrganizationScopeResponse(
                scope.getId(),
                unit.getId(),
                unit.getCode(),
                unit.getName(),
                unit.getType().getId(),
                unit.getType().getCode(),
                unit.getType().getName(),
                scope.getScopeLevel().name()
        );
    }

    private UsuarioResponse toUsuarioResponse(Usuario usuario) {
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
                        .map(Role::getName)
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

    private RoleUserResponse toRoleUserResponse(Usuario usuario) {
        return new RoleUserResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getNomeExibicao(),
                usuario.getEmail(),
                usuario.isAtivo(),
                usuario.isBloqueado(),
                usuario.getAvatarUrl()
        );
    }

    private String normalizarNome(String name) {
        return name.trim().toUpperCase();
    }

    private String normalizarDescricao(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }

        return description.trim();
    }

    private String normalizeSearch(String search) {
        if (isBlank(search)) {
            return null;
        }

        return search.trim().toLowerCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Pageable resolvePageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, 10, Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id")));
        }

        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id"))
            );
        }

        return pageable;
    }

    private Pageable resolveUsuarioPageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, 10, Sort.by(Sort.Order.asc("nome"), Sort.Order.asc("id")));
        }

        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Order.asc("nome"), Sort.Order.asc("id"))
            );
        }

        return pageable;
    }

    private Specification<Role> matchesSearch(String search) {
        if (isBlank(search)) {
            return null;
        }

        String term = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), term),
                cb.like(cb.lower(root.get("description")), term)
        );
    }

    private Specification<Role> matchesBoolean(String field, Boolean value) {
        if (value == null) {
            return null;
        }

        return (root, query, cb) -> cb.equal(root.get(field), value);
    }

}
