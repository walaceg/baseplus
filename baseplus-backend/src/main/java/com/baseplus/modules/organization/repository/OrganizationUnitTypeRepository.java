package com.baseplus.modules.organization.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.baseplus.modules.organization.domain.OrganizationUnitType;

public interface OrganizationUnitTypeRepository extends JpaRepository<OrganizationUnitType, Long>, JpaSpecificationExecutor<OrganizationUnitType> {

    Optional<OrganizationUnitType> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}

