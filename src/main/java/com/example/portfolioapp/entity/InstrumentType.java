package com.example.portfolioapp.entity;

/**
 * Types of financial instruments.
 * Replaces the old AssetType entity with a cleaner enum.
 */
public enum InstrumentType {
    STOCK("Stock", "Equity shares in a company", RiskLevel.MEDIUM),
    ETF("ETF", "Exchange-Traded Fund", RiskLevel.MEDIUM),
    MUTUAL_FUND("Mutual Fund", "Pooled investment fund", RiskLevel.MEDIUM),
    BOND("Bond", "Fixed income security", RiskLevel.LOW),
    TREASURY("Treasury", "Government debt security", RiskLevel.LOW),
    CRYPTO("Cryptocurrency", "Digital/virtual currency", RiskLevel.VERY_HIGH),
    COMMODITY("Commodity", "Physical goods (gold, oil)", RiskLevel.HIGH),
    FOREX("Forex", "Foreign currency pair", RiskLevel.HIGH),
    OPTION("Option", "Derivative contract", RiskLevel.VERY_HIGH),
    FUTURE("Future", "Futures contract", RiskLevel.VERY_HIGH);

    private final String displayName;
    private final String description;
    private final RiskLevel defaultRiskLevel;

    InstrumentType(String displayName, String description, RiskLevel defaultRiskLevel) {
        this.displayName = displayName;
        this.description = description;
        this.defaultRiskLevel = defaultRiskLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public RiskLevel getDefaultRiskLevel() {
        return defaultRiskLevel;
    }

    public boolean isEquity() {
        return this == STOCK || this == ETF || this == MUTUAL_FUND;
    }

    public boolean isFixedIncome() {
        return this == BOND || this == TREASURY;
    }

    public boolean isCrypto() {
        return this == CRYPTO;
    }

    public boolean isDerivative() {
        return this == OPTION || this == FUTURE;
    }
}
