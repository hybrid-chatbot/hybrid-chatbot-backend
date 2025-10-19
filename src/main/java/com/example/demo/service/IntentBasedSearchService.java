package com.example.demo.service;

import com.example.demo.model.ChatMessage;
import com.example.navershopping.entity.NaverShoppingItem;
import com.example.navershopping.service.NaverShoppingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 의도 분석 결과(intentName, parameters)를 바탕으로
 * 저장된 네이버 쇼핑 상품 DB를 조회하여 결과를 구성하는 서비스입니다.
 *
 * 핵심 역할
 * - intentName에 따라 검색/추천/필터/비교 로직 분기
 * - DB에 이미 저장된 상품들(NaverShoppingItem)만 활용
 * - 프론트 UI가 바로 사용할 수 있는 ProductInfo 리스트로 변환하여 반환
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntentBasedSearchService {

    private final NaverShoppingService naverShoppingService;

    /**
     * 의도 분석 결과를 바탕으로 상품을 검색하고 쇼핑 데이터를 생성합니다.
     *
     * @param intentName    의도명 (예: "product_search", "product_recommendation", "product_filter")
     * @param originalQuery 원본 사용자 쿼리(필요 시 키워드로 활용)
     * @param parameters    Dialogflow 파라미터(가격 범위, 브랜드 등)
     * @return ProductInfo 리스트를 포함한 ShoppingData (UI 렌더링에 바로 사용)
     */
    public ChatMessage.ShoppingData searchProductsByIntent(String intentName, String originalQuery, Map<String, Object> parameters) {
        log.info("의도 기반 상품 검색 시작 - 의도: {}, 쿼리: {}, 파라미터: {}", intentName, originalQuery, parameters);

        try {
            List<NaverShoppingItem> products;

            switch (intentName.toLowerCase()) {
                case "product_search":
                case "search_products":
                    // 일반 상품 검색
                    products = searchProductsByQuery(originalQuery);
                    break;
                    
                case "product_recommendation":
                case "recommend_products":
                    // 추천 상품 검색
                    products = searchRecommendedProducts(originalQuery, parameters);
                    break;
                    
                case "product_filter":
                case "filter_products":
                    // 필터링된 상품 검색
                    products = searchFilteredProducts(originalQuery, parameters);
                    break;
                    
                case "product_compare":
                case "compare_products":
                    // 상품 비교를 위한 검색
                    products = searchProductsForComparison(originalQuery, parameters);
                    break;
                    
                default:
                    // 기본적으로 일반 검색 수행
                    log.warn("알 수 없는 의도: {}, 일반 검색으로 처리", intentName);
                    products = searchProductsByQuery(originalQuery);
            }

            // 쇼핑 데이터 생성
            return ChatMessage.ShoppingData.builder()
                    .intentType(intentName)
                    .originalQuery(originalQuery)
                    .totalResults(products.size())
                    .searchTime(java.time.LocalDateTime.now().toString())
                    .confidence("high")
                    .products(convertToProductInfo(products))
                    .build();

        } catch (Exception e) {
            log.error("의도 기반 상품 검색 실패 - 의도: {}, 쿼리: {}", intentName, originalQuery, e);
            return ChatMessage.ShoppingData.builder()
                    .intentType(intentName)
                    .originalQuery(originalQuery)
                    .totalResults(0)
                    .searchTime(java.time.LocalDateTime.now().toString())
                    .confidence("low")
                    .products(java.util.Collections.emptyList())
                    .build();
        }
    }

    /**
     * 일반 상품 검색: 저장된 검색쿼리/제목 기준으로 최대 10개 반환
     */
    private List<NaverShoppingItem> searchProductsByQuery(String query) {
        log.info("일반 상품 검색 실행 - 쿼리: {}", query);
        
        // 먼저 저장된 상품에서 검색 시도
        List<NaverShoppingItem> savedProducts = naverShoppingService.getSavedProductsByQuery(query);
        
        if (!savedProducts.isEmpty()) {
            log.info("저장된 상품에서 {}개 검색됨", savedProducts.size());
            return savedProducts.stream().limit(10).toList();
        }
        
        // 저장된 상품이 없으면 제목으로 검색
        savedProducts = naverShoppingService.getSavedProductsByTitle(query);
        if (!savedProducts.isEmpty()) {
            log.info("제목 검색으로 {}개 검색됨", savedProducts.size());
            return savedProducts.stream().limit(10).toList();
        }
        
        // 저장된 상품이 전혀 없으면 빈 리스트 반환
        log.warn("검색 결과가 없음 - 쿼리: {}", query);
        return List.of();
    }

    /**
     * 추천 상품 검색: 저장된 상품의 검색 빈도(searchCount) 기준 상위 10개를 반환
     */
    private List<NaverShoppingItem> searchRecommendedProducts(String query, Map<String, Object> parameters) {
        log.info("추천 상품 검색 실행 - 쿼리: {}, 파라미터: {}", query, parameters);
        
        // 추천 로직: 검색 횟수가 많은 상품들을 우선적으로 반환
        List<NaverShoppingItem> allProducts = naverShoppingService.getSavedProductsByQuery(query);
        
        if (allProducts.isEmpty()) {
            allProducts = naverShoppingService.getSavedProductsByTitle(query);
        }
        
        // 검색 횟수 기준으로 정렬하여 상위 10개 반환
        return allProducts.stream()
                .sorted((a, b) -> Integer.compare(b.getSearchCount(), a.getSearchCount()))
                .limit(10)
                .toList();
    }

    /**
     * 필터링된 상품 검색: minPrice/maxPrice, brand, mallName 등 파라미터 적용
     */
    private List<NaverShoppingItem> searchFilteredProducts(String query, Map<String, Object> parameters) {
        log.info("필터링된 상품 검색 실행 - 쿼리: {}, 파라미터: {}", query, parameters);
        
        List<NaverShoppingItem> products = naverShoppingService.getSavedProductsByQuery(query);
        
        if (products.isEmpty()) {
            products = naverShoppingService.getSavedProductsByTitle(query);
        }
        
        // 가격 필터링
        if (parameters.containsKey("minPrice") || parameters.containsKey("maxPrice")) {
            Integer minPrice = (Integer) parameters.getOrDefault("minPrice", 0);
            Integer maxPrice = (Integer) parameters.getOrDefault("maxPrice", Integer.MAX_VALUE);
            
            products = products.stream()
                    .filter(item -> item.getLprice() != null && 
                                   item.getLprice() >= minPrice && 
                                   item.getLprice() <= maxPrice)
                    .toList();
        }
        
        // 브랜드 필터링
        if (parameters.containsKey("brand")) {
            String brand = (String) parameters.get("brand");
            products = products.stream()
                    .filter(item -> item.getBrand() != null && 
                                   item.getBrand().toLowerCase().contains(brand.toLowerCase()))
                    .toList();
        }
        
        // 쇼핑몰 필터링
        if (parameters.containsKey("mallName")) {
            String mallName = (String) parameters.get("mallName");
            products = products.stream()
                    .filter(item -> item.getMallName() != null && 
                                   item.getMallName().toLowerCase().contains(mallName.toLowerCase()))
                    .toList();
        }
        
        return products.stream().limit(10).toList();
    }

    /**
     * 상품 비교 검색: 가격 기준 정렬(오름차순) 후 최대 10개 반환
     */
    private List<NaverShoppingItem> searchProductsForComparison(String query, Map<String, Object> parameters) {
        log.info("상품 비교 검색 실행 - 쿼리: {}, 파라미터: {}", query, parameters);
        
        // 비교를 위해 다양한 조건으로 검색하여 다양한 상품들을 반환
        List<NaverShoppingItem> products = naverShoppingService.getSavedProductsByQuery(query);
        
        if (products.isEmpty()) {
            products = naverShoppingService.getSavedProductsByTitle(query);
        }
        
        // 가격대별로 다양한 상품들을 반환 (최저가, 중간가, 최고가)
        return products.stream()
                .sorted((a, b) -> {
                    if (a.getLprice() == null) return 1;
                    if (b.getLprice() == null) return -1;
                    return Integer.compare(a.getLprice(), b.getLprice());
                })
                .limit(10)
                .toList();
    }

    /**
     * NaverShoppingItem을 프론트가 사용하는 ProductInfo로 변환합니다.
     * - id는 JPA 엔티티의 Long id를 그대로 사용
     * - 카테고리/브랜드/가격/이미지/링크 등 UI 필요한 필드만 추려 반환
     */
    private List<ChatMessage.ProductInfo> convertToProductInfo(List<NaverShoppingItem> items) {
        return items.stream()
                .map((NaverShoppingItem item) -> ChatMessage.ProductInfo.builder()
                        .id(item.getId()) // NaverShoppingItem.id가 Long이면 그대로 사용
                        .title(item.getTitle())
                        .link(item.getLink())
                        .image(item.getImage())
                        .lprice(item.getLprice())
                        .hprice(item.getHprice())
                        .mallName(item.getMallName())
                        .brand(item.getBrand())
                        .category1(item.getCategory1())
                        .category2(item.getCategory2())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }
}



