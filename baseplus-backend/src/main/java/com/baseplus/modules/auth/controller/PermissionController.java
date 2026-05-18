package com.baseplus.modules.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baseplus.modules.auth.dto.CreatePermissionRequest;
import com.baseplus.modules.auth.dto.PermissionResponse;
import com.baseplus.modules.auth.dto.UpdatePermissionRequest;
import com.baseplus.modules.auth.service.PermissionService;
import com.baseplus.shared.dto.ApiResponse;
import com.baseplus.shared.dto.PageResponse;

@RestController
@RequestMapping("/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @PreAuthorize("@authorizationService.hasPermission('PERMISSIONS_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> listar(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = {"name", "id"}, direction = Sort.Direction.ASC) Pageable pageable
    ) {
        PageResponse<PermissionResponse> response = permissionService.listar(search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Permissions carregadas."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authorizationService.hasPermission('PERMISSIONS_VIEW')")
    public ResponseEntity<ApiResponse<PermissionResponse>> buscar(@PathVariable Long id) {
        PermissionResponse response = permissionService.buscar(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Permission carregada."));
    }

    @PostMapping
    @PreAuthorize("@authorizationService.hasPermission('PERMISSIONS_CREATE')")
    public ResponseEntity<ApiResponse<PermissionResponse>> criar(@RequestBody CreatePermissionRequest request) {
        PermissionResponse response = permissionService.criar(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Permission criada com sucesso."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authorizationService.hasPermission('PERMISSIONS_EDIT')")
    public ResponseEntity<ApiResponse<PermissionResponse>> atualizar(
            @PathVariable Long id,
            @RequestBody UpdatePermissionRequest request
    ) {
        PermissionResponse response = permissionService.atualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Permission atualizada com sucesso."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authorizationService.hasPermission('PERMISSIONS_DELETE')")
    public ResponseEntity<ApiResponse<Void>> remover(@PathVariable Long id) {
        permissionService.remover(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Permission removida com sucesso."));
    }
}
