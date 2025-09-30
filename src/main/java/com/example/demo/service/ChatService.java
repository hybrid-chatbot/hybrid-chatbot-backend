package com.example.demo.service;

import com.example.demo.dto.AnalysisTrace; // ✨ AnalysisTrace import
import com.example.demo.model.ChatMessage;
import com.example.demo.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    /**
     * (기존 메서드) 채팅 메시지를 저장합니다.
     * sender가 'user'일 경우, analysisInfo는 null일 수 있습니다.
     */
    public void saveMessage(String sessionId, String userId, String sender, String message, String languageCode, ChatMessage.AnalysisInfo analysisInfo) {
        // ✨ 새로운 saveMessage 메서드를 호출하여 코드 중복을 방지합니다.
        this.saveMessage(sessionId, userId, sender, message, languageCode, analysisInfo, null);
    }

    /**
     * ✨ (새로 추가된 메서드) 채팅 메시지와 '분석 과정(Trace)'을 함께 저장합니다.
     */
    public void saveMessage(String sessionId, String userId, String sender, String message, String languageCode, ChatMessage.AnalysisInfo analysisInfo, AnalysisTrace analysisTrace) {
        ChatMessage chatMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .userId(userId)
                .sender(sender)
                .message(message)
                .languageCode(languageCode)
                .timestamp(LocalDateTime.now())
                .analysisInfo(analysisInfo)
                .analysisTrace(analysisTrace) // ✨ 분석 과정을 함께 저장합니다.
                .build();
        chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getRecentMessagesBySessionId(String sessionId) {
        return chatMessageRepository.findTop10BySessionIdOrderByTimestampDesc(sessionId);
    }

    public List<ChatMessage> getRecentMessagesByUserId(String userId) {
        return chatMessageRepository.findTop10ByUserIdOrderByTimestampDesc(userId);
    }

    public Optional<ChatMessage> findLatestMessageBySessionId(String sessionId) {
        return chatMessageRepository.findTopBySessionIdOrderByTimestampDesc(sessionId);
    }
}
