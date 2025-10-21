package com.example.navershopping.service;

import com.example.navershopping.entity.ChatHistory;
import com.example.navershopping.repository.ChatHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChatHistoryService {

    private final ChatHistoryRepository chatHistoryRepository;

    @Autowired
    public ChatHistoryService(ChatHistoryRepository chatHistoryRepository) {
        this.chatHistoryRepository = chatHistoryRepository;
    }

    public ChatHistory saveChatHistory(String userId, String userMessage, String botResponse, String sessionId) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUserId(userId);
        chatHistory.setUserMessage(userMessage);
        chatHistory.setBotResponse(botResponse);
        chatHistory.setSessionId(sessionId);
        return chatHistoryRepository.save(chatHistory);
    }

    public List<ChatHistory> getChatHistoryByUserId(String userId) {
        return chatHistoryRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public List<ChatHistory> getChatHistoryBySessionId(String sessionId) {
        return chatHistoryRepository.findBySessionIdOrderByTimestampDesc(sessionId);
    }

    public void deleteChatHistoryByUserId(String userId) {
        chatHistoryRepository.deleteByUserId(userId);
    }
} 