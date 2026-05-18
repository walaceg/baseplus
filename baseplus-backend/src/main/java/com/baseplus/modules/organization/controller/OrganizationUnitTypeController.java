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

import com.baseplus.modules.organization.dto.CreateOrganizationUnitTypeRequest;
import com.baseplus.modules.organization.dto.OrganizationUnitTypeResponse;
import com.baseplus.modules.organization.dto.UpdateOrganizationUnitTypeRequest;
import com.baseplus.modules.organization.service.OrganizationUnitTypeService;
import com.baseplus.shared.dto.ApiResponse;
import com.baseplus.shared.dto.PageResponse;

@RestController
@RequestMapping("/organization-unit-types")
public class OrganizationUnitTypeController {

    private final OrganizationUnitTypeService service;

    public OrganizationUnitTypeController(OrganizationUnitTypeService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@authorizationService.hasPermission('ORGANIZATION_UNITS_VIEW') or @authorizationService.hasPermission('ROLES_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<OrganizationUnitTypeResponse>>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = {"code", "id"}, direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.listar(search, active, pageable), "Tipos organizacionais carregados."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authorizationService.hasPermission('ORGANIZATION_UNITS_VIEW') or @authorizationService.hasPermission('ROLES_VIEW')")
    public ResponseEntity<ApiResponse<OrganizationUnitTypeResponse>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.buscar(id), "Tipo organizacional carregado."));
    }

    @PostMapping
    @PreAuthorize("@authorizationService.hasPermission('ORGANIZATION_UNITS_CREATE') or @authorizationService.hasPermission('ROLES_EDIT')")
    public ResponseEntity<ApiResponse<OrganizationUnitTypeResponse>> criar(@RequestBody CreateOrganizationUnitTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.criar(request), "Tipo organizacional criado com sucesso."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authorizationService.hasPermission('ORGANIZATION_UNITS_EDIT') or @authorizationService.hasPermission('ROLES_EDIT')")
    public ResponseEntity<ApiResponse<OrganizationUnitTypeResponse>> atualizar(
            @PathVariable Long id,
            @RequestBody UpdateOrganizationUnitTypeRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.atualizar(id, request), "Tipo organizacional atualizado com sucesso."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authorizationService.hasPermission('ORGANIZATION_UNITS_DELETE') or @authorizationService.hasPermission('ROLES_DELETE')")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Tipo organizacional removido com sucesso."));
    }
}
