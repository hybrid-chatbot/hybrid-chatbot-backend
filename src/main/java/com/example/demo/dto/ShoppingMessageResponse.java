package com.example.demo.dto;

import com.example.navershopping.entity.NaverShoppingItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 쇼핑 챗봇 메시지 응답 DTO
 * 
 * 쇼핑 챗봇에서 사용자에게 보내는 응답을 담는 데이터 전송 객체입니다.
 * 상품 정보, 분석 정보, 메시지 타입 등을 포함합니다.
 * 
 * 주요 구성 요소:
 * - 사용자 정보 (userId, sessionId)
 * - 응답 메시지 (response)
 * - 메시지 타입 (messageType)
 * - 상품 카드 목록 (products)
 * - 분석 정보 (analysisInfo)
 * - 타임스탬프 (timestamp)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingMessageResponse {
    
    private String userId; // 사용자 ID
    private String sessionId; // 세션 ID
    private String response; // 봇의 응답 메시지
    private String messageType; // 메시지 타입: "text", "shopping", "recommendation", "comparison"
    private List<ProductCard> products; // 상품 카드 목록
    private ShoppingAnalysisInfo analysisInfo; // 쇼핑 분석 정보
    private String timestamp; // 응답 생성 시간
    private String sortOrder; // 정렬 순서: "asc" (오름차순), "desc" (내림차순), null (기본)
    private String sortType; // 정렬 타입: "price" (가격순), "popularity" (인기순), "recent" (최신순), null (기본)

    /**
     * 상품 카드 정보
     * 
     * 채팅 UI에서 상품을 카드 형태로 표시하기 위한 정보를 담는 클래스입니다.
     * 상품의 기본 정보, 가격, 이미지, 링크 등을 포함합니다.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductCard {
        private Long id; // 상품 ID
        private String title; // 상품명
        private String image; // 상품 이미지 URL
        private String link; // 상품 링크
        private Integer lprice; // 최저가
        private Integer hprice; // 최고가
        private String mallName; // 쇼핑몰명
        private String brand; // 브랜드
        private String category1; // 카테고리1
        private String category2; // 카테고리2
        private String productType; // 상품 타입
        private String maker; // 제조사
        private Integer searchCount; // 검색 횟수
        private String lastSearchedAt; // 마지막 검색 시간
        private String priceFormatted; // "15,000원" 형태로 포맷된 가격
        private String discountRate; // 할인율 (있는 경우)
        private boolean isRecommended; // 추천 상품 여부
        private String recommendationReason; // 추천 이유
    }

    /**
     * 쇼핑 분석 정보
     * 
     * 사용자의 쇼핑 의도와 검색 조건을 분석한 정보를 담는 클래스입니다.
     * 검색 결과의 통계 정보와 분석 데이터를 포함합니다.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShoppingAnalysisInfo {
        private String intentType; // 의도 타입: "search", "filter", "recommend", "compare"
        private String productCategory; // 상품 카테고리
        private String brand; // 브랜드
        private String color; // 색상
        private String size; // 사이즈
        private Integer minPrice; // 최소 가격
        private Integer maxPrice; // 최대 가격
        private String mallName; // 쇼핑몰명
        private List<String> keywords; // 검색 키워드 목록
        private String originalQuery; // 원본 검색어
        private Integer totalResults; // 총 검색 결과 수
        private String searchTime; // 검색 소요 시간
        private String confidence; // AI 서버 신뢰도
        private String engine; // 사용된 엔진: "dynamic_sql", "fallback"
        private String analysisMethod; // 분석 방법: "LLM 기반 동적 SQL 쿼리"
    }

    /**
     * NaverShoppingItem을 ProductCard로 변환
     * 
     * 데이터베이스의 NaverShoppingItem 엔티티를 채팅 UI용 ProductCard로 변환합니다.
     * 가격 포맷팅과 할인율 계산도 함께 수행합니다.
     * 
     * @param item 변환할 NaverShoppingItem 객체
     * @return ProductCard 객체
     */
    public static ProductCard fromNaverShoppingItem(NaverShoppingItem item) {
        return ProductCard.builder()
                .id(item.getId()) // 상품 ID
                .title(item.getTitle()) // 상품명
                .image(item.getImage()) // 상품 이미지
                .link(item.getLink()) // 상품 링크
                .lprice(item.getLprice()) // 최저가
                .hprice(item.getHprice()) // 최고가
                .mallName(item.getMallName()) // 쇼핑몰명
                .brand(item.getBrand()) // 브랜드
                .category1(item.getCategory1()) // 카테고리1
                .category2(item.getCategory2()) // 카테고리2
                .productType(item.getProductType()) // 상품 타입
                .maker(item.getMaker()) // 제조사
                .searchCount(item.getSearchCount()) // 검색 횟수
                .lastSearchedAt(item.getLastSearchedAt() != null ? item.getLastSearchedAt().toString() : null) // 마지막 검색 시간
                .priceFormatted(formatPrice(item.getLprice())) // 포맷된 가격
                .discountRate(calculateDiscountRate(item.getLprice(), item.getHprice())) // 할인율
                .isRecommended(false) // 기본값: 추천 아님
                .build();
    }

    /**
     * 가격 포맷팅
     * 
     * 숫자 가격을 "15,000원" 형태의 문자열로 변환합니다.
     * 
     * @param price 포맷팅할 가격
     * @return 포맷된 가격 문자열
     */
    private static String formatPrice(Integer price) {
        if (price == null) return "가격 정보 없음";
        return String.format("%,d원", price); // 천 단위 구분자 추가
    }

    /**
     * 할인율 계산
     * 
     * 최고가와 최저가를 비교하여 할인율을 계산합니다.
     * 
     * @param lprice 최저가
     * @param hprice 최고가
     * @return 할인율 문자열 (예: "20% 할인") 또는 null
     */
    private static String calculateDiscountRate(Integer lprice, Integer hprice) {
        // 가격 정보가 없거나 할인이 없는 경우
        if (lprice == null || hprice == null || hprice <= lprice) {
            return null;
        }
        
        // 할인율 계산: ((최고가 - 최저가) / 최고가) * 100
        double discountRate = ((double)(hprice - lprice) / hprice) * 100;
        return String.format("%.0f%% 할인", discountRate);
    }

    /**
     * 추천 상품으로 설정
     * 
     * 상품 목록을 추천 상품으로 표시하도록 설정합니다.
     * 
     * @param products 추천 상품 목록
     * @param reason 추천 이유
     */
    public void setRecommendedProducts(List<ProductCard> products, String reason) {
        for (ProductCard product : products) {
            product.setRecommended(true); // 추천 상품으로 설정
            product.setRecommendationReason(reason); // 추천 이유 설정
        }
    }

    /**
     * 상품 카드 생성 (간단한 버전)
     * 
     * 최소한의 정보만으로 상품 카드를 생성합니다.
     * 
     * @param title 상품명
     * @param image 상품 이미지 URL
     * @param link 상품 링크
     * @param price 상품 가격
     * @return 생성된 ProductCard 객체
     */
    public static ProductCard createSimpleProductCard(String title, String image, String link, Integer price) {
        return ProductCard.builder()
                .title(title) // 상품명
                .image(image) // 상품 이미지
                .link(link) // 상품 링크
                .lprice(price) // 가격
                .priceFormatted(formatPrice(price)) // 포맷된 가격
                .build();
    }
}



