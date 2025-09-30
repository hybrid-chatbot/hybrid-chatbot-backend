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
    private String message; // 사용자 메시지
    private String languageCode;
    private LocalDateTime timestamp;
    private AnalysisInfo analysisInfo; // ✨ analysisInfo 필드 추가
    private ShoppingData shoppingData; // ✨ 쇼핑 데이터 필드 추가

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
        private String intentType; // "product_search", "product_filter", "product_recommendation", "product_compare"
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
        private Long id; // JPA 엔티티 PK (프론트 키로 사용)
        private String title;
        private String image;
        private String link;
        private Integer lprice; // 최저가
        private Integer hprice; // 최고가
        private String mallName;
        private String brand;
        private String category1; // 대분류
        private String category2; // 중분류
        private String category3;
        private String category4;
        private String productType;
        private String maker;
        private Integer searchCount; // 누적 검색 횟수 (추천 가중치)
        private String lastSearchedAt; // 마지막 검색 시각
        private String priceFormatted;
        private String discountRate;
        private boolean isRecommended;
        private String recommendationReason;
    }

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