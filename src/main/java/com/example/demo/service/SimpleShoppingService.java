package com.example.demo.service;

import com.example.demo.dto.ShoppingMessageResponse;
import com.example.demo.dto.AiServerResponse;
import com.example.demo.dto.AnalysisTrace;
import com.example.navershopping.entity.NaverShoppingItem;
import com.example.navershopping.repository.NaverShoppingItemRepository;
import com.example.navershopping.service.NaverShoppingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 간단한 쇼핑 서비스
 * 
 * 이 서비스는 쇼핑몰 챗봇의 핵심 기능을 제공합니다:
 * - 사용자 키워드로 상품 검색
 * - 브랜드, 카테고리, 가격별 필터링 검색
 * - 인기 상품 및 최신 상품 조회
 * - 검색 결과를 채팅 UI용 응답 형태로 변환
 * 
 * 주요 특징:
 * - 기존 DB 데이터 우선 검색, 부족하면 네이버 API 호출
 * - 검색 결과를 상품 카드 형태로 시각화
 * - 에러 처리 및 로깅 포함
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimpleShoppingService {

    // 상품 데이터베이스 접근을 위한 리포지토리
    private final NaverShoppingItemRepository itemRepository;
    
    // 네이버 쇼핑 API 호출을 위한 서비스
    private final NaverShoppingService naverShoppingService;
    
    // 모듈화된 서비스들
    private final AiServerService aiServerService;
    private final SearchExecutorService searchExecutorService;
    private final ResponseBuilderService responseBuilderService;

    /**
     * LLM 직접 의도분석을 포함한 상품 검색
     * 
     * 외부 AI 서버의 LLM 모델로 상품검색 의도를 분석합니다.
     * 
     * @param query 사용자가 입력한 검색 키워드
     * @param sessionId 세션 ID
     * @param languageCode 언어 코드
     * @return 상품 검색 결과가 포함된 응답 객체
     */
    @Transactional
    public ShoppingMessageResponse searchProductsWithIntent(String query, String sessionId, String languageCode) {
        log.info("LLM 직접 의도분석 기반 상품 검색 시작: {}", query);
        
        // 분석 추적을 위한 빌더
        AnalysisTrace.Builder traceBuilder = AnalysisTrace.builder();
        
        try {
            // 상품검색용 의도 목록
            List<String> shoppingIntents = List.of(
                "product_search",           // 일반 상품 검색
                "product_recommendation",   // 상품 추천
                "product_filter",          // 상품 필터링
                "product_compare",         // 상품 비교
                "brand_search",            // 브랜드별 검색
                "category_search",         // 카테고리별 검색
                "price_range_search"       // 가격대별 검색
            );
            
            // LLM 모델로 직접 의도분석
            AiServerResponse aiResponse = aiServerService.classifyIntent(query, shoppingIntents);
            
            String finalIntentName = "product_search"; // 기본값
            float finalIntentScore = 0.5f; // 기본 신뢰도 (낮음)
            String finalEngine = "fallback";
            
            if (aiResponse != null) {
                finalIntentName = aiResponse.getFinal_intent();
                finalEngine = aiResponse.getEngine();
                finalIntentScore = aiResponse.getConfidence() != null ? 
                    aiResponse.getConfidence() : 0.8f;
                
                log.info("LLM 의도분석 결과 - 의도: {}, 신뢰도: {}, 엔진: {}", 
                        finalIntentName, finalIntentScore, finalEngine);
            } else {
                log.warn("LLM 분석 실패 - 기본 검색으로 처리");
            }
            
            // 신뢰도에 따른 처리 방식 결정
            if (finalIntentScore < 0.6f) {
                log.warn("LLM 신뢰도가 낮음 ({}). 안전한 일반 검색으로 처리", finalIntentScore);
                return searchProducts(query);
            }
            
            // 최종 의도로 동적 SQL 기반 상품 검색 실행
            log.info("최종 의도분석 결과 - 의도: {}, 신뢰도: {}, 엔진: {}", finalIntentName, finalIntentScore, finalEngine);
            return executeDynamicSearchWithTrace(query, aiResponse, traceBuilder.build());
            
        } catch (Exception e) {
            log.error("LLM 직접 의도분석 기반 상품 검색 중 오류 발생: {}", query, e);
            return searchProducts(query);
        }
    }

    /**
     * 동적 SQL 기반 상품 검색 실행
     * 
     * @param query 사용자 검색어
     * @param aiResponse LLM 분석 결과
     * @param analysisTrace 분석 추적 정보
     * @return 검색 결과 응답
     */
    private ShoppingMessageResponse executeDynamicSearchWithTrace(String query, AiServerResponse aiResponse, AnalysisTrace analysisTrace) {
        log.info("동적 SQL 기반 상품 검색 실행: {}", query);
        
        try {
            // 1. 동적 SQL 기반 상품 검색
            List<NaverShoppingItem> products = searchExecutorService.executeDynamicSearch(query, aiResponse);
            
            // 2. LLM이 동적 SQL로 정렬을 처리하므로 별도 정렬 불필요
            
            // 3. 응답 생성
            return responseBuilderService.createSearchResponseWithTrace(
                query, 
                products, 
                null, // LLM이 동적 SQL로 정렬 처리
                analysisTrace,
                aiResponse != null ? aiResponse.getFinal_intent() : "product_search",
                aiResponse != null ? aiResponse.getConfidence() : 0.5f
            );
            
        } catch (Exception e) {
            log.error("동적 SQL 기반 상품 검색 중 오류 발생: {}", query, e);
            return responseBuilderService.createErrorResponse(query);
        }
    }

    

    /**
     * 키워드로 상품 검색 (기존 메서드 - 폴백용)
     * 
     * 사용자가 입력한 키워드(예: "나이키 운동화", "청바지")로 상품을 검색합니다.
     * 
     * 검색 과정:
     * 1. 가격순 정렬 요청 감지 및 키워드 정리
     * 2. 먼저 데이터베이스에서 기존 상품 검색
     * 3. 결과가 5개 미만이면 네이버 API를 호출하여 새 상품 검색 및 저장
     * 4. 가격순 정렬 요청이 있으면 정렬 적용
     * 5. 검색 결과를 채팅 UI용 응답 형태로 변환
     * 
     * @param query 사용자가 입력한 검색 키워드
     * @return 상품 검색 결과가 포함된 응답 객체
     */
    @Transactional
    public ShoppingMessageResponse searchProducts(String query) {
        log.info("상품 검색 시작: {}", query);
        
        try {
            // 1단계: 데이터베이스에서 기존 상품 검색
            List<NaverShoppingItem> products = searchExecutorService.searchProductsInDatabase(query);
            
            // 2단계: 결과가 부족하면 네이버 API 호출
            if (products.size() < 5) {
                log.info("기존 데이터가 부족하여 네이버 API 호출 - 검색어: {}", query);
                naverShoppingService.searchAndSaveProducts(query, 20, 1);
                products = searchExecutorService.searchProductsInDatabase(query);
            }
            
            // 3단계: 검색 결과를 채팅 UI용 응답 형태로 변환
            return responseBuilderService.createSearchResponse(query, products, null);
            
        } catch (Exception e) {
            log.error("상품 검색 중 오류 발생: {}", query, e);
            return responseBuilderService.createErrorResponse(query);
        }
    }

    /**
     * 브랜드별 상품 검색
     * 
     * 특정 브랜드(예: "나이키", "아디다스")의 상품들을 검색합니다.
     * 
     * @param brand 검색할 브랜드명
     * @return 해당 브랜드의 상품 목록이 포함된 응답 객체
     */
    @Transactional
    public ShoppingMessageResponse searchProductsByBrand(String brand) {
        log.info("브랜드별 상품 검색 시작: {}", brand);
        
        try {
            // 1단계: 데이터베이스에서 해당 브랜드 상품 검색
            List<NaverShoppingItem> products = itemRepository.findByBrand(brand);
            
            // 2단계: 결과가 없으면 네이버 API로 새로 검색
            if (products.isEmpty()) {
                log.info("브랜드 상품이 없어서 네이버 API 호출: {}", brand);
                naverShoppingService.searchAndSaveProducts(brand, 20, 1);
                products = itemRepository.findByBrand(brand);
            }
            
            // 3단계: 검색 결과를 응답 형태로 변환
            return responseBuilderService.createSearchResponse(brand + " 브랜드 상품", products);
            
        } catch (Exception e) {
            log.error("브랜드별 상품 검색 실패: {}", brand, e);
            return responseBuilderService.createErrorResponse(brand);
        }
    }

    /**
     * 카테고리별 상품 검색
     * 
     * 특정 카테고리(예: "청바지", "운동화", "가방")의 상품들을 검색합니다.
     * 
     * @param category 검색할 카테고리명
     * @return 해당 카테고리의 상품 목록이 포함된 응답 객체
     */
    @Transactional
    public ShoppingMessageResponse searchProductsByCategory(String category) {
        log.info("카테고리별 상품 검색 시작: {}", category);
        
        try {
            // 1단계: 데이터베이스에서 해당 카테고리 상품 검색
            List<NaverShoppingItem> products = itemRepository.findByCategory1(category);
            
            // 2단계: 결과가 없으면 네이버 API로 새로 검색
            if (products.isEmpty()) {
                log.info("카테고리 상품이 없어서 네이버 API 호출: {}", category);
                naverShoppingService.searchAndSaveProducts(category, 20, 1);
                products = itemRepository.findByCategory1(category);
            }
            
            // 3단계: 검색 결과를 응답 형태로 변환
            return responseBuilderService.createSearchResponse(category + " 카테고리 상품", products);
            
        } catch (Exception e) {
            log.error("카테고리별 상품 검색 실패: {}", category, e);
            return responseBuilderService.createErrorResponse(category);
        }
    }

    /**
     * 가격 범위별 상품 검색
     * 
     * 지정된 가격 범위 내의 상품들을 검색합니다.
     * 
     * @param minPrice 최소 가격 (원)
     * @param maxPrice 최대 가격 (원)
     * @return 해당 가격 범위의 상품 목록이 포함된 응답 객체
     */
    @Transactional
    public ShoppingMessageResponse searchProductsByPriceRange(Integer minPrice, Integer maxPrice) {
        log.info("가격 범위별 상품 검색 시작: {}원 ~ {}원", minPrice, maxPrice);
        
        try {
            // 데이터베이스에서 가격 범위에 해당하는 상품 검색
            List<NaverShoppingItem> products = itemRepository.findByPriceRange(minPrice, maxPrice);
            
            // 검색 결과를 응답 형태로 변환
            return responseBuilderService.createSearchResponse(String.format("%d원 ~ %d원 상품", minPrice, maxPrice), products);
            
        } catch (Exception e) {
            log.error("가격 범위별 상품 검색 실패: {}원 ~ {}원", minPrice, maxPrice, e);
            return responseBuilderService.createErrorResponse("가격 범위 검색");
        }
    }

    /**
     * 인기 상품 조회
     * 
     * 검색 횟수가 많은 상위 10개 상품을 조회합니다.
     * 
     * @return 인기 상품 목록이 포함된 응답 객체
     */
    @Transactional
    public ShoppingMessageResponse getPopularProducts() {
        log.info("인기 상품 조회 시작");
        
        try {
            // 검색 횟수 기준으로 상위 10개 상품 조회
            List<NaverShoppingItem> products = itemRepository.findTop10ByOrderBySearchCountDesc();
            
            // 추천 응답 형태로 변환 (추천 상품으로 표시)
            return responseBuilderService.createRecommendationResponse("인기 상품", products);
            
        } catch (Exception e) {
            log.error("인기 상품 조회 실패", e);
            return responseBuilderService.createErrorResponse("인기 상품");
        }
    }

    /**
     * 최신 상품 조회
     * 
     * 최근에 검색된 상위 10개 상품을 조회합니다.
     * 
     * @return 최신 상품 목록이 포함된 응답 객체
     */
    @Transactional
    public ShoppingMessageResponse getRecentProducts() {
        log.info("최신 상품 조회 시작");
        
        try {
            // 최근 검색 시간 기준으로 상위 10개 상품 조회
            List<NaverShoppingItem> products = itemRepository.findTop10ByOrderByLastSearchedAtDesc();
            
            // 추천 응답 형태로 변환 (추천 상품으로 표시)
            return responseBuilderService.createRecommendationResponse("최신 상품", products);
            
        } catch (Exception e) {
            log.error("최신 상품 조회 실패", e);
            return responseBuilderService.createErrorResponse("최신 상품");
        }
    }

    /**
     * 상품 상세 정보 조회
     * 
     * 특정 상품의 상세 정보를 조회합니다.
     * 
     * @param id 조회할 상품의 ID
     * @return 상품 상세 정보 (없으면 null)
     */
    @Transactional
    public NaverShoppingItem getProductDetail(Long id) {
        return itemRepository.findById(id).orElse(null);
    }




    /**
     * 나이키 운동화 더미 데이터 생성
     * 
     * 테스트를 위해 다양한 가격의 나이키 운동화 더미 데이터를 생성합니다.
     * 
     * @return 생성된 더미 데이터 개수
     */
    @Transactional
    public int createDummyNikeShoes() {
        log.info("나이키 운동화 더미 데이터 생성 시작");
        
        try {
            // 기존 더미 데이터 삭제
            itemRepository.deleteByBrand("나이키");
            
            // 더미 데이터 생성 (다양한 가격대로)
            List<NaverShoppingItem> dummyItems = List.of(
                createDummyItem("나이키 에어맥스 90", 59000, "나이키", "운동화", "러닝화"),
                createDummyItem("나이키 에어포스 1", 89000, "나이키", "운동화", "스니커즈"),
                createDummyItem("나이키 조던 1", 129000, "나이키", "운동화", "스니커즈"),
                createDummyItem("나이키 덩크 로우", 109000, "나이키", "운동화", "스니커즈"),
                createDummyItem("나이키 에어맥스 270", 139000, "나이키", "운동화", "러닝화"),
                createDummyItem("나이키 프리런 5.0", 79000, "나이키", "운동화", "러닝화"),
                createDummyItem("나이키 리액트 엘리먼트 55", 99000, "나이키", "운동화", "러닝화"),
                createDummyItem("나이키 에어맥스 97", 149000, "나이키", "운동화", "러닝화"),
                createDummyItem("나이키 코르테즈", 69000, "나이키", "운동화", "스니커즈"),
                createDummyItem("나이키 블레이저 미드", 89000, "나이키", "운동화", "스니커즈")
            );
            
            // 데이터베이스에 저장
            List<NaverShoppingItem> savedItems = itemRepository.saveAll(dummyItems);
            
            log.info("나이키 운동화 더미 데이터 생성 완료: {}개", savedItems.size());
            return savedItems.size();
            
        } catch (Exception e) {
            log.error("나이키 운동화 더미 데이터 생성 실패", e);
            throw e;
        }
    }

    /**
     * 더미 상품 아이템 생성
     * 
     * @param title 상품명
     * @param price 가격
     * @param brand 브랜드
     * @param category2 카테고리2
     * @param category3 카테고리3
     * @return 더미 상품 아이템
     */
    private NaverShoppingItem createDummyItem(String title, int price, String brand, String category2, String category3) {
        // 고유한 productId 생성
        String uniqueId = "dummy-" + System.nanoTime() + "-" + price;
        
        return NaverShoppingItem.builder()
                .title(title)
                .link("https://example.com/" + title.replace(" ", "-").toLowerCase())
                .image("https://via.placeholder.com/300x300?text=" + title.replace(" ", "+"))
                .lprice(price)
                .hprice(price + 10000) // 최고가는 최저가 + 10,000원
                .mallName("테스트 쇼핑몰")
                .productId(uniqueId)
                .productType("2")
                .brand(brand)
                .maker(brand)
                .category1("패션잡화")
                .category2(category2)
                .category3(category3)
                .category4("")
                .searchCount(0)
                .lastSearchedAt(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 모든 상품 조회 (디버깅용)
     * 
     * @return 모든 상품 목록
     */
    public List<NaverShoppingItem> getAllProducts() {
        log.info("모든 상품 조회");
        return itemRepository.findAll();
    }





    


}
