package com.maintenance.enums;

public enum ReportType {
    MAINTENANCE_SUMMARY("Maintenance Summary", "📊", "Overview of all maintenance activities"),
    COST_ANALYSIS("Cost Analysis", "💰", "Financial breakdown of maintenance costs"),
    STAFF_PERFORMANCE("Staff Performance", "👥", "Staff productivity and efficiency metrics"),
    TENANT_SATISFACTION("Tenant Satisfaction", "😊", "Tenant feedback and satisfaction ratings"),
    BUILDING_STATISTICS("Building Statistics", "🏢", "Building-wide maintenance statistics");

    private final String displayName;
    private final String icon;
    private final String description;

    ReportType(String displayName, String icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
