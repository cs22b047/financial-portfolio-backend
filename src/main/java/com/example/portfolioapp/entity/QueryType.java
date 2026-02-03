package com.example.portfolioapp.entity;

/**
 * Type of query being processed
 */
public enum QueryType {
    /**
     * Direct data query (e.g., "What's AAPL's price?")
     * Involves SQL generation and data fetching
     */
    DATA,

    /**
     * Explanation query (e.g., "Why did AAPL drop?")
     * Involves data analysis and LLM-generated explanation
     */
    EXPLANATION,

    /**
     * General conversation (e.g., "Hello", "Thank you")
     * Casual chat without specific data requests
     */
    GENERAL,

    /**
     * Error occurred during processing
     */
    ERROR
}
