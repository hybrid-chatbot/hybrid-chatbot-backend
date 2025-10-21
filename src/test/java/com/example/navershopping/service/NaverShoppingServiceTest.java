package com.example.navershopping.service;

import com.example.navershopping.dto.NaverShoppingResponse;
import com.example.navershopping.entity.NaverShoppingItem;
import com.example.navershopping.repository.NaverShoppingItemRepository;
import com.example.navershopping.repository.NaverShoppingSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NaverShoppingServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private NaverShoppingItemRepository itemRepository;

    @Mock
    private NaverShoppingSearchRepository searchRepository;

    @InjectMocks
    private NaverShoppingService naverShoppingService;

    @BeforeEach
    void setUp() {
        // @Value 어노테이션으로 주입되는 값들을 설정
        ReflectionTestUtils.setField(naverShoppingService, "clientId", "test-client-id");
        ReflectionTestUtils.setField(naverShoppingService, "clientSecret", "test-client-secret");
    }

    @Test
    void testSaveOrUpdateProductItem_NewProduct() {
        // Given
        String searchQuery = "아이폰";
        NaverShoppingResponse.Item apiItem = createTestApiItem("product1", "아이폰 15", 1000000);
        
        when(itemRepository.findByProductId("product1")).thenReturn(Optional.empty());
        when(itemRepository.save(any(NaverShoppingItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NaverShoppingItem result = naverShoppingService.saveOrUpdateProductItem(searchQuery, apiItem);

        // Then
        assertNotNull(result);
        assertEquals("product1", result.getProductId());
        assertEquals("아이폰 15", result.getTitle());
        assertEquals(1000000, result.getLprice());
        assertEquals(searchQuery, result.getSearchQuery());
        assertEquals(1, result.getSearchCount());
        assertNotNull(result.getLastSearchedAt());

        verify(itemRepository).findByProductId("product1");
        verify(itemRepository).save(any(NaverShoppingItem.class));
    }

    @Test
    void testSaveOrUpdateProductItem_ExistingProduct() {
        // Given
        String searchQuery = "아이폰";
        NaverShoppingResponse.Item apiItem = createTestApiItem("product1", "아이폰 15 Pro", 1200000);
        
        NaverShoppingItem existingItem = createExistingItem("product1", "아이폰 15", 1000000, 2);
        
        when(itemRepository.findByProductId("product1")).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(NaverShoppingItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NaverShoppingItem result = naverShoppingService.saveOrUpdateProductItem(searchQuery, apiItem);

        // Then
        assertNotNull(result);
        assertEquals("product1", result.getProductId());
        assertEquals("아이폰 15 Pro", result.getTitle()); // 업데이트된 제목
        assertEquals(1200000, result.getLprice()); // 업데이트된 가격
        assertEquals(searchQuery, result.getSearchQuery());
        assertEquals(3, result.getSearchCount()); // 검색 횟수 증가
        assertNotNull(result.getLastSearchedAt());

        verify(itemRepository).findByProductId("product1");
        verify(itemRepository).save(existingItem);
    }

    @Test
    void testSaveProductItems() {
        // Given
        String searchQuery = "아이폰";
        List<NaverShoppingResponse.Item> apiItems = Arrays.asList(
            createTestApiItem("product1", "아이폰 15", 1000000),
            createTestApiItem("product2", "아이폰 15 Pro", 1200000)
        );

        when(itemRepository.findByProductId(anyString())).thenReturn(Optional.empty());
        when(itemRepository.save(any(NaverShoppingItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<NaverShoppingItem> result = naverShoppingService.saveProductItems(searchQuery, apiItems);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        verify(itemRepository, times(2)).findByProductId(anyString());
        verify(itemRepository, times(2)).save(any(NaverShoppingItem.class));
    }

    @Test
    void testGetUpdateStats() {
        // Given
        List<NaverShoppingItem> allItems = Arrays.asList(
            createExistingItem("product1", "아이폰 15", 1000000, 1), // 새 상품
            createExistingItem("product2", "아이폰 15 Pro", 1200000, 3), // 업데이트된 상품
            createExistingItem("product3", "아이폰 14", 800000, 2) // 업데이트된 상품
        );

        when(itemRepository.findAll()).thenReturn(allItems);

        // When
        var result = naverShoppingService.getUpdateStats();

        // Then
        assertNotNull(result);
        assertEquals(3L, result.get("totalItems"));
        assertEquals(2L, result.get("updatedItems"));
        assertEquals(1L, result.get("newItems"));
        assertEquals(66.67, (Double) result.get("updateRate"), 0.01);
    }

    @Test
    void testGetRecentlyUpdatedProducts() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        NaverShoppingItem item1 = createExistingItem("product1", "아이폰 15", 1000000, 1);
        item1.setUpdatedAt(now.minusHours(1));
        
        NaverShoppingItem item2 = createExistingItem("product2", "아이폰 15 Pro", 1200000, 2);
        item2.setUpdatedAt(now.minusMinutes(30));
        
        NaverShoppingItem item3 = createExistingItem("product3", "아이폰 14", 800000, 1);
        item3.setUpdatedAt(now.minusHours(2));

        List<NaverShoppingItem> allItems = Arrays.asList(item1, item2, item3);
        when(itemRepository.findAll()).thenReturn(allItems);

        // When
        List<NaverShoppingItem> result = naverShoppingService.getRecentlyUpdatedProducts(2);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("product2", result.get(0).getProductId()); // 가장 최근 업데이트
        assertEquals("product1", result.get(1).getProductId()); // 두 번째로 최근 업데이트
    }

    @Test
    void testGetMostSearchedProducts() {
        // Given
        NaverShoppingItem item1 = createExistingItem("product1", "아이폰 15", 1000000, 5);
        NaverShoppingItem item2 = createExistingItem("product2", "아이폰 15 Pro", 1200000, 10);
        NaverShoppingItem item3 = createExistingItem("product3", "아이폰 14", 800000, 2);

        List<NaverShoppingItem> allItems = Arrays.asList(item1, item2, item3);
        when(itemRepository.findAll()).thenReturn(allItems);

        // When
        List<NaverShoppingItem> result = naverShoppingService.getMostSearchedProducts(2);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("product2", result.get(0).getProductId()); // 가장 많이 검색됨
        assertEquals("product1", result.get(1).getProductId()); // 두 번째로 많이 검색됨
    }

    @Test
    void testForceUpdateProduct() {
        // Given
        String productId = "product1";
        String searchQuery = "아이폰";
        NaverShoppingItem existingItem = createExistingItem(productId, "아이폰 15", 1000000, 2);
        
        when(itemRepository.findByProductId(productId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(NaverShoppingItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NaverShoppingItem result = naverShoppingService.forceUpdateProduct(productId, searchQuery);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals(searchQuery, result.getSearchQuery());
        assertEquals(3, result.getSearchCount()); // 검색 횟수 증가
        assertNotNull(result.getLastSearchedAt());

        verify(itemRepository).findByProductId(productId);
        verify(itemRepository).save(existingItem);
    }

    @Test
    void testForceUpdateProduct_NotFound() {
        // Given
        String productId = "nonexistent";
        String searchQuery = "아이폰";
        
        when(itemRepository.findByProductId(productId)).thenReturn(Optional.empty());

        // When
        NaverShoppingItem result = naverShoppingService.forceUpdateProduct(productId, searchQuery);

        // Then
        assertNull(result);
        verify(itemRepository).findByProductId(productId);
        verify(itemRepository, never()).save(any());
    }

    private NaverShoppingResponse.Item createTestApiItem(String productId, String title, int price) {
        NaverShoppingResponse.Item item = new NaverShoppingResponse.Item();
        item.setProductId(productId);
        item.setTitle(title);
        item.setLprice(price);
        item.setHprice(price + 100000);
        item.setLink("https://example.com/" + productId);
        item.setImage("https://example.com/image/" + productId + ".jpg");
        item.setMallName("테스트몰");
        item.setProductType("1");
        item.setBrand("Apple");
        item.setMaker("Apple Inc.");
        item.setCategory1("디지털/가전");
        item.setCategory2("휴대폰");
        item.setCategory3("스마트폰");
        item.setCategory4("아이폰");
        return item;
    }

    private NaverShoppingItem createExistingItem(String productId, String title, int price, int searchCount) {
        return NaverShoppingItem.builder()
                .id(1L)
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
}