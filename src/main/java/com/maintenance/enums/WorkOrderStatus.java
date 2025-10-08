package com.maintenance.enums;

public enum WorkOrderStatus {
    CREATED("Created", "#2196F3"),
    SCHEDULED("Scheduled", "#00BCD4"),
    IN_PROGRESS("In Progress", "#FF9800"),
    COMPLETED("Completed", "#4CAF50"),
    CANCELLED("Cancelled", "#F44336");

    private final String displayName;
    private final String color;

    WorkOrderStatus(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public boolean isActive() {
        return this == CREATED || this == SCHEDULED || this == IN_PROGRESS;
    }

    public boolean isComplete() {
        return this == COMPLETED;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
