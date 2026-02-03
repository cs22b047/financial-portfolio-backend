package com.example.portfolioapp.entity;

/**
 * Types of exchanges/trading venues.
 */
public enum ExchangeType {
    STOCK("Stock Exchange"),
    BOND("Bond Market"),
    CRYPTO("Cryptocurrency Exchange"),
    FOREX("Foreign Exchange"),
    COMMODITY("Commodity Exchange"),
    DERIVATIVES("Derivatives Exchange"),
    OTC("Over-the-Counter");

    private final String displayName;

    ExchangeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
