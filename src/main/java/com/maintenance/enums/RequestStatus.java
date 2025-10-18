package com.maintenance.enums;

public enum RequestStatus {
    SUBMITTED("Submitted", "#2196F3", "📋"),
    ACKNOWLEDGED("Acknowledged", "#03A9F4", "👀"),
    ASSIGNED("Assigned", "#00BCD4", "👷"),
    IN_PROGRESS("In Progress", "#FF9800", "⚙️"),
    REOPENED("Reopened", "#FFB74D", "♻️"),
    ON_HOLD("On Hold", "#9E9E9E", "⏸️"),
    COMPLETED("Completed", "#4CAF50", "✅"),
    CLOSED("Closed", "#607D8B", "🔒"),
    CANCELLED("Cancelled", "#F44336", "❌");

    private final String displayName;
    private final String color;
    private final String icon;

    RequestStatus(String displayName, String color, String icon) {
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

    public boolean isActive() {
        return this == SUBMITTED || this == ACKNOWLEDGED ||
                this == ASSIGNED || this == IN_PROGRESS;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CLOSED || this == CANCELLED;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
