package com.example.portfolioapp.entity;

public enum AccountType {
    SAVINGS("Savings Account"),
    CHECKING("Checking Account"),
    MONEY_MARKET("Money Market Account"),
    CD("Certificate of Deposit"),
    HIGH_YIELD_SAVINGS("High Yield Savings");
    
    private final String displayName;
    
    AccountType(String displayName) {
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