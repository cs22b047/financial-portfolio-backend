package com.example.portfolioapp.exception;

/**
 * Custom exception for chatbot-related errors
 */
public class ChatbotException extends RuntimeException {

    public ChatbotException(String message) {
        super(message);
    }

    public ChatbotException(String message, Throwable cause) {
        super(message, cause);
    }
}
