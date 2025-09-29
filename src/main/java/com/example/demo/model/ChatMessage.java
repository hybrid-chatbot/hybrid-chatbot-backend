// src/main/java/com/example/demo/model/ChatMessage.java
package com.example.demo.model;

import com.example.demo.dto.AnalysisTrace; // ✨ AnalysisTrace import
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "chat_messages")
public class ChatMessage {
    @Id
    private String id;
    private String sessionId;
    private String userId;
    private String sender;
    private String message;
    private String languageCode;
    private LocalDateTime timestamp;
    private AnalysisInfo analysisInfo;
    private AnalysisTrace analysisTrace; // ✨ '생각의 흔적'을 저장할 필드를 추가합니다.

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

    // 이 메서드는 이제 사용되지 않을 수 있으므로, 그대로 두거나 삭제해도 괜찮습니다.
    public AnalysisInfo getDialogflowResponse() {
        return this.analysisInfo;
    }

    // 쇼핑 데이터를 위한 중첩 클래스
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShoppingData {
        private String intentType;
        private String originalQuery;
        private Integer totalResults;
        private String searchTime;
        private String confidence;
        private java.util.List<ProductInfo> products;
    }

    // 상품 정보를 위한 중첩 클래스
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private Long id;
        private String title;
        private String link;
        private String image;
        private Integer lprice;
        private Integer hprice;
        private String mallName;
        private String brand;
        private String category1;
        private String category2;
        private String category3;
        private String category4;
    }
}