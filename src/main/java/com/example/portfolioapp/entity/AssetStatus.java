package com.example.portfolioapp.entity;

/**
 * Status enum for assets - matches database ENUM
 */
public enum AssetStatus {
    WATCHLIST,  // User is watching but not owning
    OWNED,      // User owns this asset
    RESEARCH,   // Under research/analysis
    SOLD        // Previously owned, now sold
}
