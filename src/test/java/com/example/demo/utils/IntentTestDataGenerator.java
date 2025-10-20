package com.example.demo.utils;

import com.example.demo.model.ChatMessage;
import com.example.navershopping.entity.NaverShoppingItem;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 의도 분석 테스트를 위한 데이터 생성 유틸리티
 */
@Component
public class IntentTestDataGenerator {

    /**
     * 다양한 의도 분석 예시 데이터 생성
     */
    public static class IntentExamples {
        
        // 상품 검색 의도 예시
        public static final Map<String, String> PRODUCT_SEARCH_EXAMPLES = Map.of(
            "아이폰 케이스", "product_search",
            "갤럭시 케이스 찾아줘", "product_search", 
            "노트북 추천", "product_search",
            "운동화 구매하고 싶어", "product_search",
            "청바지 어디서 살까", "product_search"
        );

        // 상품 추천 의도 예시
        public static final Map<String, String> PRODUCT_RECOMMENDATION_EXAMPLES = Map.of(
            "아이폰 케이스 추천해줘", "product_recommendation",
            "좋은 노트북 추천해주세요", "product_recommendation",
            "인기 있는 운동화 추천", "product_recommendation",
            "베스트셀러 청바지 추천", "product_recommendation",
            "많이 팔리는 이어폰 추천", "product_recommendation"
        );

        // 상품 필터링 의도 예시
        public static final Map<String, String> PRODUCT_FILTER_EXAMPLES = Map.of(
            "5만원 이하 아이폰 케이스", "product_filter",
            "애플 브랜드 노트북", "product_filter",
            "2만원대 운동화", "product_filter",
            "나이키 브랜드 신발", "product_filter",
            "10만원 이하 스마트폰", "product_filter"
        );

        // 상품 비교 의도 예시
        public static final Map<String, String> PRODUCT_COMPARE_EXAMPLES = Map.of(
            "아이폰 케이스 비교해줘", "product_compare",
            "노트북 성능 비교", "product_compare",
            "운동화 가격 비교", "product_compare",
            "이어폰 사양 비교", "product_compare",
            "스마트폰 모델 비교", "product_compare"
        );

        // 일반 대화 의도 예시 (쇼핑과 무관)
        public static final Map<String, String> GENERAL_CHAT_EXAMPLES = Map.of(
            "안녕하세요", "greeting",
            "오늘 날씨 어때?", "weather_inquiry",
            "도움말", "help_request",
            "고마워", "thanks",
            "잘가", "goodbye"
        );
    }

    /**
     * 테스트용 상품 데이터 생성
     */
    public static List<NaverShoppingItem> generateTestProducts() {
        return Arrays.asList(
            // 아이폰 케이스 상품들
            NaverShoppingItem.builder()
                .productId("iphone-case-1")
                .title("아이폰 15 Pro 투명 케이스")
                .link("https://test.com/iphone15pro-clear-case")
                .image("https://test.com/iphone15pro-clear-case.jpg")
                .lprice(25000)
                .hprice(35000)
                .mallName("애플스토어")
                .brand("Apple")
                .category1("디지털/가전")
                .category2("휴대폰")
                .category3("케이스")
                .searchQuery("아이폰 케이스")
                .lastSearchedAt(LocalDateTime.now())
                .searchCount(15)
                .build(),
            
            NaverShoppingItem.builder()
                .productId("iphone-case-2")
                .title("아이폰 14 실리콘 케이스")
                .link("https://test.com/iphone14-silicone-case")
                .image("https://test.com/iphone14-silicone-case.jpg")
                .lprice(20000)
                .hprice(30000)
                .mallName("삼성스토어")
                .brand("Samsung")
                .category1("디지털/가전")
                .category2("휴대폰")
                .category3("케이스")
                .searchQuery("아이폰 케이스")
                .lastSearchedAt(LocalDateTime.now())
                .searchCount(8)
                .build(),

            // 노트북 상품들
            NaverShoppingItem.builder()
                .productId("laptop-1")
                .title("MacBook Pro 14인치")
                .link("https://test.com/macbook-pro-14")
                .image("https://test.com/macbook-pro-14.jpg")
                .lprice(2500000)
                .hprice(3000000)
                .mallName("애플스토어")
                .brand("Apple")
                .category1("디지털/가전")
                .category2("컴퓨터")
                .category3("노트북")
                .searchQuery("노트북")
                .lastSearchedAt(LocalDateTime.now())
                .searchCount(12)
                .build(),

            NaverShoppingItem.builder()
                .productId("laptop-2")
                .title("LG 그램 15인치")
                .link("https://test.com/lg-gram-15")
                .image("https://test.com/lg-gram-15.jpg")
                .lprice(1200000)
                .hprice(1500000)
                .mallName("LG스토어")
                .brand("LG")
                .category1("디지털/가전")
                .category2("컴퓨터")
                .category3("노트북")
                .searchQuery("노트북")
                .lastSearchedAt(LocalDateTime.now())
                .searchCount(6)
                .build(),

            // 운동화 상품들
            NaverShoppingItem.builder()
                .productId("shoes-1")
                .title("나이키 에어맥스 270")
                .link("https://test.com/nike-airmax-270")
                .image("https://test.com/nike-airmax-270.jpg")
                .lprice(120000)
                .hprice(150000)
                .mallName("나이키스토어")
                .brand("Nike")
                .category1("패션/의류")
                .category2("신발")
                .category3("운동화")
                .searchQuery("운동화")
                .lastSearchedAt(LocalDateTime.now())
                .searchCount(20)
                .build(),

            NaverShoppingItem.builder()
                .productId("shoes-2")
                .title("아디다스 울트라부스트 22")
                .link("https://test.com/adidas-ultraboost-22")
                .image("https://test.com/adidas-ultraboost-22.jpg")
                .lprice(150000)
                .hprice(180000)
                .mallName("아디다스스토어")
                .brand("Adidas")
                .category1("패션/의류")
                .category2("신발")
                .category3("운동화")
                .searchQuery("운동화")
                .lastSearchedAt(LocalDateTime.now())
                .searchCount(18)
                .build()
        );
    }

    /**
     * 의도별 파라미터 생성
     */
    public static Map<String, Object> generateFilterParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("minPrice", 10000);
        parameters.put("maxPrice", 50000);
        parameters.put("brand", "Apple");
        parameters.put("category", "케이스");
        return parameters;
    }

    /**
     * 비교용 파라미터 생성
     */
    public static Map<String, Object> generateCompareParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("product1", "아이폰 15 케이스");
        parameters.put("product2", "아이폰 14 케이스");
        parameters.put("compareType", "price");
        return parameters;
    }

    /**
     * 테스트용 쇼핑 데이터 생성
     */
    public static ChatMessage.ShoppingData generateTestShoppingData(String intentType, String query) {
        return ChatMessage.ShoppingData.builder()
                .intentType(intentType)
                .originalQuery(query)
                .totalResults(3)
                .searchTime(LocalDateTime.now().toString())
                .confidence("high")
                .products(convertToProductInfo(generateTestProducts().subList(0, 3)))
                .build();
    }

    /**
     * NaverShoppingItem을 ProductInfo로 변환
     */
    private static List<ChatMessage.ProductInfo> convertToProductInfo(List<NaverShoppingItem> items) {
        return items.stream()
                .<ChatMessage.ProductInfo>map(item -> ChatMessage.ProductInfo.builder()
                        .id(item.getId())
                        .title(item.getTitle())
                        .link(item.getLink())
                        .image(item.getImage())
                        .lprice(item.getLprice())
                        .hprice(item.getHprice())
                        .mallName(item.getMallName())
                        .brand(item.getBrand())
                        .category1(item.getCategory1())
                        .category2(item.getCategory2())
                        .category3(item.getCategory3())
                        .category4(item.getCategory4())
                        .searchCount(item.getSearchCount() != null ? item.getSearchCount() : 0)
                        .lastSearchedAt(item.getLastSearchedAt() != null ? item.getLastSearchedAt().toString() : LocalDateTime.now().toString())
                        .build())
                .toList();
    }

    /**
     * 의도 분석 결과 시뮬레이션
     */
    public static class IntentSimulation {
        
        public static String simulateIntentDetection(String userMessage) {
            String lowerMessage = userMessage.toLowerCase();
            
            if (lowerMessage.contains("추천") || lowerMessage.contains("추천해")) {
                return "product_recommendation";
            } else if (lowerMessage.contains("비교") || lowerMessage.contains("비교해")) {
                return "product_compare";
            } else if (lowerMessage.contains("이하") || lowerMessage.contains("이상") || 
                      lowerMessage.contains("브랜드") || lowerMessage.contains("만원")) {
                return "product_filter";
            } else if (lowerMessage.contains("찾아") || lowerMessage.contains("검색") || 
                      lowerMessage.contains("구매") || lowerMessage.contains("살")) {
                return "product_search";
            } else {
                return "general_chat";
            }
        }
        
        public static float simulateConfidenceScore(String intentName) {
            return switch (intentName) {
                case "product_search" -> 0.95f;
                case "product_recommendation" -> 0.90f;
                case "product_filter" -> 0.85f;
                case "product_compare" -> 0.88f;
                default -> 0.70f;
            };
        }
    }
}

