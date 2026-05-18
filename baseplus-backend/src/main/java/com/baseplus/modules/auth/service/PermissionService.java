package com.baseplus.modules.auth.service;

import java.util.List;

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
import com.baseplus.modules.auth.dto.CreatePermissionRequest;
import com.baseplus.modules.auth.dto.PermissionResponse;
import com.baseplus.modules.auth.dto.UpdatePermissionRequest;
import com.baseplus.modules.auth.repository.PermissionRepository;
import com.baseplus.modules.auth.repository.RoleRepository;
import com.baseplus.modules.audit.service.AuditLogService;
import com.baseplus.shared.dto.PageResponse;

@Service
public class PermissionService {

    private static final String ADMIN_PERMISSION = "ADMIN_ACCESS";

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService;

    public PermissionService(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            AuditLogService auditLogService
    ) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.auditLogService = auditLogService;
    }

    public PageResponse<PermissionResponse> listar(String search, Pageable pageable) {
        Pageable resolvedPageable = resolvePageable(pageable);
        Specification<Permission> specification = matchesSearch(search);

        Page<Permission> page = permissionRepository.findAll(specification, resolvedPageable);
        List<PermissionResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.from(page, content);
    }

    public PermissionResponse buscar(Long id) {
        return toResponse(getPermission(id));
    }

    @Transactional
    public PermissionResponse criar(CreatePermissionRequest request) {
        if (request == null || isBlank(request.name())) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Nome da permission e obrigatorio."));
        }

        String name = normalizarNome(request.name());
        if (permissionRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Permission ja cadastrada.", HttpStatus.CONFLICT, List.of("Ja existe uma permission com este nome."));
        }

        Permission permission = new Permission(name, normalizarDescricao(request.description()));
        PermissionResponse response = toResponse(permissionRepository.save(permission));
        auditLogService.registrarAutenticado("CREATE", "PERMISSION", response.id());
        return response;
    }

    @Transactional
    public PermissionResponse atualizar(Long id, UpdatePermissionRequest request) {
        if (request == null || isBlank(request.name())) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Nome da permission e obrigatorio."));
        }

        Permission permission = getPermission(id);
        String name = normalizarNome(request.name());
        permissionRepository.findByNameIgnoreCase(name)
                .filter(outraPermission -> !outraPermission.getId().equals(permission.getId()))
                .ifPresent(outraPermission -> {
                    throw new BusinessException("Permission ja cadastrada.", HttpStatus.CONFLICT, List.of("Ja existe uma permission com este nome."));
                });

        permission.setName(name);
        permission.setDescription(normalizarDescricao(request.description()));

        PermissionResponse response = toResponse(permissionRepository.save(permission));
        auditLogService.registrarAutenticado("UPDATE", "PERMISSION", response.id());
        return response;
    }

    @Transactional
    public void remover(Long id) {
        Permission permission = getPermission(id);
        if (isProtectedPermission(permission)) {
            throw new BusinessException("Operacao invalida.", HttpStatus.BAD_REQUEST, List.of("Nao e permitido remover a permission ADMIN_ACCESS."));
        }

        for (Role role : roleRepository.findAll()) {
            if (role.getPermissions().remove(permission)) {
                roleRepository.save(role);
            }
        }

        permissionRepository.delete(permission);
        auditLogService.registrarAutenticado("DELETE", "PERMISSION", permission.getId());
    }

    private Permission getPermission(Long id) {
        if (id == null) {
            throw new BusinessException("Permission nao encontrada.", HttpStatus.NOT_FOUND, List.of("Permission nao encontrada."));
        }

        return permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Permission nao encontrada.", HttpStatus.NOT_FOUND, List.of("Permission nao encontrada.")));
    }

    private PermissionResponse toResponse(Permission permission) {
        return new PermissionResponse(permission.getId(), permission.getName(), permission.getDescription());
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isProtectedPermission(Permission permission) {
        return permission != null && ADMIN_PERMISSION.equalsIgnoreCase(permission.getName());
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

    private Specification<Permission> matchesSearch(String search) {
        if (isBlank(search)) {
            return null;
        }

        String term = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), term),
                cb.like(cb.lower(root.get("description")), term)
        );
    }
}
