package com.example.portfolioapp.entity;

/**
 * Credit rating agencies.
 */
public enum RatingAgency {
    SP("S&P Global Ratings"),
    MOODYS("Moody's Investors Service"),
    FITCH("Fitch Ratings"),
    DBRS("DBRS Morningstar"),
    KROLL("Kroll Bond Rating Agency");

    private final String fullName;

    RatingAgency(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }
}
