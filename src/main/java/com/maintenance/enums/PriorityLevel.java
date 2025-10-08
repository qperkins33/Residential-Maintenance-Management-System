package com.maintenance.enums;

public enum PriorityLevel {
    LOW("Low", "#4CAF50", 1),
    MEDIUM("Medium", "#FFC107", 2),
    HIGH("High", "#FF9800", 3),
    URGENT("Urgent", "#FF5722", 4),
    EMERGENCY("Emergency", "#F44336", 5);

    private final String displayName;
    private final String color;
    private final int level;

    PriorityLevel(String displayName, String color, int level) {
        this.displayName = displayName;
        this.color = color;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
