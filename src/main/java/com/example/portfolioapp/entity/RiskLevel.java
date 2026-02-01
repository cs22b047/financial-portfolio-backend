package com.example.portfolioapp.entity;

public enum RiskLevel {
    LOW("Low Risk"),
    MEDIUM("Medium Risk"),
    HIGH("High Risk"),
    VERY_HIGH("Very High Risk");
    
    private final String displayName;
    
    RiskLevel(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}