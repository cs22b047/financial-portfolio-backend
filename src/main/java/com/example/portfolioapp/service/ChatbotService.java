package com.example.portfolioapp.service;

import com.example.portfolioapp.dto.chatbot.*;
import com.example.portfolioapp.entity.*;
import com.example.portfolioapp.exception.ChatbotException;
import com.example.portfolioapp.exception.ResourceNotFoundException;
import com.example.portfolioapp.repository.ChatMessageRepository;
import com.example.portfolioapp.repository.ConversationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for chatbot operations
 * Orchestrates conversation management and Python chatbot service calls
 */
@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ChatMessageRepository messageRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${chatbot.python.url:http://localhost:5000}")
    private String pythonServiceUrl;

    @Value("${chatbot.history.max-messages:10}")
    private int maxHistoryMessages;

    /**
     * Send a message and get chatbot response
     */
    @Transactional
    public ChatResponse sendMessage(String sessionId, String userMessage) {
        logger.info("Processing message for session: {}", sessionId);

        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        try {
            // 1. Get or create conversation
            Conversation conversation = getOrCreateConversation(sessionId);

            // 2. Save user message
            ChatMessage userMsg = saveUserMessage(conversation, userMessage);

            // 3. Get conversation history for context
            List<ChatMessage> history = getRecentHistory(conversation, maxHistoryMessages);

            // 4. Call Python chatbot service
            long startTime = System.currentTimeMillis();
            ChatbotResponse pythonResponse = callPythonChatbot(sessionId, userMessage, history);
            long processingTime = System.currentTimeMillis() - startTime;

            // 5. Save assistant message
            ChatMessage assistantMsg = saveAssistantMessage(conversation, pythonResponse, processingTime);

            // 6. Update conversation metadata
            conversation.setLastMessageAt(LocalDateTime.now());
            if (conversation.getTitle() == null || conversation.getTitle().isEmpty()) {
                conversation.setTitle(generateConversationTitle(userMessage));
            }
            conversationRepository.save(conversation);

            // 7. Build and return response
            return buildChatResponse(pythonResponse, processingTime);

        } catch (ChatbotException e) {
            logger.error("Chatbot service error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error processing message", e);
            throw new ChatbotException("Failed to process message: " + e.getMessage(), e);
        }
    }

    /**
     * Create a new conversation
     */
    @Transactional
    public Conversation createConversation() {
        return createConversation("default_user");
    }

    /**
     * Create a new conversation for a specific user
     */
    @Transactional
    public Conversation createConversation(String userId) {
        String sessionId = UUID.randomUUID().toString();
        Conversation conversation = new Conversation(sessionId, userId);
        conversation.setStatus(ConversationStatus.ACTIVE);

        Conversation saved = conversationRepository.save(conversation);
        logger.info("Created new conversation with session ID: {}", sessionId);

        return saved;
    }

    /**
     * Get conversation by session ID
     */
    public Conversation getConversation(String sessionId) {
        return conversationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with session ID: " + sessionId));
    }

    /**
     * List conversations for a user
     */
    public List<Conversation> listConversations(String userId) {
        return conversationRepository.findByUserIdOrderByLastMessageAtDesc(userId);
    }

    /**
     * Delete conversation (soft delete)
     */
    @Transactional
    public void deleteConversation(String sessionId) {
        Conversation conversation = getConversation(sessionId);
        conversation.setStatus(ConversationStatus.DELETED);
        conversationRepository.save(conversation);
        logger.info("Deleted conversation: {}", sessionId);
    }

    /**
     * Get or create conversation
     */
    private Conversation getOrCreateConversation(String sessionId) {
        return conversationRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    Conversation newConv = new Conversation(sessionId);
                    newConv.setStatus(ConversationStatus.ACTIVE);
                    return conversationRepository.save(newConv);
                });
    }

    /**
     * Save user message to database
     */
    private ChatMessage saveUserMessage(Conversation conversation, String content) {
        ChatMessage message = new ChatMessage(conversation, MessageRole.USER, content);
        message.setQueryType(QueryType.GENERAL);
        return messageRepository.save(message);
    }

    /**
     * Save assistant message to database
     */
    private ChatMessage saveAssistantMessage(
            Conversation conversation,
            ChatbotResponse pythonResponse,
            long processingTime
    ) {
        ChatMessage message = new ChatMessage(
                conversation,
                MessageRole.ASSISTANT,
                pythonResponse.getResponse()
        );

        // Set query type
        QueryType queryType = parseQueryType(pythonResponse.getQuery_type());
        message.setQueryType(queryType);

        // Set metadata
        message.setSqlQuery(pythonResponse.getSql_query());
        message.setTokensUsed(pythonResponse.getTokens_used());
        message.setProcessingTimeMs((int) processingTime);
        message.setModelUsed("meta-llama/llama-4-scout-17b-16e-instruct");

        // Serialize data and tables to JSON
        try {
            if (pythonResponse.getData() != null) {
                message.setSqlResult(objectMapper.writeValueAsString(pythonResponse.getData()));
            }
            if (pythonResponse.getTables_used() != null && !pythonResponse.getTables_used().isEmpty()) {
                message.setTablesAccessed(objectMapper.writeValueAsString(pythonResponse.getTables_used()));
            }
        } catch (Exception e) {
            logger.warn("Failed to serialize response data: {}", e.getMessage());
        }

        // Set error if present
        if (pythonResponse.getError() != null) {
            message.setErrorMessage(pythonResponse.getError());
            message.setQueryType(QueryType.ERROR);
        }

        return messageRepository.save(message);
    }

    /**
     * Get recent conversation history
     */
    private List<ChatMessage> getRecentHistory(Conversation conversation, int limit) {
        List<ChatMessage> allMessages = messageRepository.findByConversationOrderByCreatedDateAsc(conversation);

        // Return last N messages
        int startIndex = Math.max(0, allMessages.size() - limit);
        return allMessages.subList(startIndex, allMessages.size());
    }

    /**
     * Call Python chatbot microservice
     */
    private ChatbotResponse callPythonChatbot(
            String sessionId,
            String message,
            List<ChatMessage> history
    ) {
        try {
            // Convert history to format expected by Python service
            List<Map<String, String>> historyList = convertHistoryToList(history);

            // Build request
            ChatbotRequest request = new ChatbotRequest(sessionId, message, historyList);

            // Call Python service
            String url = pythonServiceUrl + "/api/chat";
            logger.debug("Calling Python chatbot service at: {}", url);

            ChatbotResponse response = restTemplate.postForObject(url, request, ChatbotResponse.class);

            if (response == null) {
                throw new ChatbotException("Python chatbot service returned null response");
            }

            return response;

        } catch (Exception e) {
            logger.error("Failed to call Python chatbot service", e);
            throw new ChatbotException("Chatbot service unavailable: " + e.getMessage(), e);
        }
    }

    /**
     * Convert message history to list format for Python service
     */
    private List<Map<String, String>> convertHistoryToList(List<ChatMessage> history) {
        return history.stream()
                .map(msg -> {
                    Map<String, String> historyItem = new HashMap<>();
                    historyItem.put("role", msg.getRole().name().toLowerCase());
                    historyItem.put("content", msg.getContent());
                    return historyItem;
                })
                .collect(Collectors.toList());
    }

    /**
     * Build ChatResponse DTO from Python response
     */
    private ChatResponse buildChatResponse(ChatbotResponse pythonResponse, long processingTime) {
        QueryType queryType = parseQueryType(pythonResponse.getQuery_type());

        return new ChatResponse(
                pythonResponse.getResponse(),
                queryType,
                pythonResponse.getSql_query(),
                pythonResponse.getData(),
                (int) processingTime
        );
    }

    /**
     * Parse query type string to enum
     */
    private QueryType parseQueryType(String queryTypeStr) {
        if (queryTypeStr == null) {
            return QueryType.GENERAL;
        }

        try {
            return QueryType.valueOf(queryTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown query type: {}", queryTypeStr);
            return QueryType.GENERAL;
        }
    }

    /**
     * Generate conversation title from first message
     */
    private String generateConversationTitle(String firstMessage) {
        // Take first 50 characters or until first line break
        String title = firstMessage.split("\n")[0];
        if (title.length() > 50) {
            title = title.substring(0, 47) + "...";
        }
        return title;
    }
}
