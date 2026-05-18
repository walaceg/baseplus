package com.baseplus.modules.organization.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baseplus.core.exception.BusinessException;
import com.baseplus.modules.audit.service.AuditLogService;
import com.baseplus.modules.organization.domain.OrganizationUnitType;
import com.baseplus.modules.organization.dto.CreateOrganizationUnitTypeRequest;
import com.baseplus.modules.organization.dto.OrganizationUnitTypeResponse;
import com.baseplus.modules.organization.dto.UpdateOrganizationUnitTypeRequest;
import com.baseplus.modules.organization.repository.OrganizationUnitRepository;
import com.baseplus.modules.organization.repository.OrganizationUnitTypeRepository;
import com.baseplus.shared.dto.PageResponse;

@Service
public class OrganizationUnitTypeService {

    private final OrganizationUnitTypeRepository repository;
    private final OrganizationUnitRepository organizationUnitRepository;
    private final AuditLogService auditLogService;

    public OrganizationUnitTypeService(
            OrganizationUnitTypeRepository repository,
            OrganizationUnitRepository organizationUnitRepository,
            AuditLogService auditLogService
    ) {
        this.repository = repository;
        this.organizationUnitRepository = organizationUnitRepository;
        this.auditLogService = auditLogService;
    }

    public PageResponse<OrganizationUnitTypeResponse> listar(String search, Boolean active, Pageable pageable) {
        Pageable resolvedPageable = pageable == null
                ? PageRequest.of(0, 20, Sort.by("code").ascending())
                : pageable;
        Specification<OrganizationUnitType> specification = Specification.where(matchesSearch(search))
                .and(matchesActive(active));
        Page<OrganizationUnitType> page = repository.findAll(specification, resolvedPageable);
        return PageResponse.from(page, page.getContent().stream().map(this::toResponse).toList());
    }

    public OrganizationUnitTypeResponse buscar(Long id) {
        return toResponse(getType(id));
    }

    @Transactional
    public OrganizationUnitTypeResponse criar(CreateOrganizationUnitTypeRequest request) {
        if (request == null || isBlank(request.code()) || isBlank(request.name())) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Codigo e nome sao obrigatorios."));
        }

        String code = normalizeCode(request.code());
        if (repository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("Tipo ja cadastrado.", HttpStatus.CONFLICT, List.of("Ja existe um tipo organizacional com este codigo."));
        }

        OrganizationUnitType type = new OrganizationUnitType(code, request.name().trim());
        type.setActive(request.active() == null || request.active());
        OrganizationUnitTypeResponse response = toResponse(repository.save(type));
        auditLogService.registrarAutenticado("CREATE", "ORGANIZATION_UNIT_TYPE", response.id());
        return response;
    }

    @Transactional
    public OrganizationUnitTypeResponse atualizar(Long id, UpdateOrganizationUnitTypeRequest request) {
        if (request == null || isBlank(request.code()) || isBlank(request.name())) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Codigo e nome sao obrigatorios."));
        }

        OrganizationUnitType type = getType(id);
        String code = normalizeCode(request.code());
        if (repository.existsByCodeIgnoreCaseAndIdNot(code, type.getId())) {
            throw new BusinessException("Tipo ja cadastrado.", HttpStatus.CONFLICT, List.of("Ja existe um tipo organizacional com este codigo."));
        }

        type.setCode(code);
        type.setName(request.name().trim());
        if (request.active() != null) {
            type.setActive(request.active());
        }
        OrganizationUnitTypeResponse response = toResponse(repository.save(type));
        auditLogService.registrarAutenticado("UPDATE", "ORGANIZATION_UNIT_TYPE", response.id());
        return response;
    }

    @Transactional
    public void excluir(Long id) {
        OrganizationUnitType type = getType(id);
        if (organizationUnitRepository.existsByType_Id(type.getId())) {
            throw new BusinessException("Tipo em uso.", HttpStatus.CONFLICT, List.of("Nao e possivel excluir tipo com unidades organizacionais vinculadas."));
        }

        repository.delete(type);
        auditLogService.registrarAutenticado("DELETE", "ORGANIZATION_UNIT_TYPE", id);
    }

    public OrganizationUnitType getType(Long id) {
        if (id == null) {
            throw new BusinessException("Tipo nao encontrado.", HttpStatus.NOT_FOUND, List.of("Tipo organizacional nao encontrado."));
        }
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Tipo nao encontrado.", HttpStatus.NOT_FOUND, List.of("Tipo organizacional nao encontrado.")));
    }

    private OrganizationUnitTypeResponse toResponse(OrganizationUnitType type) {
        return new OrganizationUnitTypeResponse(type.getId(), type.getCode(), type.getName(), type.isActive(), type.getCreatedAt(), type.getUpdatedAt());
    }

    private Specification<OrganizationUnitType> matchesSearch(String search) {
        String normalized = normalizeSearch(search);
        if (normalized == null) {
            return null;
        }
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("code")), "%" + normalized + "%"),
                builder.like(builder.lower(root.get("name")), "%" + normalized + "%")
        );
    }

    private Specification<OrganizationUnitType> matchesActive(Boolean active) {
        if (active == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get("active"), active);
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizeSearch(String value) {
        return isBlank(value) ? null : value.trim().toLowerCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
