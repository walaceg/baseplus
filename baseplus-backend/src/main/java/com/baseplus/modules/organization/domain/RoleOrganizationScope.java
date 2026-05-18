package com.baseplus.modules.organization.domain;

import com.baseplus.modules.auth.domain.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "role_organization_scopes",
        uniqueConstraints = @UniqueConstraint(name = "uk_role_organization_scope_unit", columnNames = {"role_id", "organization_unit_id"})
)
public class RoleOrganizationScope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "organization_unit_id", nullable = false)
    private OrganizationUnit organizationUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrganizationScopeLevel scopeLevel;

    protected RoleOrganizationScope() {
    }

    public RoleOrganizationScope(Role role, OrganizationUnit organizationUnit, OrganizationScopeLevel scopeLevel) {
        this.role = role;
        this.organizationUnit = organizationUnit;
        this.scopeLevel = scopeLevel;
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public OrganizationUnit getOrganizationUnit() {
        return organizationUnit;
    }

    public void setOrganizationUnit(OrganizationUnit organizationUnit) {
        this.organizationUnit = organizationUnit;
    }

    public OrganizationScopeLevel getScopeLevel() {
        return scopeLevel;
    }

    public void setScopeLevel(OrganizationScopeLevel scopeLevel) {
        this.scopeLevel = scopeLevel;
    }
}

