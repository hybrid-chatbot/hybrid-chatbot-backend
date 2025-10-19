package com.example.demo.service;

import com.example.demo.dto.ShoppingMessageResponse;
import com.example.navershopping.entity.NaverShoppingItem;
import com.example.navershopping.repository.NaverShoppingItemRepository;
import com.example.navershopping.service.NaverShoppingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * 키워드로 상품 검색
     *
     * 사용자가 입력한 키워드(예: "나이키 운동화", "청바지")로 상품을 검색합니다.
     *
     * 검색 과정:
     * 1. 먼저 데이터베이스에서 기존 상품 검색
     * 2. 결과가 5개 미만이면 네이버 API를 호출하여 새 상품 검색 및 저장
     * 3. 검색 결과를 채팅 UI용 응답 형태로 변환
     *
     * @param query 사용자가 입력한 검색 키워드
     * @return 상품 검색 결과가 포함된 응답 객체
     */
    @Transactional
    public ShoppingMessageResponse searchProducts(String query) {
        log.info("상품 검색 시작: {}", query);
        
        try {
            // 1단계: 데이터베이스에서 기존 상품 검색
            // 제목에 키워드가 포함된 상품들을 대소문자 구분 없이 검색
            List<NaverShoppingItem> products = itemRepository.findByTitleContainingIgnoreCase(query);
            
            // 2단계: 결과가 부족하면 네이버 API 호출
            if (products.size() < 5) {
                log.info("기존 데이터가 부족하여 네이버 API 호출 - 검색어: {}", query);
                // 네이버 쇼핑 API 호출하여 최대 20개 상품 검색 및 DB 저장
                naverShoppingService.searchAndSaveProducts(query, 20, 1);
                // 저장된 상품들을 다시 검색
                products = itemRepository.findByTitleContainingIgnoreCase(query);
            }
            
            // 3단계: 검색 결과를 채팅 UI용 응답 형태로 변환
            return createSearchResponse(query, products);
            
        } catch (Exception e) {
            log.error("상품 검색 중 오류 발생: {}", query, e);
            return createErrorResponse(query);
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
            return createSearchResponse(brand + " 브랜드 상품", products);
            
        } catch (Exception e) {
            log.error("브랜드별 상품 검색 실패: {}", brand, e);
            return createErrorResponse(brand);
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
            return createSearchResponse(category + " 카테고리 상품", products);
            
        } catch (Exception e) {
            log.error("카테고리별 상품 검색 실패: {}", category, e);
            return createErrorResponse(category);
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
            return createSearchResponse(String.format("%d원 ~ %d원 상품", minPrice, maxPrice), products);
            
        } catch (Exception e) {
            log.error("가격 범위별 상품 검색 실패: {}원 ~ {}원", minPrice, maxPrice, e);
            return createErrorResponse("가격 범위 검색");
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
            return createRecommendationResponse("인기 상품", products);
            
        } catch (Exception e) {
            log.error("인기 상품 조회 실패", e);
            return createErrorResponse("인기 상품");
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
            return createRecommendationResponse("최신 상품", products);
            
        } catch (Exception e) {
            log.error("최신 상품 조회 실패", e);
            return createErrorResponse("최신 상품");
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
     * 검색 응답 생성
     *
     * 검색된 상품들을 채팅 UI에서 표시할 수 있는 응답 형태로 변환합니다.
     *
     * @param query 검색 키워드
     * @param products 검색된 상품 목록
     * @return 채팅 UI용 응답 객체
     */
    private ShoppingMessageResponse createSearchResponse(String query, List<NaverShoppingItem> products) {
        // 사용자에게 보여줄 메시지 생성
        String responseMessage = String.format("'%s' 검색 결과 %d개의 상품을 찾았습니다.", query, products.size());
        
        // 상품들을 상품 카드 형태로 변환 (최대 20개로 제한)
        List<ShoppingMessageResponse.ProductCard> productCards = products.stream()
                .limit(20) // 성능을 위해 최대 20개로 제한
                .map(ShoppingMessageResponse::fromNaverShoppingItem) // NaverShoppingItem을 ProductCard로 변환
                .collect(Collectors.toList());

        // 검색 분석 정보 생성
        ShoppingMessageResponse.ShoppingAnalysisInfo analysisInfo = ShoppingMessageResponse.ShoppingAnalysisInfo.builder()
                .intentType("search") // 검색 의도
                .originalQuery(query) // 원본 검색어
                .totalResults(products.size()) // 총 결과 수
                .build();

        // 최종 응답 객체 생성
        return ShoppingMessageResponse.builder()
                .response(responseMessage) // 사용자에게 보여줄 메시지
                .messageType("shopping") // 메시지 타입 (쇼핑)
                .products(productCards) // 상품 카드 목록
                .analysisInfo(analysisInfo) // 분석 정보
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) // 현재 시간
                .build();
    }

    /**
     * 추천 응답 생성
     *
     * 추천 상품들을 채팅 UI에서 표시할 수 있는 응답 형태로 변환합니다.
     *
     * @param type 추천 타입 (예: "인기 상품", "최신 상품")
     * @param products 추천 상품 목록
     * @return 채팅 UI용 응답 객체
     */
    private ShoppingMessageResponse createRecommendationResponse(String type, List<NaverShoppingItem> products) {
        // 사용자에게 보여줄 메시지 생성
        String responseMessage = String.format("%s %d개를 찾았습니다.", type, products.size());
        
        // 상품들을 상품 카드 형태로 변환 (최대 10개로 제한)
        List<ShoppingMessageResponse.ProductCard> productCards = products.stream()
                .limit(10) // 추천은 최대 10개로 제한
                .map(ShoppingMessageResponse::fromNaverShoppingItem) // NaverShoppingItem을 ProductCard로 변환
                .collect(Collectors.toList());

        // 각 상품 카드를 추천 상품으로 설정
        for (ShoppingMessageResponse.ProductCard card : productCards) {
            card.setRecommended(true); // 추천 상품 표시
            card.setRecommendationReason(type); // 추천 이유 설정
        }

        // 최종 응답 객체 생성
        return ShoppingMessageResponse.builder()
                .response(responseMessage) // 사용자에게 보여줄 메시지
                .messageType("recommendation") // 메시지 타입 (추천)
                .products(productCards) // 상품 카드 목록
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) // 현재 시간
                .build();
    }

    /**
     * 에러 응답 생성
     *
     * 검색 중 오류가 발생했을 때 사용자에게 보여줄 에러 응답을 생성합니다.
     *
     * @param query 검색 키워드
     * @return 에러 메시지가 포함된 응답 객체
     */
    private ShoppingMessageResponse createErrorResponse(String query) {
        return ShoppingMessageResponse.builder()
                .response("죄송합니다. '" + query + "' 검색 중 오류가 발생했습니다.") // 에러 메시지
                .messageType("text") // 일반 텍스트 메시지
                .products(new ArrayList<>()) // 빈 상품 목록
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) // 현재 시간
                .build();
    }
}
