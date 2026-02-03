package com.example.portfolioapp.entity;

/**
 * Bond rating categories (investment grade vs speculative).
 */
public enum RatingCategory {
    INVESTMENT_GRADE("Investment Grade", "BBB- / Baa3 and above"),
    SPECULATIVE("Speculative Grade", "BB+ / Ba1 and below"),
    DEFAULT("Default", "D rating");

    private final String displayName;
    private final String description;

    RatingCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
