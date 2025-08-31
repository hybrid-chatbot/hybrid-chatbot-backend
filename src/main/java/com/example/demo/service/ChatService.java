package com.example.demo.service;

import com.example.demo.model.ChatMessage;
import com.example.demo.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    /**
     * 채팅 메시지를 저장합니다.
     * sender가 'user'일 경우, analysisInfo는 null일 수 있습니다.
     */
    public void saveMessage(String sessionId, String userId, String sender, String message, String languageCode, ChatMessage.AnalysisInfo analysisInfo) {
        ChatMessage chatMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .userId(userId)
                .sender(sender)
                .message(message)
                .languageCode(languageCode)
                .timestamp(LocalDateTime.now())
                .analysisInfo(analysisInfo) // ✨ 이 부분이 바뀌었습니다.
                .build();
        chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getRecentMessagesBySessionId(String sessionId) {
        return chatMessageRepository.findTop10BySessionIdOrderByTimestampDesc(sessionId);
    }

    public List<ChatMessage> getRecentMessagesByUserId(String userId) {
        return chatMessageRepository.findTop10ByUserIdOrderByTimestampDesc(userId);
    }
}