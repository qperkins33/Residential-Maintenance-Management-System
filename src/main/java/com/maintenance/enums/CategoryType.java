package com.maintenance.enums;

public enum CategoryType {
    PLUMBING("Plumbing", "🚰"),
    ELECTRICAL("Electrical", "⚡"),
    HVAC("HVAC", "❄️"),
    APPLIANCE("Appliance", "🔧"),
    STRUCTURAL("Structural", "🏗️"),
    PEST_CONTROL("Pest Control", "🐛"),
    SAFETY_SECURITY("Safety & Security", "🔒"),
    CLEANING("Cleaning", "🧹"),
    GENERAL_MAINTENANCE("General Maintenance", "🔨"),
    EMERGENCY("Emergency", "🚨");

    private final String displayName;
    private final String icon;

    CategoryType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
