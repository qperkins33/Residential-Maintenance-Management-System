package com.maintenance.enums;

public enum AccessLevel {
    BASIC("Basic Access"),
    MANAGER("Manager Access"),
    ADMIN("Admin Access"),
    SUPER_ADMIN("Super Admin Access");

    private final String description;

    AccessLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
