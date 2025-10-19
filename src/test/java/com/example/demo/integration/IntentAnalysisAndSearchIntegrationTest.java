package com.example.demo.integration;

import com.example.demo.dto.MessageRequest;
import com.example.demo.kafka.MessageConsumer;
import com.example.demo.model.ChatMessage;
import com.example.demo.service.ChatService;
import com.example.demo.service.IntentBasedSearchService;
import com.example.demo.utils.IntentTestDataGenerator;
import com.example.navershopping.entity.NaverShoppingItem;
import com.example.navershopping.service.NaverShoppingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 의도 분석과 데이터베이스 검색 통합 테스트
 * 실제 시나리오를 시뮬레이션하여 전체 흐름을 검증
 */
@SpringBootTest
@ActiveProfiles("test")
class IntentAnalysisAndSearchIntegrationTest {

    @Autowired
    private IntentBasedSearchService intentBasedSearchService;

    @Autowired
    private ChatService chatService;

    @MockBean
    private NaverShoppingService naverShoppingService;

    private List<NaverShoppingItem> testProducts;

    @BeforeEach
    void setUp() {
        testProducts = IntentTestDataGenerator.generateTestProducts();
    }

    @Test
    void testCompleteShoppingFlow_IphoneCaseSearch() {
        // Given: 사용자가 "아이폰 케이스 찾아줘"라고 입력
        String userMessage = "아이폰 케이스 찾아줘";
        String expectedIntent = IntentTestDataGenerator.IntentSimulation.simulateIntentDetection(userMessage);
        
        // Mock 설정: 아이폰 케이스 관련 상품들 반환
        List<NaverShoppingItem> iphoneCases = testProducts.stream()
                .filter(item -> item.getTitle().contains("아이폰") && item.getTitle().contains("케이스"))
                .toList();
        
        when(naverShoppingService.getSavedProductsByQuery(userMessage))
                .thenReturn(iphoneCases);

        // When: 의도 기반 검색 실행
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                expectedIntent, userMessage, null);

        // Then: 검색 결과 검증
        assertNotNull(result);
        assertEquals(expectedIntent, result.getIntentType());
        assertEquals(userMessage, result.getOriginalQuery());
        assertTrue(result.getTotalResults() > 0);
        
        // 아이폰 케이스만 검색되었는지 확인
        List<ChatMessage.ProductInfo> products = result.getProducts();
        for (ChatMessage.ProductInfo product : products) {
            assertTrue(product.getTitle().contains("아이폰"));
            assertTrue(product.getTitle().contains("케이스"));
        }
        
        verify(naverShoppingService).getSavedProductsByQuery(userMessage);
    }

    @Test
    void testCompleteShoppingFlow_ProductRecommendation() {
        // Given: 사용자가 "좋은 노트북 추천해주세요"라고 입력
        String userMessage = "좋은 노트북 추천해주세요";
        String expectedIntent = IntentTestDataGenerator.IntentSimulation.simulateIntentDetection(userMessage);
        
        // Mock 설정: 노트북 관련 상품들 반환
        List<NaverShoppingItem> laptops = testProducts.stream()
                .filter(item -> item.getCategory3().equals("노트북"))
                .toList();
        
        when(naverShoppingService.getSavedProductsByQuery(userMessage))
                .thenReturn(laptops);

        // When: 의도 기반 검색 실행
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                expectedIntent, userMessage, null);

        // Then: 추천 결과 검증
        assertNotNull(result);
        assertEquals(expectedIntent, result.getIntentType());
        assertTrue(result.getTotalResults() > 0);
        
        // 검색 횟수 기준으로 정렬되었는지 확인 (추천 로직)
        List<ChatMessage.ProductInfo> products = result.getProducts();
        if (products.size() > 1) {
            assertTrue(products.get(0).getSearchCount() >= products.get(1).getSearchCount());
        }
    }

    @Test
    void testCompleteShoppingFlow_ProductFilter() {
        // Given: 사용자가 "5만원 이하 아이폰 케이스"라고 입력
        String userMessage = "5만원 이하 아이폰 케이스";
        String expectedIntent = IntentTestDataGenerator.IntentSimulation.simulateIntentDetection(userMessage);
        
        // 필터링 파라미터 설정
        Map<String, Object> parameters = Map.of(
                "minPrice", 0,
                "maxPrice", 50000,
                "brand", "Apple"
        );
        
        // Mock 설정: 모든 아이폰 케이스 반환
        List<NaverShoppingItem> allIphoneCases = testProducts.stream()
                .filter(item -> item.getTitle().contains("아이폰") && item.getTitle().contains("케이스"))
                .toList();
        
        when(naverShoppingService.getSavedProductsByQuery(userMessage))
                .thenReturn(allIphoneCases);

        // When: 의도 기반 검색 실행
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                expectedIntent, userMessage, parameters);

        // Then: 필터링 결과 검증
        assertNotNull(result);
        assertEquals(expectedIntent, result.getIntentType());
        
        // 가격 필터링이 적용되었는지 확인
        List<ChatMessage.ProductInfo> products = result.getProducts();
        for (ChatMessage.ProductInfo product : products) {
            assertTrue(product.getLprice() <= 50000);
            assertEquals("Apple", product.getBrand());
        }
    }

    @Test
    void testCompleteShoppingFlow_ProductCompare() {
        // Given: 사용자가 "아이폰 케이스 비교해줘"라고 입력
        String userMessage = "아이폰 케이스 비교해줘";
        String expectedIntent = IntentTestDataGenerator.IntentSimulation.simulateIntentDetection(userMessage);
        
        // Mock 설정: 아이폰 케이스들 반환
        List<NaverShoppingItem> iphoneCases = testProducts.stream()
                .filter(item -> item.getTitle().contains("아이폰") && item.getTitle().contains("케이스"))
                .toList();
        
        when(naverShoppingService.getSavedProductsByQuery(userMessage))
                .thenReturn(iphoneCases);

        // When: 의도 기반 검색 실행
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                expectedIntent, userMessage, null);

        // Then: 비교 결과 검증
        assertNotNull(result);
        assertEquals(expectedIntent, result.getIntentType());
        assertTrue(result.getTotalResults() > 0);
        
        // 가격 기준으로 정렬되었는지 확인 (비교 로직)
        List<ChatMessage.ProductInfo> products = result.getProducts();
        if (products.size() > 1) {
            assertTrue(products.get(0).getLprice() <= products.get(1).getLprice());
        }
    }

    @Test
    void testCompleteShoppingFlow_NoResults() {
        // Given: 존재하지 않는 상품 검색
        String userMessage = "존재하지 않는 상품";
        String expectedIntent = IntentTestDataGenerator.IntentSimulation.simulateIntentDetection(userMessage);
        
        // Mock 설정: 빈 결과 반환
        when(naverShoppingService.getSavedProductsByQuery(userMessage))
                .thenReturn(List.of());
        when(naverShoppingService.getSavedProductsByTitle(userMessage))
                .thenReturn(List.of());

        // When: 의도 기반 검색 실행
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                expectedIntent, userMessage, null);

        // Then: 빈 결과 검증
        assertNotNull(result);
        assertEquals(expectedIntent, result.getIntentType());
        assertEquals(0, result.getTotalResults());
        assertTrue(result.getProducts().isEmpty());
    }

    @Test
    void testCompleteShoppingFlow_GeneralChat() {
        // Given: 쇼핑과 무관한 일반 대화
        String userMessage = "안녕하세요";
        String expectedIntent = IntentTestDataGenerator.IntentSimulation.simulateIntentDetection(userMessage);
        
        // When: 의도 기반 검색 실행
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                expectedIntent, userMessage, null);

        // Then: 일반 대화는 상품 검색하지 않음
        assertNotNull(result);
        assertEquals(expectedIntent, result.getIntentType());
        assertEquals(0, result.getTotalResults());
        assertTrue(result.getProducts().isEmpty());
    }

    @Test
    void testChatMessageStorageWithShoppingData() {
        // Given: 채팅 메시지와 쇼핑 데이터
        String sessionId = "test-session-" + System.currentTimeMillis();
        String userId = "test-user";
        String message = "아이폰 케이스 찾아줘";
        
        ChatMessage.AnalysisInfo analysisInfo = ChatMessage.AnalysisInfo.builder()
                .engine("dialogflow")
                .intentName("product_search")
                .originalIntentName("product_search")
                .originalIntentScore(0.95f)
                .build();
        
        ChatMessage.ShoppingData shoppingData = IntentTestDataGenerator.generateTestShoppingData(
                "product_search", message);

        // When: 쇼핑 데이터와 함께 메시지 저장
        chatService.saveMessageWithShoppingData(
                sessionId, userId, "bot", message, "ko", analysisInfo, shoppingData);

        // Then: 저장된 메시지 검증
        Optional<ChatMessage> savedMessage = chatService.findLatestMessageBySessionId(sessionId);
        assertTrue(savedMessage.isPresent());
        
        ChatMessage messageEntity = savedMessage.get();
        assertEquals(sessionId, messageEntity.getSessionId());
        assertEquals("bot", messageEntity.getSender());
        assertEquals(message, messageEntity.getMessage());
        
        // 분석 정보 검증
        assertNotNull(messageEntity.getAnalysisInfo());
        assertEquals("product_search", messageEntity.getAnalysisInfo().getIntentName());
        assertEquals(0.95f, messageEntity.getAnalysisInfo().getOriginalIntentScore());
        
        // 쇼핑 데이터 검증
        assertNotNull(messageEntity.getShoppingData());
        assertEquals("product_search", messageEntity.getShoppingData().getIntentType());
        assertEquals(message, messageEntity.getShoppingData().getOriginalQuery());
        assertTrue(messageEntity.getShoppingData().getTotalResults() > 0);
    }

    @Test
    void testMultipleIntentScenarios() {
        // 다양한 의도 시나리오 테스트
        Map<String, String> testScenarios = Map.of(
                "아이폰 케이스 찾아줘", "product_search",
                "좋은 노트북 추천해주세요", "product_recommendation", 
                "5만원 이하 운동화", "product_filter",
                "이어폰 비교해줘", "product_compare",
                "안녕하세요", "general_chat"
        );

        for (Map.Entry<String, String> scenario : testScenarios.entrySet()) {
            String userMessage = scenario.getKey();
            String expectedIntent = scenario.getValue();
            
            // Mock 설정
            if (!expectedIntent.equals("general_chat")) {
                when(naverShoppingService.getSavedProductsByQuery(userMessage))
                        .thenReturn(testProducts.subList(0, 2));
            }

            // When
            ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                    expectedIntent, userMessage, null);

            // Then
            assertNotNull(result);
            assertEquals(expectedIntent, result.getIntentType());
            assertEquals(userMessage, result.getOriginalQuery());
            
            if (expectedIntent.equals("general_chat")) {
                assertEquals(0, result.getTotalResults());
            } else {
                assertTrue(result.getTotalResults() >= 0);
            }
        }
    }
}

