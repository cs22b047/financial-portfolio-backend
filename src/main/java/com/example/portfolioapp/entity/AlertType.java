package com.example.portfolioapp.entity;

/**
 * Types of price alerts.
 */
public enum AlertType {
    PRICE_ABOVE("Price Above", "Alert when price rises above target"),
    PRICE_BELOW("Price Below", "Alert when price falls below target"),
    PERCENT_CHANGE_UP("% Change Up", "Alert on percentage increase"),
    PERCENT_CHANGE_DOWN("% Change Down", "Alert on percentage decrease"),
    VOLUME_SPIKE("Volume Spike", "Alert on unusual trading volume"),
    TARGET_REACHED("Target Reached", "Alert when target price is reached");

    private final String displayName;
    private final String description;

    AlertType(String displayName, String description) {
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
