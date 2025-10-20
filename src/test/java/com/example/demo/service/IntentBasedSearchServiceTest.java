package com.example.demo.service;

import com.example.demo.model.ChatMessage;
import com.example.navershopping.entity.NaverShoppingItem;
import com.example.navershopping.service.NaverShoppingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 의도 기반 검색 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
class IntentBasedSearchServiceTest {

    @Mock
    private NaverShoppingService naverShoppingService;

    @InjectMocks
    private IntentBasedSearchService intentBasedSearchService;

    private List<NaverShoppingItem> mockProducts;

    @BeforeEach
    void setUp() {
        // 테스트용 상품 데이터 생성
        mockProducts = Arrays.asList(
                NaverShoppingItem.builder()
                        .productId("test-product-1")
                        .title("아이폰 15 케이스")
                        .link("https://example.com/product1")
                        .image("https://example.com/image1.jpg")
                        .lprice(15000)
                        .hprice(20000)
                        .mallName("테스트몰1")
                        .brand("Apple")
                        .category1("디지털/가전")
                        .category2("휴대폰")
                        .category3("케이스")
                        .searchQuery("아이폰 케이스")
                        .lastSearchedAt(LocalDateTime.now())
                        .searchCount(5)
                        .build(),
                NaverShoppingItem.builder()
                        .productId("test-product-2")
                        .title("아이폰 14 케이스")
                        .link("https://example.com/product2")
                        .image("https://example.com/image2.jpg")
                        .lprice(12000)
                        .hprice(18000)
                        .mallName("테스트몰2")
                        .brand("Samsung")
                        .category1("디지털/가전")
                        .category2("휴대폰")
                        .category3("케이스")
                        .searchQuery("아이폰 케이스")
                        .lastSearchedAt(LocalDateTime.now())
                        .searchCount(3)
                        .build(),
                NaverShoppingItem.builder()
                        .productId("test-product-3")
                        .title("갤럭시 케이스")
                        .link("https://example.com/product3")
                        .image("https://example.com/image3.jpg")
                        .lprice(10000)
                        .hprice(15000)
                        .mallName("테스트몰3")
                        .brand("Samsung")
                        .category1("디지털/가전")
                        .category2("휴대폰")
                        .category3("케이스")
                        .searchQuery("갤럭시 케이스")
                        .lastSearchedAt(LocalDateTime.now())
                        .searchCount(1)
                        .build()
        );
    }

    @Test
    void testProductSearchIntent() {
        // Given
        String intentName = "product_search";
        String originalQuery = "아이폰 케이스";
        Map<String, Object> parameters = new HashMap<>();
        
        when(naverShoppingService.getSavedProductsByQuery(originalQuery))
                .thenReturn(mockProducts.subList(0, 2));

        // When
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                intentName, originalQuery, parameters);

        // Then
        assertNotNull(result);
        assertEquals(intentName, result.getIntentType());
        assertEquals(originalQuery, result.getOriginalQuery());
        assertEquals(2, result.getTotalResults());
        assertNotNull(result.getProducts());
        assertEquals(2, result.getProducts().size());
        
        // 첫 번째 상품 검증
        ChatMessage.ProductInfo firstProduct = result.getProducts().get(0);
        assertEquals("test-product-1", firstProduct.getProductId());
        assertEquals("아이폰 15 케이스", firstProduct.getTitle());
        assertEquals(15000, firstProduct.getLprice());
        assertEquals("Apple", firstProduct.getBrand());
        
        verify(naverShoppingService).getSavedProductsByQuery(originalQuery);
    }

    @Test
    void testProductRecommendationIntent() {
        // Given
        String intentName = "product_recommendation";
        String originalQuery = "아이폰 케이스 추천해줘";
        Map<String, Object> parameters = new HashMap<>();
        
        when(naverShoppingService.getSavedProductsByQuery(originalQuery))
                .thenReturn(mockProducts);

        // When
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                intentName, originalQuery, parameters);

        // Then
        assertNotNull(result);
        assertEquals(intentName, result.getIntentType());
        assertEquals(originalQuery, result.getOriginalQuery());
        assertEquals(3, result.getTotalResults());
        
        // 검색 횟수 기준으로 정렬되었는지 확인 (searchCount 내림차순)
        List<ChatMessage.ProductInfo> products = result.getProducts();
        assertTrue(products.get(0).getSearchCount() >= products.get(1).getSearchCount());
        assertTrue(products.get(1).getSearchCount() >= products.get(2).getSearchCount());
    }

    @Test
    void testProductFilterIntent() {
        // Given
        String intentName = "product_filter";
        String originalQuery = "아이폰 케이스";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("minPrice", 10000);
        parameters.put("maxPrice", 15000);
        parameters.put("brand", "Apple");
        
        when(naverShoppingService.getSavedProductsByQuery(originalQuery))
                .thenReturn(mockProducts);

        // When
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                intentName, originalQuery, parameters);

        // Then
        assertNotNull(result);
        assertEquals(intentName, result.getIntentType());
        
        // 필터링된 결과 검증 (가격 범위와 브랜드 필터)
        List<ChatMessage.ProductInfo> products = result.getProducts();
        for (ChatMessage.ProductInfo product : products) {
            assertTrue(product.getLprice() >= 10000);
            assertTrue(product.getLprice() <= 15000);
            assertEquals("Apple", product.getBrand());
        }
    }

    @Test
    void testProductCompareIntent() {
        // Given
        String intentName = "product_compare";
        String originalQuery = "아이폰 케이스 비교";
        Map<String, Object> parameters = new HashMap<>();
        
        when(naverShoppingService.getSavedProductsByQuery(originalQuery))
                .thenReturn(mockProducts);

        // When
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                intentName, originalQuery, parameters);

        // Then
        assertNotNull(result);
        assertEquals(intentName, result.getIntentType());
        assertEquals(3, result.getTotalResults());
        
        // 가격 기준으로 정렬되었는지 확인
        List<ChatMessage.ProductInfo> products = result.getProducts();
        assertTrue(products.get(0).getLprice() <= products.get(1).getLprice());
        assertTrue(products.get(1).getLprice() <= products.get(2).getLprice());
    }

    @Test
    void testNoProductsFound() {
        // Given
        String intentName = "product_search";
        String originalQuery = "존재하지 않는 상품";
        Map<String, Object> parameters = new HashMap<>();
        
        when(naverShoppingService.getSavedProductsByQuery(originalQuery))
                .thenReturn(Arrays.asList());
        when(naverShoppingService.getSavedProductsByTitle(originalQuery))
                .thenReturn(Arrays.asList());

        // When
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                intentName, originalQuery, parameters);

        // Then
        assertNotNull(result);
        assertEquals(intentName, result.getIntentType());
        assertEquals(0, result.getTotalResults());
        assertTrue(result.getProducts().isEmpty());
    }

    @Test
    void testUnknownIntent() {
        // Given
        String intentName = "unknown_intent";
        String originalQuery = "테스트 메시지";
        Map<String, Object> parameters = new HashMap<>();
        
        when(naverShoppingService.getSavedProductsByQuery(originalQuery))
                .thenReturn(mockProducts.subList(0, 1));

        // When
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                intentName, originalQuery, parameters);

        // Then
        assertNotNull(result);
        assertEquals(intentName, result.getIntentType());
        assertEquals(1, result.getTotalResults());
    }

    @Test
    void testExceptionHandling() {
        // Given
        String intentName = "product_search";
        String originalQuery = "테스트 쿼리";
        Map<String, Object> parameters = new HashMap<>();
        
        when(naverShoppingService.getSavedProductsByQuery(anyString()))
                .thenThrow(new RuntimeException("데이터베이스 오류"));

        // When
        ChatMessage.ShoppingData result = intentBasedSearchService.searchProductsByIntent(
                intentName, originalQuery, parameters);

        // Then
        assertNotNull(result);
        assertEquals(intentName, result.getIntentType());
        assertEquals(0, result.getTotalResults());
        assertEquals("low", result.getConfidence());
    }
}

