package com.example.portfolioapp.entity;

/**
 * Role of the message sender
 */
public enum MessageRole {
    /**
     * Message from the user
     */
    USER,

    /**
     * Message from the chatbot assistant
     */
    ASSISTANT,

    /**
     * System message (e.g., conversation started, error occurred)
     */
    SYSTEM
}
