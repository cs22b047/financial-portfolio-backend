package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.ChatMessage;
import com.example.portfolioapp.entity.Conversation;
import com.example.portfolioapp.entity.MessageRole;
import com.example.portfolioapp.entity.QueryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Find all messages in a conversation, ordered by creation date
     */
    List<ChatMessage> findByConversationOrderByCreatedDateAsc(Conversation conversation);

    /**
     * Find messages by conversation and role
     */
    List<ChatMessage> findByConversationAndRoleOrderByCreatedDateDesc(Conversation conversation, MessageRole role);

    /**
     * Find recent messages in a conversation (for context)
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation = :conversation ORDER BY m.createdDate DESC LIMIT :limit")
    List<ChatMessage> findRecentMessages(@Param("conversation") Conversation conversation, @Param("limit") int limit);

    /**
     * Find messages by query type
     */
    List<ChatMessage> findByConversationAndQueryType(Conversation conversation, QueryType queryType);

    /**
     * Count messages in a conversation
     */
    long countByConversation(Conversation conversation);

    /**
     * Count messages by conversation and role
     */
    long countByConversationAndRole(Conversation conversation, MessageRole role);
}
