package com.example.portfolioapp.entity;

public enum DividendType {
    // Stock dividend types
    CASH_DIVIDEND("Cash Dividend"),
    STOCK_DIVIDEND("Stock Dividend"),
    SPECIAL_DIVIDEND("Special Dividend"),
    RETURN_OF_CAPITAL("Return of Capital"),
    
    // Bond income types
    COUPON_PAYMENT("Coupon Payment"),
    INTEREST_PAYMENT("Interest Payment"),
    MATURITY_PAYMENT("Maturity Payment"),
    
    // Cash income types
    SAVINGS_INTEREST("Savings Interest"),
    CD_INTEREST("CD Interest"),
    MONEY_MARKET_INTEREST("Money Market Interest"),
    
    // Crypto reward types
    STAKING_REWARD("Staking Reward"),
    MINING_REWARD("Mining Reward"),
    LIQUIDITY_MINING("Liquidity Mining"),
    AIRDROP("Airdrop"),
    GOVERNANCE_REWARD("Governance Reward"),
    
    // Other income types
    RENTAL_INCOME("Rental Income"),
    OTHER_INCOME("Other Income");
    
    private final String displayName;
    
    DividendType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    // Helper methods to categorize dividend types by asset type
    public boolean isStockRelated() {
        return this == CASH_DIVIDEND || this == STOCK_DIVIDEND || 
               this == SPECIAL_DIVIDEND || this == RETURN_OF_CAPITAL;
    }
    
    public boolean isBondRelated() {
        return this == COUPON_PAYMENT || this == INTEREST_PAYMENT || 
               this == MATURITY_PAYMENT;
    }
    
    public boolean isCashRelated() {
        return this == SAVINGS_INTEREST || this == CD_INTEREST || 
               this == MONEY_MARKET_INTEREST;
    }
    
    public boolean isCryptoRelated() {
        return this == STAKING_REWARD || this == MINING_REWARD || 
               this == LIQUIDITY_MINING || this == AIRDROP || 
               this == GOVERNANCE_REWARD;
    }
    
    public boolean isTaxable() {
        // Most dividends are taxable, but some crypto rewards may not be
        return this != AIRDROP; // Airdrops may have different tax treatment
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}