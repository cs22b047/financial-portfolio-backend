package com.example.portfolioapp.dto.chatbot;

import java.util.List;

/**
 * Response DTO from Python chatbot service
 */
public class ChatbotResponse {
    private String response;
    private String query_type;
    private String sql_query;
    private Object data;
    private List<String> tables_used;
    private Integer tokens_used;
    private Integer processing_time_ms;
    private String error;

    public ChatbotResponse() {}

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getQuery_type() {
        return query_type;
    }

    public void setQuery_type(String query_type) {
        this.query_type = query_type;
    }

    public String getSql_query() {
        return sql_query;
    }

    public void setSql_query(String sql_query) {
        this.sql_query = sql_query;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public List<String> getTables_used() {
        return tables_used;
    }

    public void setTables_used(List<String> tables_used) {
        this.tables_used = tables_used;
    }

    public Integer getTokens_used() {
        return tokens_used;
    }

    public void setTokens_used(Integer tokens_used) {
        this.tokens_used = tokens_used;
    }

    public Integer getProcessing_time_ms() {
        return processing_time_ms;
    }

    public void setProcessing_time_ms(Integer processing_time_ms) {
        this.processing_time_ms = processing_time_ms;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
