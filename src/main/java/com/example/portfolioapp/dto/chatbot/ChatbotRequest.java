package com.example.portfolioapp.dto.chatbot;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for calling Python chatbot service
 */
public class ChatbotRequest {
    private String session_id;
    private String message;
    private List<Map<String, String>> history;

    public ChatbotRequest() {}

    public ChatbotRequest(String sessionId, String message, List<Map<String, String>> history) {
        this.session_id = sessionId;
        this.message = message;
        this.history = history;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Map<String, String>> getHistory() {
        return history;
    }

    public void setHistory(List<Map<String, String>> history) {
        this.history = history;
    }
}
