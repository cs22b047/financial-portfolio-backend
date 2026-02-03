package com.example.portfolioapp.entity;

/**
 * Risk levels for financial instruments.
 */
public enum RiskLevel {
    VERY_LOW("Very Low", 1),
    LOW("Low", 2),
    MEDIUM("Medium", 3),
    HIGH("High", 4),
    VERY_HIGH("Very High", 5);

    private final String displayName;
    private final int numericLevel;

    RiskLevel(String displayName, int numericLevel) {
        this.displayName = displayName;
        this.numericLevel = numericLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getNumericLevel() {
        return numericLevel;
    }
}
