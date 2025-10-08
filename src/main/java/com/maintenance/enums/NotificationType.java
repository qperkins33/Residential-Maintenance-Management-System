package com.maintenance.enums;

public enum NotificationType {
    REQUEST_SUBMITTED("Request Submitted", "📋", "#2196F3"),
    REQUEST_ASSIGNED("Request Assigned", "👷", "#00BCD4"),
    STATUS_UPDATE("Status Update", "🔄", "#FF9800"),
    WORK_SCHEDULED("Work Scheduled", "📅", "#9C27B0"),
    WORK_COMPLETED("Work Completed", "✅", "#4CAF50"),
    SYSTEM_ALERT("System Alert", "⚠️", "#FF5722"),
    REMINDER("Reminder", "🔔", "#FFC107");

    private final String displayName;
    private final String icon;
    private final String color;

    NotificationType(String displayName, String icon, String color) {
        this.displayName = displayName;
        this.icon = icon;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
