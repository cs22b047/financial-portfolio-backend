package com.example.portfolioapp.entity;

public enum TransactionType {
    // Common transaction types
    BUY("Buy"),
    SELL("Sell"),
    
    // Stock-specific transactions
    DIVIDEND("Dividend"),
    STOCK_SPLIT("Stock Split"),
    STOCK_DIVIDEND("Stock Dividend"),
    RIGHTS_ISSUE("Rights Issue"),
    SPIN_OFF("Spin-off"),
    
    // Bond-specific transactions
    INTEREST_PAYMENT("Interest Payment"),
    BOND_MATURITY("Bond Maturity"),
    CALL("Bond Call"),
    PUT("Bond Put"),
    
    // Cash-specific transactions
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    INTEREST_EARNED("Interest Earned"),
    TRANSFER_IN("Transfer In"),
    TRANSFER_OUT("Transfer Out"),
    FEE("Fee"),
    
    // Crypto-specific transactions
    STAKE("Stake"),
    UNSTAKE("Unstake"),
    STAKING_REWARD("Staking Reward"),
    MINING_REWARD("Mining Reward"),
    AIRDROP("Airdrop"),
    FORK("Fork"),
    SWAP("Swap"),
    
    // Other types
    ADJUSTMENT("Adjustment"),
    CORPORATE_ACTION("Corporate Action");
    
    private final String displayName;
    
    TransactionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    // Helper methods to categorize transaction types
    public boolean isIncomeTransaction() {
        return this == DIVIDEND || this == INTEREST_PAYMENT || this == INTEREST_EARNED ||
               this == STAKING_REWARD || this == MINING_REWARD || this == AIRDROP;
    }
    
    public boolean isPurchaseTransaction() {
        return this == BUY || this == DEPOSIT || this == TRANSFER_IN || this == STAKE;
    }
    
    public boolean isSaleTransaction() {
        return this == SELL || this == WITHDRAWAL || this == TRANSFER_OUT || this == UNSTAKE;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}