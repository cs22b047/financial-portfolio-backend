package com.example.portfolioapp.entity;

/**
 * Simplified transaction types.
 * Complex corporate actions moved to CorporateActionType.
 */
public enum SimpleTransactionType {
    BUY("Buy", "Purchase of securities", true),
    SELL("Sell", "Sale of securities", false),
    TRANSFER_IN("Transfer In", "Securities transferred into account", true),
    TRANSFER_OUT("Transfer Out", "Securities transferred out of account", false),
    DEPOSIT("Deposit", "Cash deposit", true),
    WITHDRAWAL("Withdrawal", "Cash withdrawal", false);

    private final String displayName;
    private final String description;
    private final boolean increasesPosition;

    SimpleTransactionType(String displayName, String description, boolean increasesPosition) {
        this.displayName = displayName;
        this.description = description;
        this.increasesPosition = increasesPosition;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean increasesPosition() {
        return increasesPosition;
    }

    public boolean isCashTransaction() {
        return this == DEPOSIT || this == WITHDRAWAL;
    }
}
