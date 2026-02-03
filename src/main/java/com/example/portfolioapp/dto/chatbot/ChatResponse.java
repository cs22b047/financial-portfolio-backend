package com.example.portfolioapp.dto.chatbot;

import com.example.portfolioapp.entity.QueryType;
import java.time.LocalDateTime;

/**
 * Response DTO for chat message from backend to frontend
 */
public class ChatResponse {
    private String response;
    private QueryType queryType;
    private String sqlQuery;
    private Object data;
    private Integer processingTimeMs;
    private LocalDateTime timestamp;

    public ChatResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatResponse(String response, QueryType queryType, String sqlQuery, Object data, Integer processingTimeMs) {
        this.response = response;
        this.queryType = queryType;
        this.sqlQuery = sqlQuery;
        this.data = data;
        this.processingTimeMs = processingTimeMs;
        this.timestamp = LocalDateTime.now();
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Integer getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Integer processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
