package com.example.portfolioapp.entity;

/**
 * Enum representing the direction of a price alert.
 * Determines whether the alert triggers when price goes above or below the target.
 */
public enum AlertDirection {
    /**
     * Alert triggers when current price rises to or above the target price
     */
    ABOVE,
    
    /**
     * Alert triggers when current price falls to or below the target price
     */
    BELOW
}
