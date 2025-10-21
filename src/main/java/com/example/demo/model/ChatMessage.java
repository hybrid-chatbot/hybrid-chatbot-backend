package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "chat_messages") // MongoDB 컬렉션 이름 지정
public class ChatMessage {
    @Id
    private String id;
    private String sessionId;
    private String userId;
    private String sender;
    private String message;
    private String languageCode;
    private LocalDateTime timestamp;
    private AnalysisInfo analysisInfo; // ✨ analysisInfo 필드 추가

    // --- ChatMessage 클래스 내부에 선언된 중첩 클래스 ---
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisInfo {
        private String engine;
        private String intentName;
        private String originalIntentName;
        private float originalIntentScore;
    }
}