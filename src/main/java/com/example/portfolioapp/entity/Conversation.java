package com.example.portfolioapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Conversation entity representing a chat session.
 * Each conversation has a unique session ID and contains multiple messages.
 */
@Entity
@Table(name = "conversations",
       uniqueConstraints = @UniqueConstraint(name = "uk_conversation_session", columnNames = "session_id"),
       indexes = {
           @Index(name = "idx_session_id", columnList = "session_id"),
           @Index(name = "idx_user_id", columnList = "user_id"),
           @Index(name = "idx_status", columnList = "conversation_status"),
           @Index(name = "idx_last_message", columnList = "last_message_at")
       })
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", unique = true, nullable = false, length = 100)
    private String sessionId;

    @Column(name = "user_id", length = 100)
    private String userId = "default_user";

    @Column(name = "title", length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_status", nullable = false)
    private ConversationStatus status = ConversationStatus.ACTIVE;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @JsonIgnore
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("createdDate ASC")
    private List<ChatMessage> messages = new ArrayList<>();

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // Constructors
    public Conversation() {}

    public Conversation(String sessionId) {
        this.sessionId = sessionId;
    }

    public Conversation(String sessionId, String userId) {
        this.sessionId = sessionId;
        this.userId = userId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ConversationStatus getStatus() {
        return status;
    }

    public void setStatus(ConversationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    // Helper methods
    public void addMessage(ChatMessage message) {
        messages.add(message);
        message.setConversation(this);
        this.lastMessageAt = LocalDateTime.now();
    }

    public void removeMessage(ChatMessage message) {
        messages.remove(message);
        message.setConversation(null);
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", sessionId='" + sessionId + '\'' +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", messageCount=" + messages.size() +
                '}';
    }
}
