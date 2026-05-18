package com.baseplus.modules.organization.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baseplus.modules.organization.dto.CreateOrganizationUnitRequest;
import com.baseplus.modules.organization.dto.OrganizationUnitResponse;
import com.baseplus.modules.organization.dto.UpdateOrganizationUnitRequest;
import com.baseplus.modules.organization.service.OrganizationUnitService;
import com.baseplus.shared.dto.ApiResponse;
import com.baseplus.shared.dto.PageResponse;

@RestController
@RequestMapping("/organization-units")
public class OrganizationUnitController {

    private final OrganizationUnitService service;

    public OrganizationUnitController(OrganizationUnitService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@authorizationService.hasPermission('ORGANIZATION_UNITS_VIEW') or @authorizationService.hasPermission('ROLES_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<OrganizationUnitResponse>>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = {"code", "id"}, direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.listar(search, typeId, active, pageable), "Unidades organizacionais carregadas."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authorizationService.hasPermission('ORGANIZATION_UNITS_VIEW') or @authorizationService.hasPermission('ROLES_VIEW')")
    public ResponseEntity<ApiResponse<OrganizationUnitResponse>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.buscar(id), "Unidade organizacional carregada."));
    }

    @PostMapping
    @PreAuthorize("@authorizationService.hasPermission('ORGANIZATION_UNITS_CREATE') or @authorizationService.hasPermission('ROLES_EDIT')")
    public ResponseEntity<ApiResponse<OrganizationUnitResponse>> criar(@RequestBody CreateOrganizationUnitRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.criar(request), "Unidade organizacional criada com sucesso."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authorizationService.hasPermission('ORGANIZATION_UNITS_EDIT') or @authorizationService.hasPermission('ROLES_EDIT')")
    public ResponseEntity<ApiResponse<OrganizationUnitResponse>> atualizar(
            @PathVariable Long id,
            @RequestBody UpdateOrganizationUnitRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.atualizar(id, request), "Unidade organizacional atualizada com sucesso."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authorizationService.hasPermission('ORGANIZATION_UNITS_DELETE') or @authorizationService.hasPermission('ROLES_DELETE')")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Unidade organizacional removida com sucesso."));
    }
}
