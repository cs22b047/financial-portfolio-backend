package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.Conversation;
import com.example.portfolioapp.entity.ConversationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Find conversation by session ID
     */
    Optional<Conversation> findBySessionId(String sessionId);

    /**
     * Find conversations by user ID and status
     */
    List<Conversation> findByUserIdAndStatus(String userId, ConversationStatus status);

    /**
     * Find all conversations for a user, ordered by most recent message
     */
    List<Conversation> findByUserIdOrderByLastMessageAtDesc(String userId);

    /**
     * Find active conversations for a user
     */
    @Query("SELECT c FROM Conversation c WHERE c.userId = :userId AND c.status = 'ACTIVE' ORDER BY c.lastMessageAt DESC")
    List<Conversation> findActiveConversationsByUser(@Param("userId") String userId);

    /**
     * Check if session ID exists
     */
    boolean existsBySessionId(String sessionId);

    /**
     * Count conversations by user
     */
    long countByUserId(String userId);
}
