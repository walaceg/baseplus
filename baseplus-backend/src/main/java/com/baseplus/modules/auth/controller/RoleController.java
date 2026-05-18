package com.baseplus.modules.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baseplus.modules.auth.dto.CreateRoleRequest;
import com.baseplus.modules.auth.dto.RoleUserResponse;
import com.baseplus.modules.auth.dto.RoleResponse;
import com.baseplus.modules.auth.dto.UpdateRoleRequest;
import com.baseplus.modules.auth.dto.UpdateRoleStatusRequest;
import com.baseplus.modules.auth.service.RoleService;
import com.baseplus.modules.organization.dto.RoleOrganizationScopeRequest;
import com.baseplus.modules.usuario.dto.UsuarioResponse;
import com.baseplus.shared.dto.ApiResponse;
import com.baseplus.shared.dto.PageResponse;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("@authorizationService.hasPermission('ROLES_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) Boolean sistema,
            @PageableDefault(size = 10, sort = {"name", "id"}, direction = Sort.Direction.ASC) Pageable pageable
    ) {
        PageResponse<RoleResponse> response = roleService.listar(search, ativo, sistema, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Roles carregadas."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authorizationService.hasPermission('ROLES_VIEW')")
    public ResponseEntity<ApiResponse<RoleResponse>> buscar(@PathVariable Long id) {
        RoleResponse response = roleService.buscar(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Role carregada."));
    }

    @GetMapping("/{id}/usuarios")
    @PreAuthorize("@authorizationService.hasPermission('ROLES_MANAGE_USERS')")
    public ResponseEntity<ApiResponse<PageResponse<RoleUserResponse>>> listarUsuarios(
            @PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean vinculado,
            @PageableDefault(size = 10, sort = {"nome", "id"}, direction = Sort.Direction.ASC) Pageable pageable
    ) {
        PageResponse<RoleUserResponse> response = roleService.listarUsuarios(id, search, vinculado, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Usuarios do perfil carregados."));
    }

    @PostMapping
    @PreAuthorize("@authorizationService.hasPermission('ROLES_CREATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> criar(@RequestBody CreateRoleRequest request) {
        RoleResponse response = roleService.criar(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Role criada com sucesso."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authorizationService.hasPermission('ROLES_EDIT')")
    public ResponseEntity<ApiResponse<RoleResponse>> atualizar(
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest request
    ) {
        RoleResponse response = roleService.atualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Role atualizada com sucesso."));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@authorizationService.hasPermission('ROLES_EDIT')")
    public ResponseEntity<ApiResponse<RoleResponse>> atualizarStatus(
            @PathVariable Long id,
            @RequestBody UpdateRoleStatusRequest request
    ) {
        RoleResponse response = roleService.atualizarStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, request.ativo() ? "Role ativada com sucesso." : "Role desativada com sucesso."));
    }

    @PostMapping("/{id}/permissions/{permissionId}")
    @PreAuthorize("@authorizationService.hasPermission('ROLES_MANAGE_PERMISSIONS')")
    public ResponseEntity<ApiResponse<RoleResponse>> associarPermissao(
            @PathVariable Long id,
            @PathVariable Long permissionId
    ) {
        RoleResponse response = roleService.associarPermissao(id, permissionId);
        return ResponseEntity.ok(ApiResponse.success(response, "Permissao associada com sucesso."));
    }

    @DeleteMapping("/{id}/permissions/{permissionId}")
    @PreAuthorize("@authorizationService.hasPermission('ROLES_MANAGE_PERMISSIONS')")
    public ResponseEntity<ApiResponse<RoleResponse>> removerPermissao(
            @PathVariable Long id,
            @PathVariable Long permissionId
    ) {
        RoleResponse response = roleService.removerPermissao(id, permissionId);
        return ResponseEntity.ok(ApiResponse.success(response, "Permissao removida com sucesso."));
    }

    @PostMapping("/{id}/organization-scopes")
    @PreAuthorize("@authorizationService.hasPermission('ROLES_MANAGE_ORGANIZATION_SCOPES')")
    public ResponseEntity<ApiResponse<RoleResponse>> associarEscopoOrganizacional(
            @PathVariable Long id,
            @RequestBody RoleOrganizationScopeRequest request
    ) {
        RoleResponse response = roleService.associarEscopoOrganizacional(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Escopo organizacional associado com sucesso."));
    }

    @DeleteMapping("/{id}/organization-scopes/{scopeId}")
    @PreAuthorize("@authorizationService.hasPermission('ROLES_MANAGE_ORGANIZATION_SCOPES')")
    public ResponseEntity<ApiResponse<RoleResponse>> removerEscopoOrganizacional(
            @PathVariable Long id,
            @PathVariable Long scopeId
    ) {
        RoleResponse response = roleService.removerEscopoOrganizacional(id, scopeId);
        return ResponseEntity.ok(ApiResponse.success(response, "Escopo organizacional removido com sucesso."));
    }

    @PostMapping("/{id}/usuarios/{usuarioId}")
    @PreAuthorize("@authorizationService.hasPermission('ROLES_MANAGE_USERS')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> associarUsuario(
            @PathVariable Long id,
            @PathVariable Long usuarioId
    ) {
        UsuarioResponse response = roleService.associarUsuario(id, usuarioId);
        return ResponseEntity.ok(ApiResponse.success(response, "Usuario vinculado ao perfil com sucesso."));
    }

    @DeleteMapping("/{id}/usuarios/{usuarioId}")
    @PreAuthorize("@authorizationService.hasPermission('ROLES_MANAGE_USERS')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> removerUsuario(
            @PathVariable Long id,
            @PathVariable Long usuarioId
    ) {
        UsuarioResponse response = roleService.removerUsuario(id, usuarioId);
        return ResponseEntity.ok(ApiResponse.success(response, "Usuario removido do perfil com sucesso."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authorizationService.hasPermission('ROLES_DELETE')")
    public ResponseEntity<ApiResponse<Void>> remover(@PathVariable Long id) {
        roleService.remover(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Role removida com sucesso."));
    }
}
