package com.example.demo.service;

import com.example.demo.dto.ShoppingMessageResponse;
import com.example.navershopping.entity.NaverShoppingItem;
import com.example.navershopping.repository.NaverShoppingItemRepository;
import com.example.navershopping.service.NaverShoppingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SimpleShoppingService 테스트 클래스
 * 
 * 쇼핑 서비스의 주요 기능들을 테스트합니다:
 * - 키워드 검색
 * - 브랜드별 검색
 * - 카테고리별 검색
 * - 가격 범위 검색
 * - 인기 상품 조회
 * - 최신 상품 조회
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SimpleShoppingServiceTest {

    @Mock
    private NaverShoppingItemRepository itemRepository;
    
    @Mock
    private NaverShoppingService naverShoppingService;
    
    @Mock
    private AiServerService aiServerService;
    
    @Mock
    private SearchExecutorService searchExecutorService;
    
    @Mock
    private ResponseBuilderService responseBuilderService;
    
    
    @InjectMocks
    private SimpleShoppingService simpleShoppingService;
    
    private NaverShoppingItem sampleProduct1;
    private NaverShoppingItem sampleProduct2;
    private List<NaverShoppingItem> sampleProducts;

    @BeforeEach
    void setUp() {
        // 테스트용 샘플 상품 데이터 생성
        sampleProduct1 = NaverShoppingItem.builder()
                .id(1L)
                .title("나이키 에어맥스 270")
                .image("https://example.com/nike1.jpg")
                .link("https://example.com/nike1")
                .lprice(150000)
                .hprice(180000)
                .mallName("나이키 공식몰")
                .brand("나이키")
                .category1("운동화")
                .category2("스니커즈")
                .productType("신발")
                .maker("나이키")
                .searchCount(10)
                .lastSearchedAt(LocalDateTime.now())
                .build();

        sampleProduct2 = NaverShoppingItem.builder()
                .id(2L)
                .title("아디다스 울트라부스트 22")
                .image("https://example.com/adidas1.jpg")
                .link("https://example.com/adidas1")
                .lprice(120000)
                .hprice(150000)
                .mallName("아디다스 공식몰")
                .brand("아디다스")
                .category1("운동화")
                .category2("러닝화")
                .productType("신발")
                .maker("아디다스")
                .searchCount(5)
                .lastSearchedAt(LocalDateTime.now().minusDays(1))
                .build();

        sampleProducts = Arrays.asList(sampleProduct1, sampleProduct2);
        
        // ResponseBuilderService 모킹 설정: 인자 기반으로 실제 메시지/카드를 생성하도록 Answer 사용
        when(responseBuilderService.createSearchResponse(anyString(), anyList(), any())).thenAnswer(invocation -> {
            String query = invocation.getArgument(0);
            List<NaverShoppingItem> items = invocation.getArgument(1);
            String sortOrder = invocation.getArgument(2);
            String responseMsg = "'" + query + "' 검색 결과 " + (items != null ? items.size() : 0) + "개의 상품을 찾았습니다.";
            List<ShoppingMessageResponse.ProductCard> cards = (items == null ? Collections.<NaverShoppingItem>emptyList() : items)
                    .stream()
                    .map(ShoppingMessageResponse::fromNaverShoppingItem)
                    .toList();
            return ShoppingMessageResponse.builder()
                    .response(responseMsg)
                    .messageType("shopping")
                    .products(cards)
                    .sortOrder(sortOrder)
                    .sortType(sortOrder != null ? "price" : null)
                    .build();
        });

        when(responseBuilderService.createRecommendationResponse(anyString(), anyList())).thenAnswer(invocation -> {
            String type = invocation.getArgument(0);
            List<NaverShoppingItem> items = invocation.getArgument(1);
            String responseMsg = type + " " + (items != null ? items.size() : 0) + "개를 찾았습니다.";
            List<ShoppingMessageResponse.ProductCard> cards = (items == null ? Collections.<NaverShoppingItem>emptyList() : items)
                    .stream()
                    .map(ShoppingMessageResponse::fromNaverShoppingItem)
                    .toList();
            // 추천 플래그 설정
            for (ShoppingMessageResponse.ProductCard c : cards) {
                c.setRecommended(true);
                c.setRecommendationReason(type);
            }
            return ShoppingMessageResponse.builder()
                    .response(responseMsg)
                    .messageType("recommendation")
                    .products(cards)
                    .build();
        });

        // 오버로드된 2-인자 버전도 스텁
        when(responseBuilderService.createSearchResponse(anyString(), anyList())).thenAnswer(invocation -> {
            String query = invocation.getArgument(0);
            List<NaverShoppingItem> items = invocation.getArgument(1);
            String responseMsg = "'" + query + "' 검색 결과 " + (items != null ? items.size() : 0) + "개의 상품을 찾았습니다.";
            List<ShoppingMessageResponse.ProductCard> cards = (items == null ? Collections.<NaverShoppingItem>emptyList() : items)
                    .stream()
                    .map(ShoppingMessageResponse::fromNaverShoppingItem)
                    .toList();
            return ShoppingMessageResponse.builder()
                    .response(responseMsg)
                    .messageType("shopping")
                    .products(cards)
                    .build();
        });

        // 기본적으로 전역 anyString 스텁은 사용하지 않음 (특정 eq 스텁만 사용)

        // 정렬 키워드 분석은 기본적으로 영향 없도록 설정 (KeywordAnalyzerService 제거됨)

        // 키워드 검색 경로별 결과 설정
        when(searchExecutorService.searchProductsInDatabase(eq("나이키 운동화")))
                .thenReturn(Arrays.asList(sampleProduct1));
        when(searchExecutorService.searchProductsInDatabase(eq("운동화")))
                .thenReturn(sampleProducts);
        // 네이버 API 호출 시나리오: 처음엔 없음 -> API 후 재조회 시 1개 반환
        when(searchExecutorService.searchProductsInDatabase(eq("새로운상품")))
                .thenReturn(Collections.emptyList())
                .thenReturn(Arrays.asList(sampleProduct1));
        // 에러 시나리오 유도
        when(searchExecutorService.searchProductsInDatabase(eq("서버테스트")))
                .thenThrow(new RuntimeException("forced-error"));

        when(responseBuilderService.createErrorResponse(anyString())).thenAnswer(invocation -> {
            String q = invocation.getArgument(0);
            return ShoppingMessageResponse.builder()
                    .response("죄송합니다. '" + q + "' 검색 중 오류가 발생했습니다.")
                    .messageType("text")
                    .products(Collections.emptyList())
                    .build();
        });
    }

    @Test
    void 키워드_검색_성공_테스트() {
        // Given: 키워드 검색 시 상품이 존재하는 경우
        String query = "나이키 운동화";
        when(searchExecutorService.searchProductsInDatabase(query))
                .thenReturn(Arrays.asList(sampleProduct1));

        // When: 키워드 검색 실행
        ShoppingMessageResponse response = simpleShoppingService.searchProducts(query);

        // Then: 검색 결과 검증
        assertNotNull(response);
        assertEquals("'나이키 운동화' 검색 결과 1개의 상품을 찾았습니다.", response.getResponse());
        assertEquals("shopping", response.getMessageType());
        assertEquals(1, response.getProducts().size());
        assertEquals("나이키 에어맥스 270", response.getProducts().get(0).getTitle());
        assertEquals("150,000원", response.getProducts().get(0).getPriceFormatted());
        
        verify(searchExecutorService, times(2)).searchProductsInDatabase(query);
    }

    @Test
    void 키워드_검색_결과없음_네이버API호출_테스트() {
        // Given: DB에 결과가 없어서 네이버 API 호출이 필요한 경우
        String query = "새로운상품";
        when(searchExecutorService.searchProductsInDatabase(query))
                .thenReturn(Collections.emptyList())
                .thenReturn(Arrays.asList(sampleProduct1));

        // When: 키워드 검색 실행
        ShoppingMessageResponse response = simpleShoppingService.searchProducts(query);

        // Then: 네이버 API 호출 및 재검색 검증
        assertNotNull(response);
        assertEquals(1, response.getProducts().size());
        verify(naverShoppingService).searchAndSaveProducts(query, 20, 1);
        verify(searchExecutorService, times(2)).searchProductsInDatabase(query);
    }

    @Test
    void 브랜드별_검색_성공_테스트() {
        // Given: 브랜드별 검색 시 상품이 존재하는 경우
        String brand = "나이키";
        when(itemRepository.findByBrand(brand))
                .thenReturn(Arrays.asList(sampleProduct1));

        // When: 브랜드별 검색 실행
        ShoppingMessageResponse response = simpleShoppingService.searchProductsByBrand(brand);

        // Then: 검색 결과 검증
        assertNotNull(response);
        assertEquals("'나이키 브랜드 상품' 검색 결과 1개의 상품을 찾았습니다.", response.getResponse());
        assertEquals(1, response.getProducts().size());
        assertEquals("나이키", response.getProducts().get(0).getBrand());
        
        verify(itemRepository).findByBrand(brand);
    }

    @Test
    void 카테고리별_검색_성공_테스트() {
        // Given: 카테고리별 검색 시 상품이 존재하는 경우
        String category = "운동화";
        when(itemRepository.findByCategory1(category))
                .thenReturn(sampleProducts);

        // When: 카테고리별 검색 실행
        ShoppingMessageResponse response = simpleShoppingService.searchProductsByCategory(category);

        // Then: 검색 결과 검증
        assertNotNull(response);
        assertEquals("'운동화 카테고리 상품' 검색 결과 2개의 상품을 찾았습니다.", response.getResponse());
        assertEquals(2, response.getProducts().size());
        
        verify(itemRepository).findByCategory1(category);
    }

    @Test
    void 가격범위_검색_성공_테스트() {
        // Given: 가격 범위 검색 시 상품이 존재하는 경우
        Integer minPrice = 100000;
        Integer maxPrice = 200000;
        when(itemRepository.findByPriceRange(minPrice, maxPrice))
                .thenReturn(sampleProducts);

        // When: 가격 범위 검색 실행
        ShoppingMessageResponse response = simpleShoppingService.searchProductsByPriceRange(minPrice, maxPrice);

        // Then: 검색 결과 검증
        assertNotNull(response);
        assertEquals("'100000원 ~ 200000원 상품' 검색 결과 2개의 상품을 찾았습니다.", response.getResponse());
        assertEquals(2, response.getProducts().size());
        
        verify(itemRepository).findByPriceRange(minPrice, maxPrice);
    }

    @Test
    void 인기상품_조회_성공_테스트() {
        // Given: 인기 상품 조회 시 상품이 존재하는 경우
        when(itemRepository.findTop10ByOrderBySearchCountDesc())
                .thenReturn(sampleProducts);

        // When: 인기 상품 조회 실행
        ShoppingMessageResponse response = simpleShoppingService.getPopularProducts();

        // Then: 조회 결과 검증
        assertNotNull(response);
        assertEquals("인기 상품 2개를 찾았습니다.", response.getResponse());
        assertEquals("recommendation", response.getMessageType());
        assertEquals(2, response.getProducts().size());
        
        // 추천 상품으로 설정되었는지 확인
        assertTrue(response.getProducts().get(0).isRecommended());
        assertEquals("인기 상품", response.getProducts().get(0).getRecommendationReason());
        
        verify(itemRepository).findTop10ByOrderBySearchCountDesc();
    }

    @Test
    void 최신상품_조회_성공_테스트() {
        // Given: 최신 상품 조회 시 상품이 존재하는 경우
        when(itemRepository.findTop10ByOrderByLastSearchedAtDesc())
                .thenReturn(sampleProducts);

        // When: 최신 상품 조회 실행
        ShoppingMessageResponse response = simpleShoppingService.getRecentProducts();

        // Then: 조회 결과 검증
        assertNotNull(response);
        assertEquals("최신 상품 2개를 찾았습니다.", response.getResponse());
        assertEquals("recommendation", response.getMessageType());
        assertEquals(2, response.getProducts().size());
        
        verify(itemRepository).findTop10ByOrderByLastSearchedAtDesc();
    }

    @Test
    void 상품상세정보_조회_성공_테스트() {
        // Given: 상품 상세 정보 조회 시 상품이 존재하는 경우
        Long productId = 1L;
        when(itemRepository.findById(productId))
                .thenReturn(Optional.of(sampleProduct1));

        // When: 상품 상세 정보 조회 실행
        NaverShoppingItem result = simpleShoppingService.getProductDetail(productId);

        // Then: 조회 결과 검증
        assertNotNull(result);
        assertEquals(sampleProduct1.getId(), result.getId());
        assertEquals(sampleProduct1.getTitle(), result.getTitle());
        
        verify(itemRepository).findById(productId);
    }

    @Test
    void 상품상세정보_조회_없음_테스트() {
        // Given: 상품이 존재하지 않는 경우
        Long productId = 999L;
        when(itemRepository.findById(productId))
                .thenReturn(Optional.empty());

        // When: 상품 상세 정보 조회 실행
        NaverShoppingItem result = simpleShoppingService.getProductDetail(productId);

        // Then: null 반환 검증
        assertNull(result);
        
        verify(itemRepository).findById(productId);
    }

    @Test
    void 검색_실패_에러응답_테스트() {
        // Given: 검색 중 예외가 발생하는 경우
        String query = "에러테스트";
        when(searchExecutorService.searchProductsInDatabase(query))
                .thenThrow(new RuntimeException("데이터베이스 연결 오류"));

        // When: 키워드 검색 실행
        ShoppingMessageResponse response = simpleShoppingService.searchProducts(query);

        // Then: 에러 응답 검증
        assertNotNull(response);
        assertEquals("죄송합니다. '에러테스트' 검색 중 오류가 발생했습니다.", response.getResponse());
        assertEquals("text", response.getMessageType());
        assertTrue(response.getProducts().isEmpty());
    }

    @Test
    void 상품카드_변환_테스트() {
        // Given: NaverShoppingItem 객체
        NaverShoppingItem item = sampleProduct1;

        // When: ProductCard로 변환
        ShoppingMessageResponse.ProductCard card = ShoppingMessageResponse.fromNaverShoppingItem(item);

        // Then: 변환 결과 검증
        assertNotNull(card);
        assertEquals(item.getId(), card.getId());
        assertEquals(item.getTitle(), card.getTitle());
        assertEquals(item.getImage(), card.getImage());
        assertEquals(item.getLink(), card.getLink());
        assertEquals(item.getLprice(), card.getLprice());
        assertEquals(item.getHprice(), card.getHprice());
        assertEquals(item.getMallName(), card.getMallName());
        assertEquals(item.getBrand(), card.getBrand());
        assertEquals("150,000원", card.getPriceFormatted());
        assertNotNull(card.getDiscountRate());
        assertFalse(card.isRecommended());
    }

    @Test
    void 가격포맷팅_테스트() {
        // Given: 다양한 가격 값들
        Integer price1 = 150000;
        Integer price2 = 0;
        Integer price3 = null;

        // When & Then: 가격 포맷팅 검증
        // 이 테스트는 private 메서드이므로 간접적으로 테스트
        NaverShoppingItem product1 = NaverShoppingItem.builder()
                .id(sampleProduct1.getId())
                .title(sampleProduct1.getTitle())
                .image(sampleProduct1.getImage())
                .link(sampleProduct1.getLink())
                .lprice(price1)
                .hprice(sampleProduct1.getHprice())
                .mallName(sampleProduct1.getMallName())
                .brand(sampleProduct1.getBrand())
                .category1(sampleProduct1.getCategory1())
                .category2(sampleProduct1.getCategory2())
                .productType(sampleProduct1.getProductType())
                .maker(sampleProduct1.getMaker())
                .searchCount(sampleProduct1.getSearchCount())
                .lastSearchedAt(sampleProduct1.getLastSearchedAt())
                .build();
        
        ShoppingMessageResponse.ProductCard card1 = ShoppingMessageResponse.fromNaverShoppingItem(product1);
        assertEquals("150,000원", card1.getPriceFormatted());

        NaverShoppingItem product2 = NaverShoppingItem.builder()
                .id(sampleProduct1.getId())
                .title(sampleProduct1.getTitle())
                .image(sampleProduct1.getImage())
                .link(sampleProduct1.getLink())
                .lprice(price2)
                .hprice(sampleProduct1.getHprice())
                .mallName(sampleProduct1.getMallName())
                .brand(sampleProduct1.getBrand())
                .category1(sampleProduct1.getCategory1())
                .category2(sampleProduct1.getCategory2())
                .productType(sampleProduct1.getProductType())
                .maker(sampleProduct1.getMaker())
                .searchCount(sampleProduct1.getSearchCount())
                .lastSearchedAt(sampleProduct1.getLastSearchedAt())
                .build();
        
        ShoppingMessageResponse.ProductCard card2 = ShoppingMessageResponse.fromNaverShoppingItem(product2);
        assertEquals("0원", card2.getPriceFormatted());

        NaverShoppingItem product3 = NaverShoppingItem.builder()
                .id(sampleProduct1.getId())
                .title(sampleProduct1.getTitle())
                .image(sampleProduct1.getImage())
                .link(sampleProduct1.getLink())
                .lprice(price3)
                .hprice(sampleProduct1.getHprice())
                .mallName(sampleProduct1.getMallName())
                .brand(sampleProduct1.getBrand())
                .category1(sampleProduct1.getCategory1())
                .category2(sampleProduct1.getCategory2())
                .productType(sampleProduct1.getProductType())
                .maker(sampleProduct1.getMaker())
                .searchCount(sampleProduct1.getSearchCount())
                .lastSearchedAt(sampleProduct1.getLastSearchedAt())
                .build();
        
        ShoppingMessageResponse.ProductCard card3 = ShoppingMessageResponse.fromNaverShoppingItem(product3);
        assertEquals("가격 정보 없음", card3.getPriceFormatted());
    }

    @Test
    void 할인율_계산_테스트() {
        // Given: 할인이 있는 상품
        NaverShoppingItem discountedItem = NaverShoppingItem.builder()
                .id(sampleProduct1.getId())
                .title(sampleProduct1.getTitle())
                .image(sampleProduct1.getImage())
                .link(sampleProduct1.getLink())
                .lprice(120000)  // 최저가
                .hprice(150000)  // 최고가
                .mallName(sampleProduct1.getMallName())
                .brand(sampleProduct1.getBrand())
                .category1(sampleProduct1.getCategory1())
                .category2(sampleProduct1.getCategory2())
                .productType(sampleProduct1.getProductType())
                .maker(sampleProduct1.getMaker())
                .searchCount(sampleProduct1.getSearchCount())
                .lastSearchedAt(sampleProduct1.getLastSearchedAt())
                .build();

        // When: ProductCard로 변환
        ShoppingMessageResponse.ProductCard card = ShoppingMessageResponse.fromNaverShoppingItem(discountedItem);

        // Then: 할인율 계산 검증
        assertNotNull(card.getDiscountRate());
        assertEquals("20% 할인", card.getDiscountRate());
    }
}
