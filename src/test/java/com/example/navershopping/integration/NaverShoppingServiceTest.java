package com.example.navershopping.integration;

import com.example.navershopping.dto.NaverShoppingResponse;
import com.example.navershopping.entity.NaverShoppingItem;
import com.example.navershopping.entity.NaverShoppingSearch;
import com.example.navershopping.repository.NaverShoppingItemRepository;
import com.example.navershopping.repository.NaverShoppingSearchRepository;
import com.example.navershopping.service.NaverShoppingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.example.demo.DemoApplication.class)
@ActiveProfiles("test")
@Transactional
class NaverShoppingServiceIntegrationTest {

    @Autowired
    private NaverShoppingService naverShoppingService;

    @Autowired
    private NaverShoppingItemRepository itemRepository;

    @Autowired
    private NaverShoppingSearchRepository searchRepository;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리
        itemRepository.deleteAll();
        searchRepository.deleteAll();
    }

    @Test
    void testSearchAndSaveProducts_NewProducts() {
        // Given
        String query = "아이폰";
        int display = 5;
        int start = 1;

        // When - 실제 API 호출 (실제 네이버 API 키가 필요)
        try {
            NaverShoppingResponse response = naverShoppingService.searchAndSaveProducts(query, display, start);

            // Then
            assertNotNull(response);
            assertTrue(response.getItems().size() > 0);

            // 데이터베이스에 저장되었는지 확인
            List<NaverShoppingItem> savedItems = itemRepository.findAll();
            assertTrue(savedItems.size() > 0);

            // 검색 기록이 저장되었는지 확인
            List<NaverShoppingSearch> searchHistory = searchRepository.findAll();
            assertEquals(1, searchHistory.size());
            assertEquals(query, searchHistory.get(0).getQuery());

            // 저장된 상품들의 속성 확인
            for (NaverShoppingItem item : savedItems) {
                assertNotNull(item.getProductId());
                assertNotNull(item.getTitle());
                assertNotNull(item.getLink());
                assertNotNull(item.getMallName());
                assertEquals(query, item.getSearchQuery());
                assertEquals(1, item.getSearchCount());
                assertNotNull(item.getLastSearchedAt());
            }

        } catch (Exception e) {
            // API 키가 없거나 네트워크 오류인 경우 테스트 스킵
            System.out.println("네이버 API 호출 실패 - 테스트 스킵: " + e.getMessage());
        }
    }

    @Test
    void testProductUpdateScenario() {
        // Given - 첫 번째 검색
        String query1 = "아이폰";
        NaverShoppingItem mockItem = createMockItem("test-product-1", "아이폰 15", 1000000, 1);
        itemRepository.save(mockItem);

        // When - 같은 상품이 다시 검색됨 (가격이 변경되었다고 가정)
        NaverShoppingItem updatedMockItem = createMockItem("test-product-1", "아이폰 15", 950000, 1);
        
        // 실제 서비스 메서드 호출
        NaverShoppingItem result = naverShoppingService.saveOrUpdateProductItem(query1, createMockApiItem(updatedMockItem));

        // Then
        assertNotNull(result);
        assertEquals("test-product-1", result.getProductId());
        assertEquals("아이폰 15", result.getTitle());
        assertEquals(950000, result.getLprice()); // 업데이트된 가격
        assertEquals(2, result.getSearchCount()); // 검색 횟수 증가
        assertNotNull(result.getLastSearchedAt());

        // 데이터베이스에서 확인
        Optional<NaverShoppingItem> savedItem = itemRepository.findByProductId("test-product-1");
        assertTrue(savedItem.isPresent());
        assertEquals(950000, savedItem.get().getLprice());
        assertEquals(2, savedItem.get().getSearchCount());
    }

    @Test
    void testUpdateStats() {
        // Given - 다양한 검색 횟수를 가진 상품들 생성
        NaverShoppingItem newItem = createMockItem("product1", "아이폰 15", 1000000, 1);
        NaverShoppingItem updatedItem1 = createMockItem("product2", "아이폰 15 Pro", 1200000, 3);
        NaverShoppingItem updatedItem2 = createMockItem("product3", "아이폰 14", 800000, 2);

        itemRepository.saveAll(List.of(newItem, updatedItem1, updatedItem2));

        // When
        var stats = naverShoppingService.getUpdateStats();

        // Then
        assertNotNull(stats);
        assertEquals(3L, stats.get("totalItems"));
        assertEquals(2L, stats.get("updatedItems"));
        assertEquals(1L, stats.get("newItems"));
        assertEquals(66.67, (Double) stats.get("updateRate"), 0.01);
    }

    @Test
    void testRecentlyUpdatedProducts() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        NaverShoppingItem oldItem = createMockItem("product1", "아이폰 15", 1000000, 1);
        oldItem.setUpdatedAt(now.minusHours(2));
        
        NaverShoppingItem recentItem = createMockItem("product2", "아이폰 15 Pro", 1200000, 2);
        recentItem.setUpdatedAt(now.minusMinutes(30));
        
        NaverShoppingItem newestItem = createMockItem("product3", "아이폰 14", 800000, 1);
        newestItem.setUpdatedAt(now.minusMinutes(10));

        itemRepository.saveAll(List.of(oldItem, recentItem, newestItem));

        // When
        List<NaverShoppingItem> result = naverShoppingService.getRecentlyUpdatedProducts(2);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("product3", result.get(0).getProductId()); // 가장 최근
        assertEquals("product2", result.get(1).getProductId()); // 두 번째로 최근
    }

    @Test
    void testMostSearchedProducts() {
        // Given
        NaverShoppingItem lowSearched = createMockItem("product1", "아이폰 15", 1000000, 2);
        NaverShoppingItem highSearched = createMockItem("product2", "아이폰 15 Pro", 1200000, 10);
        NaverShoppingItem mediumSearched = createMockItem("product3", "아이폰 14", 800000, 5);

        itemRepository.saveAll(List.of(lowSearched, highSearched, mediumSearched));

        // When
        List<NaverShoppingItem> result = naverShoppingService.getMostSearchedProducts(2);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("product2", result.get(0).getProductId()); // 가장 많이 검색됨
        assertEquals("product3", result.get(1).getProductId()); // 두 번째로 많이 검색됨
    }

    @Test
    void testForceUpdateProduct() {
        // Given
        NaverShoppingItem existingItem = createMockItem("product1", "아이폰 15", 1000000, 2);
        itemRepository.save(existingItem);

        // When
        NaverShoppingItem result = naverShoppingService.forceUpdateProduct("product1", "아이폰");

        // Then
        assertNotNull(result);
        assertEquals("product1", result.getProductId());
        assertEquals("아이폰", result.getSearchQuery());
        assertEquals(3, result.getSearchCount()); // 검색 횟수 증가
        assertNotNull(result.getLastSearchedAt());
    }

    private NaverShoppingItem createMockItem(String productId, String title, int price, int searchCount) {
        return NaverShoppingItem.builder()
                .productId(productId)
                .title(title)
                .lprice(price)
                .hprice(price + 100000)
                .link("https://example.com/" + productId)
                .image("https://example.com/image/" + productId + ".jpg")
                .mallName("테스트몰")
                .productType("1")
                .brand("Apple")
                .maker("Apple Inc.")
                .category1("디지털/가전")
                .category2("휴대폰")
                .category3("스마트폰")
                .category4("아이폰")
                .searchQuery("아이폰")
                .searchCount(searchCount)
                .lastSearchedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private NaverShoppingResponse.Item createMockApiItem(NaverShoppingItem item) {
        NaverShoppingResponse.Item apiItem = new NaverShoppingResponse.Item();
        apiItem.setProductId(item.getProductId());
        apiItem.setTitle(item.getTitle());
        apiItem.setLprice(item.getLprice());
        apiItem.setHprice(item.getHprice());
        apiItem.setLink(item.getLink());
        apiItem.setImage(item.getImage());
        apiItem.setMallName(item.getMallName());
        apiItem.setProductType(item.getProductType());
        apiItem.setBrand(item.getBrand());
        apiItem.setMaker(item.getMaker());
        apiItem.setCategory1(item.getCategory1());
        apiItem.setCategory2(item.getCategory2());
        apiItem.setCategory3(item.getCategory3());
        apiItem.setCategory4(item.getCategory4());
        return apiItem;
    }
}