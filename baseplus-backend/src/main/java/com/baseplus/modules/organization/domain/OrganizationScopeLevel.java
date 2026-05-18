package com.baseplus.modules.organization.domain;

public enum OrganizationScopeLevel {
    VIEW(1),
    EDIT(2),
    ADMIN(3);

    private final int weight;

    OrganizationScopeLevel(int weight) {
        this.weight = weight;
    }

    public boolean includes(OrganizationScopeLevel required) {
        return required != null && weight >= required.weight;
    }
}

