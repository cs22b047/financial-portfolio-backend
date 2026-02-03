package com.example.portfolioapp.entity;

/**
 * Cost basis calculation methods for tax purposes.
 */
public enum CostBasisMethod {
    FIFO("First In, First Out", "Sell oldest shares first"),
    LIFO("Last In, First Out", "Sell newest shares first"),
    HIFO("Highest In, First Out", "Sell highest cost shares first"),
    LIFO_BY_LOT("LIFO by Lot", "LIFO within specific lots"),
    SPECIFIC_ID("Specific Identification", "Manually select which shares to sell"),
    AVERAGE_COST("Average Cost", "Use average cost of all shares");

    private final String displayName;
    private final String description;

    CostBasisMethod(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresLotTracking() {
        return this != AVERAGE_COST;
    }
}
