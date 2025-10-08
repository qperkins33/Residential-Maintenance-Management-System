package com.maintenance.enums;

public enum LeaseStatus {
    OCCUPIED("Occupied", "#4CAF50", "🏠"),
    VACANT("Vacant", "#9E9E9E", "🚪"),
    MAINTENANCE("Under Maintenance", "#FF9800", "🔧"),
    AVAILABLE("Available", "#2196F3", "✨");

    private final String displayName;
    private final String color;
    private final String icon;

    LeaseStatus(String displayName, String color, String icon) {
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isAvailableForLease() {
        return this == AVAILABLE || this == VACANT;
    }

    public boolean needsMaintenance() {
        return this == MAINTENANCE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
