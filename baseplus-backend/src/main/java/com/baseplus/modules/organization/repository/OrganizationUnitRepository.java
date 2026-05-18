package com.baseplus.modules.organization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.baseplus.modules.organization.domain.OrganizationUnit;

public interface OrganizationUnitRepository extends JpaRepository<OrganizationUnit, Long>, JpaSpecificationExecutor<OrganizationUnit> {

    boolean existsByType_IdAndCodeIgnoreCase(Long typeId, String code);

    boolean existsByType_IdAndCodeIgnoreCaseAndIdNot(Long typeId, String code, Long id);

    boolean existsByParent_Id(Long parentId);

    boolean existsByType_Id(Long typeId);
}
