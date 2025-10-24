package com.example.demo.service;

import com.example.demo.dto.AiServerResponse;
import com.example.navershopping.entity.NaverShoppingItem;
import com.example.navershopping.repository.NaverShoppingItemRepository;
import com.example.navershopping.service.NaverShoppingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 검색 실행 서비스
 * 
 * 다양한 의도에 따른 상품 검색을 실행하는 로직을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchExecutorService {

    private final NaverShoppingItemRepository itemRepository;
    private final DynamicSqlQueryService dynamicSqlQueryService;
    private final NaverShoppingService naverShoppingService;

    /**
     * LLM 의도분석 기반 동적 SQL 검색
     * 
     * @param query 사용자 검색어
     * @param aiResponse LLM 분석 결과
     * @return 검색된 상품 목록
     */
    public List<NaverShoppingItem> executeDynamicSearch(String query, AiServerResponse aiResponse) {
        log.info("동적 SQL 기반 상품 검색 시작 - 검색어: {}, 의도: {}", 
                query, aiResponse != null ? aiResponse.getFinal_intent() : "unknown");
        
        try {
            // 1. LLM 분석 결과를 기반으로 동적 SQL 쿼리 생성
            DynamicSqlQueryService.DynamicQueryResult queryResult = 
                dynamicSqlQueryService.generateDynamicQuery(query, aiResponse);
            
            log.info("생성된 SQL 쿼리: {}", queryResult.getSql());
            log.info("쿼리 파라미터: {}", queryResult.getParameters());
            
            // 2. 동적 SQL 쿼리 실행
            List<NaverShoppingItem> products = executeDynamicQuery(queryResult);
            
            log.info("동적 SQL 검색 완료 - {}개 상품 발견", products.size());
            
            // 3. 결과가 부족하면 네이버 API 호출
            if (products.size() < 5) {
                log.info("동적 SQL 결과 부족 ({}개) - 네이버 API 호출", products.size());
                naverShoppingService.searchAndSaveProducts(query, 20, 1);
                products = executeDynamicQuery(queryResult); // 재검색
            }
            
            return products;
            
        } catch (Exception e) {
            log.error("동적 SQL 검색 중 오류 발생: {}", query, e);
            // 폴백: 기존 방식으로 검색
            return searchProductsInDatabase(query);
        }
    }
    
    /**
     * 동적 SQL 쿼리 실행
     */
    private List<NaverShoppingItem> executeDynamicQuery(DynamicSqlQueryService.DynamicQueryResult queryResult) {
        try {
            // DynamicSqlQueryService를 통해 네이티브 SQL 쿼리 실행
            return dynamicSqlQueryService.executeNativeQuery(queryResult.getSql());
        } catch (Exception e) {
            log.error("동적 SQL 쿼리 실행 실패: {}", queryResult.getSql(), e);
            return new ArrayList<>();
        }
    }


    /**
     * 데이터베이스에서 상품 검색
     * 
     * 검색어를 분석하여 다양한 방법으로 상품을 검색합니다.
     * 
     * @param query 검색어
     * @return 검색된 상품 목록
     */
    public List<NaverShoppingItem> searchProductsInDatabase(String query) {
        log.info("데이터베이스에서 상품 검색: {}", query);
        
        List<NaverShoppingItem> products = new ArrayList<>();
        
        // 1. 전체 검색어로 검색
        products.addAll(itemRepository.findByTitleContainingIgnoreCase(query));
        log.info("전체 검색어 '{}'로 {}개 상품 발견", query, products.size());
        
        // 2. 검색어를 단어별로 분리하여 검색
        String[] keywords = query.split("\\s+");
        if (keywords.length > 1) {
            for (String keyword : keywords) {
                if (keyword.length() > 1) { // 1글자 키워드는 제외
                    List<NaverShoppingItem> keywordResults = itemRepository.findByTitleContainingIgnoreCase(keyword);
                    // 중복 제거하면서 추가
                    for (NaverShoppingItem item : keywordResults) {
                        if (!products.stream().anyMatch(p -> p.getId().equals(item.getId()))) {
                            products.add(item);
                        }
                    }
                    log.info("키워드 '{}'로 추가 {}개 상품 발견", keyword, keywordResults.size());
                }
            }
        }
        
        // 3. 브랜드 검색 (나이키, 아디다스 등)
        if (query.contains("나이키") || query.contains("nike")) {
            List<NaverShoppingItem> brandResults = itemRepository.findByBrand("나이키");
            for (NaverShoppingItem item : brandResults) {
                if (!products.stream().anyMatch(p -> p.getId().equals(item.getId()))) {
                    products.add(item);
                }
            }
            log.info("브랜드 '나이키'로 추가 {}개 상품 발견", brandResults.size());
        }
        
        // 4. 카테고리 검색 (운동화, 신발 등)
        if (query.contains("운동화") || query.contains("신발")) {
            // category2가 "운동화"인 상품들을 검색
            List<NaverShoppingItem> allItems = itemRepository.findAll();
            List<NaverShoppingItem> categoryResults = allItems.stream()
                    .filter(item -> "운동화".equals(item.getCategory2()))
                    .collect(Collectors.toList());
            
            for (NaverShoppingItem item : categoryResults) {
                if (!products.stream().anyMatch(p -> p.getId().equals(item.getId()))) {
                    products.add(item);
                }
            }
            log.info("카테고리 '운동화'로 추가 {}개 상품 발견", categoryResults.size());
        }
        
        log.info("총 {}개 상품 검색 완료", products.size());
        return products;
    }

    /**
     * 상품 목록을 가격순으로 정렬
     * 
     * @param products 정렬할 상품 목록
     * @param sortOrder 정렬 순서 ("asc": 오름차순, "desc": 내림차순)
     * @return 정렬된 상품 목록
     */
    public List<NaverShoppingItem> sortProductsByPrice(List<NaverShoppingItem> products, String sortOrder) {
        if (products == null || products.isEmpty()) {
            return products;
        }
        
        return products.stream()
                .sorted((a, b) -> {
                    // null 값 처리
                    if (a.getLprice() == null && b.getLprice() == null) return 0;
                    if (a.getLprice() == null) return 1;
                    if (b.getLprice() == null) return -1;
                    
                    // 정렬 순서에 따라 비교
                    int comparison = Integer.compare(a.getLprice(), b.getLprice());
                    return "desc".equalsIgnoreCase(sortOrder) ? -comparison : comparison;
                })
                .collect(Collectors.toList());
    }
}
