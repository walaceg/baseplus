package com.baseplus.modules.organization.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.baseplus.modules.organization.domain.RoleOrganizationScope;

public interface RoleOrganizationScopeRepository extends JpaRepository<RoleOrganizationScope, Long> {

    List<RoleOrganizationScope> findByRole_IdOrderByOrganizationUnit_Type_CodeAscOrganizationUnit_CodeAsc(Long roleId);

    Optional<RoleOrganizationScope> findByRole_IdAndOrganizationUnit_Id(Long roleId, Long organizationUnitId);

    void deleteByRole_Id(Long roleId);

    boolean existsByOrganizationUnit_Id(Long organizationUnitId);
}
