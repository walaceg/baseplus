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
import com.baseplus.modules.organization.domain.OrganizationUnit;
import com.baseplus.modules.organization.domain.OrganizationUnitType;
import com.baseplus.modules.organization.dto.CreateOrganizationUnitRequest;
import com.baseplus.modules.organization.dto.OrganizationUnitResponse;
import com.baseplus.modules.organization.dto.UpdateOrganizationUnitRequest;
import com.baseplus.modules.organization.repository.OrganizationUnitRepository;
import com.baseplus.modules.organization.repository.RoleOrganizationScopeRepository;
import com.baseplus.shared.dto.PageResponse;

@Service
public class OrganizationUnitService {

    private final OrganizationUnitRepository repository;
    private final RoleOrganizationScopeRepository roleOrganizationScopeRepository;
    private final OrganizationUnitTypeService typeService;
    private final AuditLogService auditLogService;

    public OrganizationUnitService(
            OrganizationUnitRepository repository,
            RoleOrganizationScopeRepository roleOrganizationScopeRepository,
            OrganizationUnitTypeService typeService,
            AuditLogService auditLogService
    ) {
        this.repository = repository;
        this.roleOrganizationScopeRepository = roleOrganizationScopeRepository;
        this.typeService = typeService;
        this.auditLogService = auditLogService;
    }

    public PageResponse<OrganizationUnitResponse> listar(String search, Long typeId, Boolean active, Pageable pageable) {
        Pageable resolvedPageable = pageable == null
                ? PageRequest.of(0, 20, Sort.by("code").ascending())
                : pageable;
        Specification<OrganizationUnit> specification = Specification.where(matchesSearch(search))
                .and(matchesType(typeId))
                .and(matchesActive(active));
        Page<OrganizationUnit> page = repository.findAll(specification, resolvedPageable);
        return PageResponse.from(page, page.getContent().stream().map(this::toResponse).toList());
    }

    public OrganizationUnitResponse buscar(Long id) {
        return toResponse(getUnit(id));
    }

    @Transactional
    public OrganizationUnitResponse criar(CreateOrganizationUnitRequest request) {
        if (request == null || request.typeId() == null || isBlank(request.code()) || isBlank(request.name())) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Tipo, codigo e nome sao obrigatorios."));
        }

        OrganizationUnitType type = typeService.getType(request.typeId());
        String code = normalizeCode(request.code());
        if (repository.existsByType_IdAndCodeIgnoreCase(type.getId(), code)) {
            throw new BusinessException("Unidade ja cadastrada.", HttpStatus.CONFLICT, List.of("Ja existe unidade com este tipo e codigo."));
        }

        OrganizationUnit unit = new OrganizationUnit(type, code, request.name().trim());
        unit.setParent(resolveParent(request.parentId(), null));
        unit.setActive(request.active() == null || request.active());
        OrganizationUnitResponse response = toResponse(repository.save(unit));
        auditLogService.registrarAutenticado("CREATE", "ORGANIZATION_UNIT", response.id());
        return response;
    }

    @Transactional
    public OrganizationUnitResponse atualizar(Long id, UpdateOrganizationUnitRequest request) {
        if (request == null || request.typeId() == null || isBlank(request.code()) || isBlank(request.name())) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Tipo, codigo e nome sao obrigatorios."));
        }

        OrganizationUnit unit = getUnit(id);
        OrganizationUnitType type = typeService.getType(request.typeId());
        String code = normalizeCode(request.code());
        if (repository.existsByType_IdAndCodeIgnoreCaseAndIdNot(type.getId(), code, unit.getId())) {
            throw new BusinessException("Unidade ja cadastrada.", HttpStatus.CONFLICT, List.of("Ja existe unidade com este tipo e codigo."));
        }

        unit.setType(type);
        unit.setCode(code);
        unit.setName(request.name().trim());
        unit.setParent(resolveParent(request.parentId(), unit.getId()));
        if (request.active() != null) {
            unit.setActive(request.active());
        }
        OrganizationUnitResponse response = toResponse(repository.save(unit));
        auditLogService.registrarAutenticado("UPDATE", "ORGANIZATION_UNIT", response.id());
        return response;
    }

    @Transactional
    public void excluir(Long id) {
        OrganizationUnit unit = getUnit(id);
        if (repository.existsByParent_Id(unit.getId())) {
            throw new BusinessException("Unidade em uso.", HttpStatus.CONFLICT, List.of("Nao e possivel excluir unidade com unidades filhas."));
        }
        if (roleOrganizationScopeRepository.existsByOrganizationUnit_Id(unit.getId())) {
            throw new BusinessException("Unidade em uso.", HttpStatus.CONFLICT, List.of("Nao e possivel excluir unidade vinculada a perfis organizacionais."));
        }

        repository.delete(unit);
        auditLogService.registrarAutenticado("DELETE", "ORGANIZATION_UNIT", id);
    }

    public OrganizationUnit getUnit(Long id) {
        if (id == null) {
            throw new BusinessException("Unidade nao encontrada.", HttpStatus.NOT_FOUND, List.of("Unidade organizacional nao encontrada."));
        }
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Unidade nao encontrada.", HttpStatus.NOT_FOUND, List.of("Unidade organizacional nao encontrada.")));
    }

    private OrganizationUnit resolveParent(Long parentId, Long currentId) {
        if (parentId == null) {
            return null;
        }
        if (currentId != null && currentId.equals(parentId)) {
            throw new BusinessException("Hierarquia invalida.", HttpStatus.BAD_REQUEST, List.of("A unidade nao pode ser pai dela mesma."));
        }
        return getUnit(parentId);
    }

    public OrganizationUnitResponse toResponse(OrganizationUnit unit) {
        OrganizationUnit parent = unit.getParent();
        return new OrganizationUnitResponse(
                unit.getId(),
                new OrganizationUnitResponse.OrganizationUnitTypeSummary(unit.getType().getId(), unit.getType().getCode(), unit.getType().getName()),
                unit.getCode(),
                unit.getName(),
                parent == null ? null : new OrganizationUnitResponse.OrganizationUnitSummary(parent.getId(), parent.getCode(), parent.getName()),
                unit.isActive(),
                unit.getCreatedAt(),
                unit.getUpdatedAt()
        );
    }

    private Specification<OrganizationUnit> matchesSearch(String search) {
        String normalized = normalizeSearch(search);
        if (normalized == null) {
            return null;
        }
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("code")), "%" + normalized + "%"),
                builder.like(builder.lower(root.get("name")), "%" + normalized + "%")
        );
    }

    private Specification<OrganizationUnit> matchesType(Long typeId) {
        if (typeId == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get("type").get("id"), typeId);
    }

    private Specification<OrganizationUnit> matchesActive(Boolean active) {
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
