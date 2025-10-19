package com.example.demo.integration;

import com.example.demo.dto.MessageRequest;
import com.example.demo.dto.ShoppingMessageResponse;
import com.example.navershopping.entity.NaverShoppingItem;
import com.example.navershopping.repository.NaverShoppingItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 쇼핑 챗봇 통합 테스트 클래스
 * 
 * 실제 데이터베이스와 함께 전체 시스템을 테스트합니다:
 * - 데이터베이스 연동 테스트
 * - 전체 API 플로우 테스트
 * - 실제 상품 데이터로 테스트
 * - 엔드투엔드 시나리오 테스트
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ShoppingChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NaverShoppingItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private NaverShoppingItem testProduct1;
    private NaverShoppingItem testProduct2;

    @BeforeEach
    void setUp() {
        // 테스트용 상품 데이터를 데이터베이스에 저장
        testProduct1 = NaverShoppingItem.builder()
                .productId("test-product-1")
                .title("나이키 에어맥스 270 화이트")
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
                .searchQuery("나이키 운동화")
                .searchCount(5)
                .lastSearchedAt(LocalDateTime.now())
                .build();

        testProduct2 = NaverShoppingItem.builder()
                .productId("test-product-2")
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
                .searchQuery("아디다스 운동화")
                .searchCount(3)
                .lastSearchedAt(LocalDateTime.now().minusDays(1))
                .build();

        // 데이터베이스에 저장
        itemRepository.saveAll(Arrays.asList(testProduct1, testProduct2));
    }

    @Test
    void 전체_채팅플로우_통합테스트() throws Exception {
        // Given: 채팅 메시지 요청
        MessageRequest request = MessageRequest.builder()
                .sessionId("integration-test-session")
                .userId("integration-test-user")
                .message("나이키 운동화")
                .languageCode("ko")
                .build();

        // When & Then: 채팅 메시지 처리 테스트
        mockMvc.perform(post("/api/shopping-chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists())
                .andExpect(jsonPath("$.messageType").value("shopping"))
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].title").value("나이키 에어맥스 270 화이트"))
                .andExpect(jsonPath("$.products[0].priceFormatted").value("150,000원"))
                .andExpect(jsonPath("$.products[0].brand").value("나이키"));
    }

    @Test
    void 키워드검색_실제데이터_테스트() throws Exception {
        // When & Then: 키워드 검색 API 테스트
        mockMvc.perform(get("/api/shopping-chat/search")
                        .param("query", "나이키"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("'나이키' 검색 결과 1개의 상품을 찾았습니다."))
                .andExpect(jsonPath("$.products[0].title").value("나이키 에어맥스 270 화이트"))
                .andExpect(jsonPath("$.products[0].brand").value("나이키"));
    }

    @Test
    void 브랜드별검색_실제데이터_테스트() throws Exception {
        // When & Then: 브랜드별 검색 API 테스트
        mockMvc.perform(get("/api/shopping-chat/brand")
                        .param("brand", "아디다스"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("아디다스 브랜드 상품 1개를 찾았습니다."))
                .andExpect(jsonPath("$.products[0].title").value("아디다스 울트라부스트 22"))
                .andExpect(jsonPath("$.products[0].brand").value("아디다스"));
    }

    @Test
    void 카테고리별검색_실제데이터_테스트() throws Exception {
        // When & Then: 카테고리별 검색 API 테스트
        mockMvc.perform(get("/api/shopping-chat/category")
                        .param("category", "운동화"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("운동화 카테고리 상품 2개를 찾았습니다."))
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2));
    }

    @Test
    void 가격범위검색_실제데이터_테스트() throws Exception {
        // When & Then: 가격 범위 검색 API 테스트
        mockMvc.perform(get("/api/shopping-chat/price-range")
                        .param("minPrice", "100000")
                        .param("maxPrice", "200000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("100000원 ~ 200000원 상품 2개를 찾았습니다."))
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2));
    }

    @Test
    void 인기상품조회_실제데이터_테스트() throws Exception {
        // When & Then: 인기 상품 조회 API 테스트
        mockMvc.perform(get("/api/shopping-chat/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("인기 상품 2개를 찾았습니다."))
                .andExpect(jsonPath("$.messageType").value("recommendation"))
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.products[0].isRecommended").value(true))
                .andExpect(jsonPath("$.products[0].recommendationReason").value("인기 상품"));
    }

    @Test
    void 최신상품조회_실제데이터_테스트() throws Exception {
        // When & Then: 최신 상품 조회 API 테스트
        mockMvc.perform(get("/api/shopping-chat/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("최신 상품 2개를 찾았습니다."))
                .andExpect(jsonPath("$.messageType").value("recommendation"))
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.products[0].isRecommended").value(true))
                .andExpect(jsonPath("$.products[0].recommendationReason").value("최신 상품"));
    }

    @Test
    void 상품상세정보조회_실제데이터_테스트() throws Exception {
        // When & Then: 상품 상세 정보 조회 API 테스트
        mockMvc.perform(get("/api/shopping-chat/product/{id}", testProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testProduct1.getId()))
                .andExpect(jsonPath("$.title").value("나이키 에어맥스 270 화이트"))
                .andExpect(jsonPath("$.brand").value("나이키"))
                .andExpect(jsonPath("$.lprice").value(150000))
                .andExpect(jsonPath("$.mallName").value("나이키 공식몰"));
    }

    @Test
    void 존재하지않는상품_조회_테스트() throws Exception {
        // When & Then: 존재하지 않는 상품 조회 테스트
        mockMvc.perform(get("/api/shopping-chat/product/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void 빈_검색결과_테스트() throws Exception {
        // When & Then: 존재하지 않는 키워드 검색 테스트
        mockMvc.perform(get("/api/shopping-chat/search")
                        .param("query", "존재하지않는상품"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("'존재하지않는상품' 검색 결과 0개의 상품을 찾았습니다."))
                .andExpect(jsonPath("$.products").isEmpty());
    }

    @Test
    void 상품카드_데이터변환_테스트() throws Exception {
        // When & Then: 상품 카드 데이터 변환 테스트
        mockMvc.perform(get("/api/shopping-chat/search")
                        .param("query", "나이키"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0].id").value(testProduct1.getId()))
                .andExpect(jsonPath("$.products[0].title").value("나이키 에어맥스 270 화이트"))
                .andExpect(jsonPath("$.products[0].image").value("https://example.com/nike1.jpg"))
                .andExpect(jsonPath("$.products[0].link").value("https://example.com/nike1"))
                .andExpect(jsonPath("$.products[0].lprice").value(150000))
                .andExpect(jsonPath("$.products[0].hprice").value(180000))
                .andExpect(jsonPath("$.products[0].priceFormatted").value("150,000원"))
                .andExpect(jsonPath("$.products[0].mallName").value("나이키 공식몰"))
                .andExpect(jsonPath("$.products[0].brand").value("나이키"))
                .andExpect(jsonPath("$.products[0].category1").value("운동화"))
                .andExpect(jsonPath("$.products[0].discountRate").value("17% 할인"))
                .andExpect(jsonPath("$.products[0].isRecommended").value(false));
    }

    @Test
    void CORS_헤더_통합테스트() throws Exception {
        // When & Then: CORS 헤더 확인
        mockMvc.perform(get("/api/shopping-chat/search")
                        .param("query", "테스트")
                        .header("Origin", "http://localhost:8501"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8501"));
    }

    @Test
    void 다중_검색시나리오_테스트() throws Exception {
        // Given: 여러 검색 시나리오
        String[] queries = {"나이키", "아디다스", "운동화"};
        
        for (String query : queries) {
            // When & Then: 각 쿼리별 검색 테스트
            mockMvc.perform(get("/api/shopping-chat/search")
                            .param("query", query))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.response").exists())
                    .andExpect(jsonPath("$.products").isArray());
        }
    }

    @Test
    void 검색결과_정렬_테스트() throws Exception {
        // When & Then: 검색 결과 정렬 테스트 (가격 순)
        mockMvc.perform(get("/api/shopping-chat/search")
                        .param("query", "운동화"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                // 첫 번째 상품이 더 저렴한지 확인 (아디다스: 120,000원 < 나이키: 150,000원)
                .andExpect(jsonPath("$.products[0].lprice").value(120000))
                .andExpect(jsonPath("$.products[1].lprice").value(150000));
    }
}
