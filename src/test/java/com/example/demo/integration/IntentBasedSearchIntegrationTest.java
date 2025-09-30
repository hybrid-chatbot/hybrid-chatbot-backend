package com.example.demo.integration;

import com.example.demo.model.ChatMessage;
import com.example.demo.service.ChatService;
import com.example.demo.service.IntentBasedSearchService;
import com.example.navershopping.entity.NaverShoppingItem;
import com.example.navershopping.service.NaverShoppingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 의도 기반 검색 통합 테스트
 * 실제 데이터베이스와 연동하여 전체 흐름을 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
class IntentBasedSearchIntegrationTest {

    @Autowired
    private IntentBasedSearchService intentBasedSearchService;

    @Autowired
    private ChatService chatService;

    @MockBean
    private NaverShoppingService naverShoppingService;

    private List<NaverShoppingItem> testProducts;

    @BeforeEach
    void setUp() {
        // 테스트용 상품 데이터 준비
        testProducts = Arrays.asList(
                NaverShoppingItem.builder()
                        .productId("integration-test-1")
                        .title("아이폰 15 Pro 케이스")
                        .link("https://test.com/iphone15pro-case")
                        .image("https://test.com/iphone15pro-case.jpg")
                        .lprice(25000)
                        .hprice(35000)
                        .mallName("애플스토어")
                        .brand("Apple")
                        .category1("디지털/가전")
                        .category2("휴대폰")
                        .category3("케이스")
                        .searchQuery("아이폰 케이스")
                        .lastSearchedAt(LocalDateTime.now())
                        .searchCount(10)
                        .build(),
                NaverShoppingItem.builder()
                        .productId("integration-test-2")
                        .title("아이폰 14 케이스")
                        .link("https://test.com/iphone14-case")
                        .image("https://test.com/iphone14-case.jpg")
                        .lprice(20000)
                        .hprice(30000)
                        .mallName("삼성스토어")
                        .brand("Samsung")
                        .category1("디지털/가전")
                        .category2("휴대폰")
                        .category3("케이스")
                        .searchQuery("아이폰 케이스")
                        .lastSearchedAt(LocalDateTime.now())
                        .searchCount(5)
                        .build(),
                NaverShoppingItem.builder()
                        .productId("integration-test-3")
                        .title("갤럭시 S24 케이스")
                        .link("https://test.com/galaxy-s24-case")
                        .image("https://test.com/galaxy-s24-case.jpg")
                        .lprice(15000)
                        .hprice(25000)
                        .mallName("LG스토어")
                        .brand("LG")
                        .category1("디지털/가전")
                        .category2("휴대폰")
                        .category3("케이스")
                        .searchQuery("갤럭시 케이스")
                        .lastSearchedAt(LocalDateTime.now())
                        .searchCount(3)
                        .build()
        );
    }

    @Test
    void testProductSearchFlow() {
        // Given
        String sessionId = "test-session-" + System.currentTimeMillis();
        String userId = "test-user";
        String intentName = "product_search";
        String originalQuery = "아이폰 케이스";
        
        when(naverShoppingService.getSavedProductsByQuery(originalQuery))
                .thenReturn(testProducts.subList(0, 2));

        // When
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                intentName, originalQuery, null);

        // Then
        assertNotNull(result);
        assertEquals(intentName, result.getIntentType());
        assertEquals(originalQuery, result.getOriginalQuery());
        assertEquals(2, result.getTotalResults());
        assertNotNull(result.getProducts());
        assertEquals(2, result.getProducts().size());
        
        // 상품 정보 검증
        ChatMessage.ProductInfo firstProduct = result.getProducts().get(0);
        assertEquals("integration-test-1", firstProduct.getId());
        assertEquals("아이폰 15 Pro 케이스", firstProduct.getTitle());
        assertEquals(25000, firstProduct.getLprice());
        assertEquals("Apple", firstProduct.getBrand());
        assertEquals("애플스토어", firstProduct.getMallName());
    }

    @Test
    void testRecommendationFlow() {
        // Given
        String intentName = "product_recommendation";
        String originalQuery = "아이폰 케이스 추천해줘";
        
        when(naverShoppingService.getSavedProductsByQuery(originalQuery))
                .thenReturn(testProducts);

        // When
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                intentName, originalQuery, null);

        // Then
        assertNotNull(result);
        assertEquals(intentName, result.getIntentType());
        assertEquals(3, result.getTotalResults());
        
        // 추천 로직 검증 (검색 횟수 기준 정렬)
        List<ChatMessage.ProductInfo> products = result.getProducts();
        assertTrue(products.get(0).getSearchCount() >= products.get(1).getSearchCount());
        assertTrue(products.get(1).getSearchCount() >= products.get(2).getSearchCount());
    }

    @Test
    void testFilterFlow() {
        // Given
        String intentName = "product_filter";
        String originalQuery = "아이폰 케이스";
        java.util.Map<String, Object> parameters = new java.util.HashMap<>();
        parameters.put("minPrice", 20000);
        parameters.put("maxPrice", 30000);
        parameters.put("brand", "Apple");
        
        when(naverShoppingService.getSavedProductsByQuery(originalQuery))
                .thenReturn(testProducts);

        // When
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                intentName, originalQuery, parameters);

        // Then
        assertNotNull(result);
        assertEquals(intentName, result.getIntentType());
        
        // 필터링 결과 검증
        List<ChatMessage.ProductInfo> products = result.getProducts();
        assertTrue(products.size() <= 3); // 최대 10개로 제한
        
        for (ChatMessage.ProductInfo product : products) {
            assertTrue(product.getLprice() >= 20000);
            assertTrue(product.getLprice() <= 30000);
            assertEquals("Apple", product.getBrand());
        }
    }

    @Test
    void testCompareFlow() {
        // Given
        String intentName = "product_compare";
        String originalQuery = "아이폰 케이스 비교";
        
        when(naverShoppingService.getSavedProductsByQuery(originalQuery))
                .thenReturn(testProducts);

        // When
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                intentName, originalQuery, null);

        // Then
        assertNotNull(result);
        assertEquals(intentName, result.getIntentType());
        assertEquals(3, result.getTotalResults());
        
        // 비교를 위한 정렬 검증 (가격 기준 오름차순)
        List<ChatMessage.ProductInfo> products = result.getProducts();
        assertTrue(products.get(0).getLprice() <= products.get(1).getLprice());
        assertTrue(products.get(1).getLprice() <= products.get(2).getLprice());
    }

    @Test
    void testEmptyResultHandling() {
        // Given
        String intentName = "product_search";
        String originalQuery = "존재하지 않는 상품";
        
        when(naverShoppingService.getSavedProductsByQuery(originalQuery))
                .thenReturn(Arrays.asList());
        when(naverShoppingService.getSavedProductsByTitle(originalQuery))
                .thenReturn(Arrays.asList());

        // When
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                intentName, originalQuery, null);

        // Then
        assertNotNull(result);
        assertEquals(intentName, result.getIntentType());
        assertEquals(0, result.getTotalResults());
        assertTrue(result.getProducts().isEmpty());
    }

    @Test
    void testErrorHandling() {
        // Given
        String intentName = "product_search";
        String originalQuery = "테스트 쿼리";
        
        when(naverShoppingService.getSavedProductsByQuery(anyString()))
                .thenThrow(new RuntimeException("데이터베이스 연결 오류"));

        // When
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                intentName, originalQuery, null);

        // Then
        assertNotNull(result);
        assertEquals(intentName, result.getIntentType());
        assertEquals(0, result.getTotalResults());
        assertEquals("low", result.getConfidence());
    }

    @Test
    void testChatMessageWithShoppingData() {
        // Given
        String message = "아이폰 케이스 찾아줘";
        
        ChatMessage.AnalysisInfo analysisInfo = ChatMessage.AnalysisInfo.builder()
                .engine("dialogflow")
                .intentName("product_search")
                .originalIntentName("product_search")
                .originalIntentScore(0.95f)
                .build();
        
        ChatMessage.ShoppingData shoppingData = ChatMessage.ShoppingData.builder()
                .intentType("product_search")
                .originalQuery(message)
                .totalResults(2)
                .searchTime(LocalDateTime.now().toString())
                .confidence("high")
                .build();

        // When
        String sessionId = "test-session-" + System.currentTimeMillis();
        String userId = "test-user";
        chatService.saveMessageWithShoppingData(
                sessionId, userId, "bot", message, "ko", analysisInfo, shoppingData);

        // Then
        Optional<ChatMessage> savedMessage = chatService.findLatestMessageBySessionId(sessionId);
        assertTrue(savedMessage.isPresent());
        
        ChatMessage messageEntity = savedMessage.get();
        assertEquals(sessionId, messageEntity.getSessionId());
        assertEquals(userId, messageEntity.getUserId());
        assertEquals("bot", messageEntity.getSender());
        assertEquals(message, messageEntity.getMessage());
        assertNotNull(messageEntity.getAnalysisInfo());
        assertNotNull(messageEntity.getShoppingData());
        assertEquals("product_search", messageEntity.getShoppingData().getIntentType());
    }
}
