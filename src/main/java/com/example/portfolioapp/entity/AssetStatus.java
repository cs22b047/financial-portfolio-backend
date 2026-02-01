package com.example.portfolioapp.entity;

public enum AssetStatus {
    OWNED("Owned"),
    WATCHLIST("Watchlist"), 
    SOLD("Sold"),
    PENDING_PURCHASE("Pending Purchase"),
    RESEARCH("Research");
    
    private final String displayName;
    
    AssetStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isOwned() {
        return this == OWNED;
    }
    
    public boolean isActive() {
        return this == OWNED || this == WATCHLIST || this == PENDING_PURCHASE || this == RESEARCH;
    }
    
    public boolean requiresQuantity() {
        return this == OWNED;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}