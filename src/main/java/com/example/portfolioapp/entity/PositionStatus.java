package com.example.portfolioapp.entity;

/**
 * Status of a portfolio position.
 * Cleaner replacement for AssetStatus.
 */
public enum PositionStatus {
    OPEN("Open", "Currently held position"),
    CLOSED("Closed", "Position fully sold"),
    WATCHLIST("Watchlist", "Tracking for potential purchase"),
    RESEARCH("Research", "Under analysis"),
    PENDING_BUY("Pending Buy", "Order placed, awaiting execution"),
    PENDING_SELL("Pending Sell", "Sell order placed, awaiting execution");

    private final String displayName;
    private final String description;

    PositionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == OPEN || this == PENDING_BUY || this == PENDING_SELL;
    }

    public boolean isWatching() {
        return this == WATCHLIST || this == RESEARCH;
    }

    public boolean isOwned() {
        return this == OPEN;
    }
}
