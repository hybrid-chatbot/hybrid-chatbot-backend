package com.example.navershopping.controller;

import com.example.navershopping.entity.ChatHistory;
import com.example.navershopping.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat-history")
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

    @Autowired
    public ChatHistoryController(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
    }

    @PostMapping
    public ResponseEntity<ChatHistory> saveChatHistory(
            @RequestParam String userId,
            @RequestParam String userMessage,
            @RequestParam String botResponse,
            @RequestParam String sessionId) {
        ChatHistory savedHistory = chatHistoryService.saveChatHistory(userId, userMessage, botResponse, sessionId);
        return ResponseEntity.ok(savedHistory);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatHistory>> getChatHistoryByUserId(@PathVariable String userId) {
        List<ChatHistory> history = chatHistoryService.getChatHistoryByUserId(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<ChatHistory>> getChatHistoryBySessionId(@PathVariable String sessionId) {
        List<ChatHistory> history = chatHistoryService.getChatHistoryBySessionId(sessionId);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteChatHistoryByUserId(@PathVariable String userId) {
        chatHistoryService.deleteChatHistoryByUserId(userId);
        return ResponseEntity.ok().build();
    }
} 