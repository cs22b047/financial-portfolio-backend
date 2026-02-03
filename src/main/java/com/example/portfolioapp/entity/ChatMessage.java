package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ChatMessage entity representing individual messages in a conversation.
 * Stores message content, metadata, and query processing information.
 */
@Entity
@Table(name = "chat_messages",
       indexes = {
           @Index(name = "idx_conversation", columnList = "conversation_id"),
           @Index(name = "idx_role", columnList = "role"),
           @Index(name = "idx_query_type", columnList = "query_type"),
           @Index(name = "idx_created_date", columnList = "created_date")
       })
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MessageRole role;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "query_type")
    private QueryType queryType;

    // For data queries
    @Column(name = "sql_query", columnDefinition = "TEXT")
    private String sqlQuery;

    @Column(name = "sql_result", columnDefinition = "JSON")
    private String sqlResult;

    @Column(name = "tables_accessed", columnDefinition = "JSON")
    private String tablesAccessed;

    // Metadata
    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    @Column(name = "model_used", length = 100)
    private String modelUsed = "meta-llama/llama-4-scout-17b-16e-instruct";

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }

    // Constructors
    public ChatMessage() {}

    public ChatMessage(Conversation conversation, MessageRole role, String content) {
        this.conversation = conversation;
        this.role = role;
        this.content = content;
    }

    public ChatMessage(Conversation conversation, MessageRole role, String content, QueryType queryType) {
        this.conversation = conversation;
        this.role = role;
        this.content = content;
        this.queryType = queryType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public MessageRole getRole() {
        return role;
    }

    public void setRole(MessageRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String getSqlResult() {
        return sqlResult;
    }

    public void setSqlResult(String sqlResult) {
        this.sqlResult = sqlResult;
    }

    public String getTablesAccessed() {
        return tablesAccessed;
    }

    public void setTablesAccessed(String tablesAccessed) {
        this.tablesAccessed = tablesAccessed;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public Integer getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Integer processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "id=" + id +
                ", role=" + role +
                ", queryType=" + queryType +
                ", content='" + (content != null && content.length() > 50 ? content.substring(0, 50) + "..." : content) + '\'' +
                ", tokensUsed=" + tokensUsed +
                ", processingTimeMs=" + processingTimeMs +
                '}';
    }
}
