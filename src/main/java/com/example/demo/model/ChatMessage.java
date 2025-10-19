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
    private AnalysisInfo analysisInfo; // ✨ analysisInfo 필드 추가
    private ShoppingData shoppingData; // ✨ 쇼핑 데이터 필드 추가
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

    // --- 쇼핑 데이터를 위한 중첩 클래스 ---
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShoppingData {
        private String intentType; // "search", "filter", "recommend", "compare"
        private String productCategory;
        private String brand;
        private String color;
        private String size;
        private Integer minPrice;
        private Integer maxPrice;
        private String mallName;
        private java.util.List<String> keywords;
        private String originalQuery;
        private Integer totalResults;
        private String searchTime;
        private String confidence;
        private java.util.List<ProductInfo> products;
    }

    // --- 상품 정보를 위한 중첩 클래스 ---
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private Long id;
        private String title;
        private String image;
        private String link;
        private Integer lprice;
        private Integer hprice;
        private String mallName;
        private String brand;
        private String category1;
        private String category2;
        private String productType;
        private String maker;
        private Integer searchCount;
        private String lastSearchedAt;
        private String priceFormatted;
        private String discountRate;
        private boolean isRecommended;
        private String recommendationReason;
    }

    public AnalysisInfo getDialogflowResponse() {
        return this.analysisInfo;
    }
}