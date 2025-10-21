package com.example.demo.service;

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
    private final NaverShoppingService naverShoppingService;
    private final KeywordAnalyzerService keywordAnalyzerService;

    /**
     * 추천 상품 검색 (가격대와 브랜드 기반)
     */
    public List<NaverShoppingItem> executeRecommendationSearch(String query) {
        log.info("추천 상품 검색 실행: {}", query);
        
        // 1. 기본 검색어로 상품 검색
        List<NaverShoppingItem> products = itemRepository.findAll().stream()
                .filter(item -> item.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                               (item.getBrand() != null && item.getBrand().toLowerCase().contains(query.toLowerCase())))
                .limit(10)
                .collect(Collectors.toList());
        
        // 2. 결과가 부족하면 네이버 API 호출
        if (products.size() < 5) {
            naverShoppingService.searchAndSaveProducts(query, 20, 1);
            products = itemRepository.findAll().stream()
                    .filter(item -> item.getTitle().toLowerCase().contains(query.toLowerCase()))
                    .limit(10)
                    .collect(Collectors.toList());
        }
        
        // 3. 추천 로직: 가격대별로 다양하게 추천
        if (products.size() > 5) {
            // 가격대별로 분산하여 추천
            List<NaverShoppingItem> lowPrice = products.stream()
                    .filter(item -> item.getLprice() < 50000)
                    .limit(3)
                    .collect(Collectors.toList());
            
            List<NaverShoppingItem> midPrice = products.stream()
                    .filter(item -> item.getLprice() >= 50000 && item.getLprice() < 100000)
                    .limit(3)
                    .collect(Collectors.toList());
            
            List<NaverShoppingItem> highPrice = products.stream()
                    .filter(item -> item.getLprice() >= 100000)
                    .limit(3)
                    .collect(Collectors.toList());
            
            products = new ArrayList<>();
            products.addAll(lowPrice);
            products.addAll(midPrice);
            products.addAll(highPrice);
        }
        
        return products;
    }

    /**
     * 필터링 상품 검색
     */
    public List<NaverShoppingItem> executeFilterSearch(String query) {
        log.info("필터링 상품 검색 실행: {}", query);
        
        List<NaverShoppingItem> products = searchProductsInDatabase(query);
        
        // 가격대 필터링 (예: "10만원 이하", "5만원~10만원")
        if (query.contains("만원")) {
            products = products.stream()
                    .filter(item -> {
                        int price = item.getLprice();
                        if (query.contains("이하")) {
                            return price <= 100000; // 10만원 이하
                        } else if (query.contains("~")) {
                            return price >= 50000 && price <= 100000; // 5만원~10만원
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        }
        
        return products;
    }

    /**
     * 비교 상품 검색
     */
    public List<NaverShoppingItem> executeComparisonSearch(String query) {
        log.info("비교 상품 검색 실행: {}", query);
        
        // 여러 키워드로 검색하여 비교 대상 상품들 수집
        String[] keywords = query.split("\\s+");
        List<NaverShoppingItem> products = new ArrayList<>();
        
        for (String keyword : keywords) {
            if (keyword.length() > 1) {
                List<NaverShoppingItem> keywordResults = itemRepository.findByTitleContainingIgnoreCase(keyword);
                products.addAll(keywordResults);
            }
        }
        
        // 중복 제거 및 최대 6개로 제한
        return products.stream()
                .distinct()
                .limit(6)
                .collect(Collectors.toList());
    }

    /**
     * 일반 상품 검색
     */
    public List<NaverShoppingItem> executeGeneralSearch(String query) {
        log.info("일반 상품 검색 실행: {}", query);
        
        // 기존 searchProductsInDatabase 로직 사용
        List<NaverShoppingItem> products = searchProductsInDatabase(query);
        
        // 결과가 부족하면 네이버 API 호출
        if (products.size() < 5) {
            naverShoppingService.searchAndSaveProducts(query, 20, 1);
            products = searchProductsInDatabase(query);
        }
        
        return products;
    }

    /**
     * 브랜드별 상품 검색
     */
    public List<NaverShoppingItem> executeBrandSearch(String query) {
        log.info("브랜드별 상품 검색 실행: {}", query);
        
        return itemRepository.findAll().stream()
                .filter(item -> {
                    String brand = item.getBrand();
                    return brand != null && brand.toLowerCase().contains(query.toLowerCase());
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 상품 검색
     */
    public List<NaverShoppingItem> executeCategorySearch(String query) {
        log.info("카테고리별 상품 검색 실행: {}", query);
        
        return itemRepository.findAll().stream()
                .filter(item -> {
                    String category1 = item.getCategory1();
                    String category2 = item.getCategory2();
                    return (category1 != null && category1.toLowerCase().contains(query.toLowerCase())) ||
                           (category2 != null && category2.toLowerCase().contains(query.toLowerCase()));
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 가격대별 상품 검색
     */
    public List<NaverShoppingItem> executePriceRangeSearch(String query) {
        log.info("가격대별 상품 검색 실행: {}", query);
        
        return itemRepository.findAll().stream()
                .filter(item -> {
                    String title = item.getTitle().toLowerCase();
                    return title.contains(query.toLowerCase());
                })
                .filter(item -> {
                    // 가격대 필터링 (예: "10만원 이하", "5만원~10만원")
                    if (query.contains("만원")) {
                        int price = item.getLprice();
                        if (query.contains("이하")) {
                            return price <= 100000; // 10만원 이하
                        } else if (query.contains("~")) {
                            return price >= 50000 && price <= 100000; // 5만원~10만원
                        }
                    }
                    return true;
                })
                .limit(10)
                .collect(Collectors.toList());
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
