package com.example.portfolioapp.controller;

import com.example.portfolioapp.dto.chatbot.ChatRequest;
import com.example.portfolioapp.dto.chatbot.ChatResponse;
import com.example.portfolioapp.entity.ChatMessage;
import com.example.portfolioapp.entity.Conversation;
import com.example.portfolioapp.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for chatbot operations
 */
@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    /**
     * Send a message to the chatbot
     * POST /api/chatbot/chat
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = chatbotService.sendMessage(
                request.getSessionId(),
                request.getMessage()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Start a new conversation
     * POST /api/chatbot/conversations
     */
    @PostMapping("/conversations")
    public ResponseEntity<Map<String, Object>> createConversation(
            @RequestParam(required = false, defaultValue = "default_user") String userId
    ) {
        Conversation conversation = chatbotService.createConversation(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", conversation.getSessionId());
        response.put("userId", conversation.getUserId());
        response.put("status", conversation.getStatus());
        response.put("createdDate", conversation.getCreatedDate());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get conversation history
     * GET /api/chatbot/conversations/{sessionId}
     */
    @GetMapping("/conversations/{sessionId}")
    public ResponseEntity<Map<String, Object>> getConversation(@PathVariable String sessionId) {
        Conversation conversation = chatbotService.getConversation(sessionId);

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", conversation.getSessionId());
        response.put("userId", conversation.getUserId());
        response.put("title", conversation.getTitle());
        response.put("status", conversation.getStatus());
        response.put("createdDate", conversation.getCreatedDate());
        response.put("lastMessageAt", conversation.getLastMessageAt());

        // Convert messages to simplified format
        List<Map<String, Object>> messages = conversation.getMessages().stream()
                .map(this::convertMessageToMap)
                .collect(Collectors.toList());
        response.put("messages", messages);
        response.put("messageCount", messages.size());

        return ResponseEntity.ok(response);
    }

    /**
     * List all conversations for a user
     * GET /api/chatbot/conversations
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<Map<String, Object>>> listConversations(
            @RequestParam(defaultValue = "default_user") String userId
    ) {
        List<Conversation> conversations = chatbotService.listConversations(userId);

        List<Map<String, Object>> response = conversations.stream()
                .map(this::convertConversationToSummary)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Delete conversation
     * DELETE /api/chatbot/conversations/{sessionId}
     */
    @DeleteMapping("/conversations/{sessionId}")
    public ResponseEntity<Map<String, String>> deleteConversation(@PathVariable String sessionId) {
        chatbotService.deleteConversation(sessionId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Conversation deleted successfully");
        response.put("sessionId", sessionId);

        return ResponseEntity.ok(response);
    }

    /**
     * Convert ChatMessage to Map for API response
     */
    private Map<String, Object> convertMessageToMap(ChatMessage message) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", message.getId());
        map.put("role", message.getRole());
        map.put("content", message.getContent());
        map.put("queryType", message.getQueryType());
        map.put("sqlQuery", message.getSqlQuery());
        map.put("tokensUsed", message.getTokensUsed());
        map.put("processingTimeMs", message.getProcessingTimeMs());
        map.put("createdDate", message.getCreatedDate());
        return map;
    }

    /**
     * Convert Conversation to summary Map for API response
     */
    private Map<String, Object> convertConversationToSummary(Conversation conversation) {
        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", conversation.getSessionId());
        map.put("userId", conversation.getUserId());
        map.put("title", conversation.getTitle());
        map.put("status", conversation.getStatus());
        map.put("messageCount", conversation.getMessages().size());
        map.put("createdDate", conversation.getCreatedDate());
        map.put("lastMessageAt", conversation.getLastMessageAt());
        return map;
    }
}
