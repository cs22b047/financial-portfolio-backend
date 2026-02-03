package com.example.portfolioapp.entity;

/**
 * Transaction type enum - matches database ENUM
 */
public enum TransactionType {
    BUY,         // Purchase of asset
    SELL,        // Sale of asset
    DIVIDEND,    // Dividend payment received
    DEPOSIT,     // Cash deposit
    WITHDRAWAL,  // Cash withdrawal
    TRANSFER     // Transfer between accounts
}
