-- =====================================================
-- Flyway Migration V5: Create Chatbot Tables
-- Description: Creates tables for chatbot functionality
-- IMPORTANT: This migration ONLY creates NEW tables.
--            It does NOT modify any existing tables.
-- =====================================================

-- Table: conversations
-- Stores chat conversation sessions
CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL UNIQUE,
    user_id VARCHAR(100) DEFAULT 'default_user',
    title VARCHAR(200),
    conversation_status ENUM('ACTIVE', 'ARCHIVED', 'DELETED') DEFAULT 'ACTIVE',
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    last_message_at DATETIME(6),

    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (conversation_status),
    INDEX idx_last_message (last_message_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
COMMENT='Chat conversation sessions';

-- Table: chat_messages
-- Stores individual messages within conversations
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    role ENUM('USER', 'ASSISTANT', 'SYSTEM') NOT NULL,
    content TEXT NOT NULL,
    query_type ENUM('DATA', 'EXPLANATION', 'GENERAL', 'ERROR') DEFAULT 'GENERAL',

    -- For data queries
    sql_query TEXT,
    sql_result JSON,
    tables_accessed JSON,

    -- Metadata
    tokens_used INT,
    processing_time_ms INT,
    model_used VARCHAR(100) DEFAULT 'meta-llama/llama-4-scout-17b-16e-instruct',
    error_message TEXT,

    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),

    INDEX idx_conversation (conversation_id),
    INDEX idx_role (role),
    INDEX idx_query_type (query_type),
    INDEX idx_created_date (created_date),

    CONSTRAINT fk_chat_messages_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES conversations(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
COMMENT='Individual chat messages within conversations';

-- Table: chat_context
-- Stores persistent context information for conversations
CREATE TABLE IF NOT EXISTS chat_context (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    context_key VARCHAR(100) NOT NULL,
    context_value TEXT,
    expires_at DATETIME(6),

    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    UNIQUE KEY uk_conversation_key (conversation_id, context_key),
    INDEX idx_conversation (conversation_id),
    INDEX idx_expires (expires_at),

    CONSTRAINT fk_chat_context_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES conversations(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
COMMENT='Persistent context information for conversations';
