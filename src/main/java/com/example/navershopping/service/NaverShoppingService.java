package com.example.navershopping.service;

import com.example.navershopping.dto.NaverShoppingResponse;
import com.example.navershopping.entity.NaverShoppingItem;
import com.example.navershopping.entity.NaverShoppingSearch;
import com.example.navershopping.repository.NaverShoppingItemRepository;
import com.example.navershopping.repository.NaverShoppingSearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class NaverShoppingService {

    private final RestTemplate restTemplate;
    private final NaverShoppingItemRepository itemRepository;
    private final NaverShoppingSearchRepository searchRepository;
    
    @Value("${naver.shopping.client.id}")
    private String clientId;
    
    @Value("${naver.shopping.client.secret}")
    private String clientSecret;

    public NaverShoppingService(RestTemplate restTemplate, NaverShoppingItemRepository itemRepository, NaverShoppingSearchRepository searchRepository) {
        this.restTemplate = restTemplate;
        this.itemRepository = itemRepository;
        this.searchRepository = searchRepository;
    }

    public NaverShoppingResponse searchProducts(String query, int display, int start) {
        String url = String.format("https://openapi.naver.com/v1/search/shop.json?query=%s&display=%d&start=%d", 
                                 query, display, start);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<NaverShoppingResponse> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, NaverShoppingResponse.class);
        
        return response.getBody();
    }

    public NaverShoppingResponse searchProducts(String query) {
        return searchProducts(query, 10, 1);
    }

    public NaverShoppingResponse searchProducts(String query, int display) {
        return searchProducts(query, display, 1);
    }

    /**
     * 네이버 쇼핑 API를 호출하고 결과를 데이터베이스에 저장
     */
    @Transactional
    public NaverShoppingResponse searchAndSaveProducts(String query, int display, int start) {
        log.info("네이버 쇼핑 API 호출 시작 - 쿼리: {}, display: {}, start: {}", query, display, start);
        
        // API 호출
        NaverShoppingResponse response = searchProducts(query, display, start);
        
        if (response != null && response.getItems() != null) {
            // 검색 기록 저장
            saveSearchRecord(query, response, display, start);
            
            // 상품 아이템들 저장
            saveProductItems(query, response.getItems());
            
            log.info("네이버 쇼핑 API 결과 저장 완료 - 총 {}개 상품", response.getItems().size());
        }
        
        return response;
    }

    /**
     * 검색 기록을 데이터베이스에 저장
     */
    @Transactional
    public NaverShoppingSearch saveSearchRecord(String query, NaverShoppingResponse response, int display, int start) {
        NaverShoppingSearch searchRecord = NaverShoppingSearch.builder()
                .query(query)
                .totalResults(response.getTotal())
                .displayCount(display)
                .startIndex(start)
                .lastBuildDate(response.getLastBuildDate())
                .build();
        
        return searchRepository.save(searchRecord);
    }

    /**
     * 상품 아이템들을 데이터베이스에 저장 또는 업데이트
     */
    @Transactional
    public List<NaverShoppingItem> saveProductItems(String searchQuery, List<NaverShoppingResponse.Item> items) {
        return items.stream()
                .map(item -> saveOrUpdateProductItem(searchQuery, item))
                .toList();
    }

    /**
     * 개별 상품 아이템을 데이터베이스에 저장 또는 업데이트
     */
    @Transactional
    public NaverShoppingItem saveOrUpdateProductItem(String searchQuery, NaverShoppingResponse.Item item) {
        // 기존 상품 조회
        Optional<NaverShoppingItem> existingItemOpt = itemRepository.findByProductId(item.getProductId());
        
        if (existingItemOpt.isPresent()) {
            // 기존 상품 업데이트
            NaverShoppingItem existingItem = existingItemOpt.get();
            log.debug("기존 상품 업데이트 - ProductId: {}", item.getProductId());
            
            // 상품 정보 업데이트
            existingItem.setTitle(item.getTitle());
            existingItem.setLink(item.getLink());
            existingItem.setImage(item.getImage());
            existingItem.setLprice(item.getLprice());
            existingItem.setHprice(item.getHprice());
            existingItem.setMallName(item.getMallName());
            existingItem.setProductType(item.getProductType());
            existingItem.setBrand(item.getBrand());
            existingItem.setMaker(item.getMaker());
            existingItem.setCategory1(item.getCategory1());
            existingItem.setCategory2(item.getCategory2());
            existingItem.setCategory3(item.getCategory3());
            existingItem.setCategory4(item.getCategory4());
            existingItem.setSearchQuery(searchQuery);
            existingItem.setLastSearchedAt(LocalDateTime.now());
            existingItem.setSearchCount(existingItem.getSearchCount() + 1);
            
            return itemRepository.save(existingItem);
        } else {
            // 새 상품 저장
            log.debug("새 상품 저장 - ProductId: {}", item.getProductId());
            
            NaverShoppingItem newItem = NaverShoppingItem.builder()
                    .productId(item.getProductId())
                    .title(item.getTitle())
                    .link(item.getLink())
                    .image(item.getImage())
                    .lprice(item.getLprice())
                    .hprice(item.getHprice())
                    .mallName(item.getMallName())
                    .productType(item.getProductType())
                    .brand(item.getBrand())
                    .maker(item.getMaker())
                    .category1(item.getCategory1())
                    .category2(item.getCategory2())
                    .category3(item.getCategory3())
                    .category4(item.getCategory4())
                    .searchQuery(searchQuery)
                    .lastSearchedAt(LocalDateTime.now())
                    .searchCount(1)
                    .build();
            
            return itemRepository.save(newItem);
        }
    }

    /**
     * 저장된 상품 검색
     */
    public List<NaverShoppingItem> getSavedProductsByQuery(String query) {
        return itemRepository.findBySearchQuery(query);
    }

    /**
     * 저장된 상품 검색 (제목 포함)
     */
    public List<NaverShoppingItem> getSavedProductsByTitle(String keyword) {
        return itemRepository.findByTitleContaining(keyword);
    }

    /**
     * 저장된 상품 검색 (가격 범위)
     */
    public List<NaverShoppingItem> getSavedProductsByPriceRange(Integer minPrice, Integer maxPrice) {
        return itemRepository.findByPriceRange(minPrice, maxPrice);
    }

    /**
     * 저장된 상품 검색 (카테고리)
     */
    public List<NaverShoppingItem> getSavedProductsByCategory(String category1) {
        return itemRepository.findByCategory1(category1);
    }

    /**
     * 저장된 상품 검색 (카테고리1, 카테고리2)
     */
    public List<NaverShoppingItem> getSavedProductsByCategory(String category1, String category2) {
        return itemRepository.findByCategory1AndCategory2(category1, category2);
    }

    /**
     * 저장된 상품 검색 (몰명)
     */
    public List<NaverShoppingItem> getSavedProductsByMall(String mallName) {
        return itemRepository.findByMallName(mallName);
    }

    /**
     * 저장된 상품 검색 (브랜드)
     */
    public List<NaverShoppingItem> getSavedProductsByBrand(String brand) {
        return itemRepository.findByBrand(brand);
    }

    /**
     * 모든 저장된 상품 조회
     */
    public List<NaverShoppingItem> getAllSavedProducts() {
        return itemRepository.findAll();
    }

    /**
     * 검색 기록 조회
     */
    public List<NaverShoppingSearch> getSearchHistory() {
        return searchRepository.findAllOrderByCreatedAtDesc();
    }

    /**
     * 특정 쿼리의 검색 기록 조회
     */
    public List<NaverShoppingSearch> getSearchHistoryByQuery(String query) {
        return searchRepository.findByQuery(query);
    }

    /**
     * 고유한 몰명 목록 조회
     */
    public List<String> getDistinctMallNames() {
        return itemRepository.findDistinctMallNames();
    }

    /**
     * 고유한 브랜드 목록 조회
     */
    public List<String> getDistinctBrands() {
        return itemRepository.findDistinctBrands();
    }

    /**
     * 고유한 카테고리1 목록 조회
     */
    public List<String> getDistinctCategory1() {
        return itemRepository.findDistinctCategory1();
    }

    /**
     * 특정 쿼리의 검색 횟수 조회
     */
    public Long getSearchCountByQuery(String query) {
        return searchRepository.countByQuery(query);
    }

    /**
     * 상품 업데이트 통계 조회
     */
    public Map<String, Object> getUpdateStats() {
        List<NaverShoppingItem> allItems = itemRepository.findAll();
        
        long totalItems = allItems.size();
        long updatedItems = allItems.stream()
                .filter(item -> item.getSearchCount() > 1)
                .count();
        long newItems = allItems.stream()
                .filter(item -> item.getSearchCount() == 1)
                .count();
        
        return Map.of(
            "totalItems", totalItems,
            "updatedItems", updatedItems,
            "newItems", newItems,
            "updateRate", totalItems > 0 ? (double) updatedItems / totalItems * 100 : 0.0
        );
    }

    /**
     * 최근 업데이트된 상품 조회
     */
    public List<NaverShoppingItem> getRecentlyUpdatedProducts(int limit) {
        return itemRepository.findAll().stream()
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                .limit(limit)
                .toList();
    }

    /**
     * 자주 검색된 상품 조회
     */
    public List<NaverShoppingItem> getMostSearchedProducts(int limit) {
        return itemRepository.findAll().stream()
                .sorted((a, b) -> b.getSearchCount().compareTo(a.getSearchCount()))
                .limit(limit)
                .toList();
    }

    /**
     * 특정 기간 내에 업데이트된 상품 조회
     */
    public List<NaverShoppingItem> getProductsUpdatedAfter(LocalDateTime after) {
        return itemRepository.findAll().stream()
                .filter(item -> item.getUpdatedAt().isAfter(after))
                .toList();
    }

    /**
     * 상품 정보 강제 업데이트 (특정 상품 ID)
     */
    @Transactional
    public NaverShoppingItem forceUpdateProduct(String productId, String searchQuery) {
        Optional<NaverShoppingItem> existingItem = itemRepository.findByProductId(productId);
        
        if (existingItem.isPresent()) {
            NaverShoppingItem item = existingItem.get();
            item.setLastSearchedAt(LocalDateTime.now());
            item.setSearchCount(item.getSearchCount() + 1);
            item.setSearchQuery(searchQuery);
            return itemRepository.save(item);
        }
        
        return null;
    }

    /**
     * 인기 상품 조회 (검색 횟수 기준)
     * 
     * @return 인기 상품 목록
     */
    public List<NaverShoppingItem> getPopularProducts() {
        log.info("인기 상품 조회 시작");
        return itemRepository.findTop10ByOrderBySearchCountDesc();
    }

    /**
     * 최신 상품 조회 (최근 검색 시간 기준)
     * 
     * @return 최신 상품 목록
     */
    public List<NaverShoppingItem> getRecentProducts() {
        log.info("최신 상품 조회 시작");
        return itemRepository.findTop10ByOrderByLastSearchedAtDesc();
    }
}