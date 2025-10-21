package com.example.demo.controller;

import com.example.demo.dto.MessageRequest;
import com.example.demo.dto.ShoppingMessageResponse;
import com.example.demo.service.ChatService;
import com.example.demo.service.SimpleShoppingService;
import com.example.navershopping.entity.NaverShoppingItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ShoppingChatController 테스트 클래스
 * 
 * 쇼핑 챗봇 컨트롤러의 REST API 엔드포인트들을 테스트합니다:
 * - 채팅 메시지 처리
 * - 상품 검색 API
 * - 브랜드별 검색 API
 * - 카테고리별 검색 API
 * - 가격 범위 검색 API
 * - 인기 상품 조회 API
 * - 최신 상품 조회 API
 * - 상품 상세 정보 조회 API
 */
@WebMvcTest(controllers = ShoppingChatController.class)
class ShoppingChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SimpleShoppingService simpleShoppingService;

    @MockBean
    private ChatService chatService;

    // 모듈화된 서비스들 모킹
    @MockBean
    private com.example.demo.service.RagAnalysisService ragAnalysisService;
    
    @MockBean
    private com.example.demo.service.SearchExecutorService searchExecutorService;
    
    @MockBean
    private com.example.demo.service.ResponseBuilderService responseBuilderService;
    
    @MockBean
    private com.example.demo.service.KeywordAnalyzerService keywordAnalyzerService;

    @Autowired
    private ObjectMapper objectMapper;

    private NaverShoppingItem sampleProduct;
    private ShoppingMessageResponse sampleResponse;

    @BeforeEach
    void setUp() {
        // 테스트용 샘플 상품 데이터 생성
        sampleProduct = NaverShoppingItem.builder()
                .id(1L)
                .title("나이키 에어맥스 270")
                .image("https://example.com/nike1.jpg")
                .link("https://example.com/nike1")
                .lprice(150000)
                .hprice(180000)
                .mallName("나이키 공식몰")
                .brand("나이키")
                .category1("운동화")
                .searchCount(10)
                .lastSearchedAt(LocalDateTime.now())
                .build();

        // 테스트용 응답 데이터 생성
        sampleResponse = ShoppingMessageResponse.builder()
                .userId("test-user")
                .sessionId("test-session")
                .response("'나이키 운동화' 검색 결과 1개의 상품을 찾았습니다.")
                .messageType("shopping")
                .products(Arrays.asList(
                        ShoppingMessageResponse.ProductCard.builder()
                                .id(1L)
                                .title("나이키 에어맥스 270")
                                .image("https://example.com/nike1.jpg")
                                .link("https://example.com/nike1")
                                .lprice(150000)
                                .priceFormatted("150,000원")
                                .mallName("나이키 공식몰")
                                .brand("나이키")
                                .build()
                ))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @Test
    void 채팅메시지_처리_성공_테스트() throws Exception {
        // Given: 채팅 메시지 요청
        MessageRequest request = MessageRequest.builder()
                .sessionId("test-session")
                .userId("test-user")
                .message("나이키 운동화 검색해줘")
                .languageCode("ko")
                .build();

        when(simpleShoppingService.searchProducts(anyString()))
                .thenReturn(sampleResponse);

        // When & Then: POST /api/shopping-chat/message 요청 테스트
        mockMvc.perform(post("/api/shopping-chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("'나이키 운동화' 검색 결과 1개의 상품을 찾았습니다."))
                .andExpect(jsonPath("$.messageType").value("shopping"))
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].title").value("나이키 에어맥스 270"))
                .andExpect(jsonPath("$.products[0].priceFormatted").value("150,000원"));

        // 서비스 메서드 호출 검증
        verify(simpleShoppingService).searchProducts("나이키 운동화 검색해줘");
        verify(chatService, times(2)).saveMessage(anyString(), anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void 채팅메시지_처리_유효성검사_실패_테스트() throws Exception {
        // Given: 잘못된 요청 (메시지가 비어있음)
        MessageRequest invalidRequest = MessageRequest.builder()
                .sessionId("test-session")
                .userId("test-user")
                .message("")  // 빈 메시지
                .languageCode("ko")
                .build();

        // When & Then: 유효성 검사 실패 테스트
        mockMvc.perform(post("/api/shopping-chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 상품검색_API_성공_테스트() throws Exception {
        // Given: 상품 검색 요청
        String query = "나이키 운동화";
        when(simpleShoppingService.searchProducts(query))
                .thenReturn(sampleResponse);

        // When & Then: GET /api/shopping-chat/search 요청 테스트
        mockMvc.perform(get("/api/shopping-chat/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("'나이키 운동화' 검색 결과 1개의 상품을 찾았습니다."))
                .andExpect(jsonPath("$.products[0].title").value("나이키 에어맥스 270"));

        verify(simpleShoppingService).searchProducts(query);
    }

    @Test
    void 브랜드별검색_API_성공_테스트() throws Exception {
        // Given: 브랜드별 검색 요청
        String brand = "나이키";
        when(simpleShoppingService.searchProductsByBrand(brand))
                .thenReturn(sampleResponse);

        // When & Then: GET /api/shopping-chat/brand 요청 테스트
        mockMvc.perform(get("/api/shopping-chat/brand")
                        .param("brand", brand))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0].brand").value("나이키"));

        verify(simpleShoppingService).searchProductsByBrand(brand);
    }

    @Test
    void 카테고리별검색_API_성공_테스트() throws Exception {
        // Given: 카테고리별 검색 요청
        String category = "운동화";
        when(simpleShoppingService.searchProductsByCategory(category))
                .thenReturn(sampleResponse);

        // When & Then: GET /api/shopping-chat/category 요청 테스트
        mockMvc.perform(get("/api/shopping-chat/category")
                        .param("category", category))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0].category1").value("운동화"));

        verify(simpleShoppingService).searchProductsByCategory(category);
    }

    @Test
    void 가격범위검색_API_성공_테스트() throws Exception {
        // Given: 가격 범위 검색 요청
        Integer minPrice = 100000;
        Integer maxPrice = 200000;
        when(simpleShoppingService.searchProductsByPriceRange(minPrice, maxPrice))
                .thenReturn(sampleResponse);

        // When & Then: GET /api/shopping-chat/price-range 요청 테스트
        mockMvc.perform(get("/api/shopping-chat/price-range")
                        .param("minPrice", minPrice.toString())
                        .param("maxPrice", maxPrice.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0].lprice").value(150000));

        verify(simpleShoppingService).searchProductsByPriceRange(minPrice, maxPrice);
    }

    @Test
    void 인기상품조회_API_성공_테스트() throws Exception {
        // Given: 인기 상품 조회 요청
        ShoppingMessageResponse popularResponse = ShoppingMessageResponse.builder()
                .userId("test-user")
                .sessionId("test-session")
                .response("인기 상품 1개를 찾았습니다.")
                .messageType("recommendation")
                .products(sampleResponse.getProducts())
                .timestamp(sampleResponse.getTimestamp())
                .build();

        when(simpleShoppingService.getPopularProducts())
                .thenReturn(popularResponse);

        // When & Then: GET /api/shopping-chat/popular 요청 테스트
        mockMvc.perform(get("/api/shopping-chat/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("인기 상품 1개를 찾았습니다."))
                .andExpect(jsonPath("$.messageType").value("recommendation"));

        verify(simpleShoppingService).getPopularProducts();
    }

    @Test
    void 최신상품조회_API_성공_테스트() throws Exception {
        // Given: 최신 상품 조회 요청
        ShoppingMessageResponse recentResponse = ShoppingMessageResponse.builder()
                .userId("test-user")
                .sessionId("test-session")
                .response("최신 상품 1개를 찾았습니다.")
                .messageType("recommendation")
                .products(sampleResponse.getProducts())
                .timestamp(sampleResponse.getTimestamp())
                .build();

        when(simpleShoppingService.getRecentProducts())
                .thenReturn(recentResponse);

        // When & Then: GET /api/shopping-chat/recent 요청 테스트
        mockMvc.perform(get("/api/shopping-chat/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("최신 상품 1개를 찾았습니다."))
                .andExpect(jsonPath("$.messageType").value("recommendation"));

        verify(simpleShoppingService).getRecentProducts();
    }

    @Test
    void 상품상세정보조회_API_성공_테스트() throws Exception {
        // Given: 상품 상세 정보 조회 요청
        Long productId = 1L;
        when(simpleShoppingService.getProductDetail(productId))
                .thenReturn(sampleProduct);

        // When & Then: GET /api/shopping-chat/product/{id} 요청 테스트
        mockMvc.perform(get("/api/shopping-chat/product/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("나이키 에어맥스 270"))
                .andExpect(jsonPath("$.brand").value("나이키"))
                .andExpect(jsonPath("$.lprice").value(150000));

        verify(simpleShoppingService).getProductDetail(productId);
    }

    @Test
    void 상품상세정보조회_API_상품없음_테스트() throws Exception {
        // Given: 존재하지 않는 상품 ID
        Long productId = 999L;
        when(simpleShoppingService.getProductDetail(productId))
                .thenReturn(null);

        // When & Then: 404 에러 반환 테스트
        mockMvc.perform(get("/api/shopping-chat/product/{id}", productId))
                .andExpect(status().isNotFound());

        verify(simpleShoppingService).getProductDetail(productId);
    }

    @Test
    void 서비스_예외발생_500에러_테스트() throws Exception {
        // Given: 서비스에서 예외 발생
        String query = "에러테스트";
        when(simpleShoppingService.searchProducts(query))
                .thenThrow(new RuntimeException("서비스 오류"));

        // When & Then: 500 에러 반환 테스트
        mockMvc.perform(get("/api/shopping-chat/search")
                        .param("query", query))
                .andExpect(status().isInternalServerError());

        verify(simpleShoppingService).searchProducts(query);
    }

    @Test
    void CORS_헤더_테스트() throws Exception {
        // Given: CORS 요청
        String query = "테스트";
        when(simpleShoppingService.searchProducts(query))
                .thenReturn(sampleResponse);

        // When & Then: CORS 헤더 확인
        mockMvc.perform(get("/api/shopping-chat/search")
                        .param("query", query)
                        .header("Origin", "http://localhost:8501"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8501"));
    }

    @Test
    void 빈_검색결과_테스트() throws Exception {
        // Given: 빈 검색 결과
        String query = "존재하지않는상품";
        ShoppingMessageResponse emptyResponse = ShoppingMessageResponse.builder()
                .response("'" + query + "' 검색 결과 0개의 상품을 찾았습니다.")
                .messageType("shopping")
                .products(Collections.emptyList())
                .timestamp(LocalDateTime.now().toString())
                .build();

        when(simpleShoppingService.searchProducts(query))
                .thenReturn(emptyResponse);

        // When & Then: 빈 결과 응답 테스트
        mockMvc.perform(get("/api/shopping-chat/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("'존재하지않는상품' 검색 결과 0개의 상품을 찾았습니다."))
                .andExpect(jsonPath("$.products").isEmpty());

        verify(simpleShoppingService).searchProducts(query);
    }
}
