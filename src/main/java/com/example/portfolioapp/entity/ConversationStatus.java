package com.example.portfolioapp.entity;

/**
 * Status of a conversation
 */
public enum ConversationStatus {
    /**
     * Active conversation
     */
    ACTIVE,

    /**
     * Archived conversation (not deleted, but not actively used)
     */
    ARCHIVED,

    /**
     * Soft-deleted conversation
     */
    DELETED
}
