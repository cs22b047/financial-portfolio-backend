package com.example.portfolioapp.entity;

/**
 * Types of corporate actions affecting positions.
 */
public enum CorporateActionType {
    STOCK_SPLIT("Stock Split", "Shares multiplied by split ratio"),
    REVERSE_SPLIT("Reverse Split", "Shares reduced by split ratio"),
    STOCK_DIVIDEND("Stock Dividend", "Additional shares issued as dividend"),
    SPIN_OFF("Spin-Off", "New company shares distributed"),
    MERGER("Merger", "Company merged with another"),
    ACQUISITION("Acquisition", "Company acquired"),
    RIGHTS_ISSUE("Rights Issue", "Right to purchase additional shares"),
    TENDER_OFFER("Tender Offer", "Offer to buy shares at premium"),
    DELISTING("Delisting", "Stock removed from exchange"),
    NAME_CHANGE("Name Change", "Company/ticker name changed"),
    CUSIP_CHANGE("CUSIP Change", "Security identifier changed"),
    BOND_CALL("Bond Call", "Bond called before maturity"),
    BOND_MATURITY("Bond Maturity", "Bond reached maturity date");

    private final String displayName;
    private final String description;

    CorporateActionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean affectsQuantity() {
        return this == STOCK_SPLIT || this == REVERSE_SPLIT ||
               this == STOCK_DIVIDEND || this == SPIN_OFF;
    }

    public boolean requiresManualAction() {
        return this == TENDER_OFFER || this == RIGHTS_ISSUE;
    }
}
