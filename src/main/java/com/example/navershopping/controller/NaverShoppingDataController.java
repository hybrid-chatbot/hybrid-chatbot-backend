package com.example.navershopping.controller;

import com.example.navershopping.dto.NaverShoppingResponse;
import com.example.navershopping.entity.NaverShoppingItem;
import com.example.navershopping.entity.NaverShoppingSearch;
import com.example.navershopping.service.NaverShoppingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/naver-shopping/data/search")
@RequiredArgsConstructor
public class NaverShoppingDataController {

    private final NaverShoppingService naverShoppingService;

    /**
     * 네이버 쇼핑 API 호출 및 데이터베이스 저장
     */
    @PostMapping("/search-and-save")
    public ResponseEntity<NaverShoppingResponse> searchAndSaveProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int display,
            @RequestParam(defaultValue = "1") int start) {
        
        log.info("네이버 쇼핑 검색 및 저장 요청 - 쿼리: {}, display: {}, start: {}", query, display, start);
        
        try {
            NaverShoppingResponse response = naverShoppingService.searchAndSaveProducts(query, display, start);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("네이버 쇼핑 검색 및 저장 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 네이버 쇼핑 API 호출 (저장 없음)
     */
    @GetMapping("/search")
    public ResponseEntity<NaverShoppingResponse> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int display,
            @RequestParam(defaultValue = "1") int start) {
        
        log.info("네이버 쇼핑 검색 요청 - 쿼리: {}, display: {}, start: {}", query, display, start);
        
        try {
            NaverShoppingResponse response = naverShoppingService.searchProducts(query, display, start);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("네이버 쇼핑 검색 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 저장된 상품 조회 (쿼리별)
     */
    @GetMapping("/saved-products")
    public ResponseEntity<List<NaverShoppingItem>> getSavedProductsByQuery(@RequestParam String query) {
        log.info("저장된 상품 조회 요청 - 쿼리: {}", query);
        
        try {
            List<NaverShoppingItem> products = naverShoppingService.getSavedProductsByQuery(query);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("저장된 상품 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 저장된 상품 조회 (제목 검색)
     */
    @GetMapping("/saved-products/search")
    public ResponseEntity<List<NaverShoppingItem>> searchSavedProductsByTitle(@RequestParam String keyword) {
        log.info("저장된 상품 제목 검색 요청 - 키워드: {}", keyword);
        
        try {
            List<NaverShoppingItem> products = naverShoppingService.getSavedProductsByTitle(keyword);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("저장된 상품 제목 검색 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 저장된 상품 조회 (가격 범위)
     */
    @GetMapping("/saved-products/price-range")
    public ResponseEntity<List<NaverShoppingItem>> getSavedProductsByPriceRange(
            @RequestParam Integer minPrice,
            @RequestParam Integer maxPrice) {
        
        log.info("저장된 상품 가격 범위 검색 요청 - 최소: {}, 최대: {}", minPrice, maxPrice);
        
        try {
            List<NaverShoppingItem> products = naverShoppingService.getSavedProductsByPriceRange(minPrice, maxPrice);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("저장된 상품 가격 범위 검색 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 저장된 상품 조회 (카테고리)
     */
    @GetMapping("/saved-products/category")
    public ResponseEntity<List<NaverShoppingItem>> getSavedProductsByCategory(
            @RequestParam String category1,
            @RequestParam(required = false) String category2) {
        
        log.info("저장된 상품 카테고리 검색 요청 - 카테고리1: {}, 카테고리2: {}", category1, category2);
        
        try {
            List<NaverShoppingItem> products;
            if (category2 != null) {
                products = naverShoppingService.getSavedProductsByCategory(category1, category2);
            } else {
                products = naverShoppingService.getSavedProductsByCategory(category1);
            }
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("저장된 상품 카테고리 검색 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 저장된 상품 조회 (몰명)
     */
    @GetMapping("/saved-products/mall")
    public ResponseEntity<List<NaverShoppingItem>> getSavedProductsByMall(@RequestParam String mallName) {
        log.info("저장된 상품 몰명 검색 요청 - 몰명: {}", mallName);
        
        try {
            List<NaverShoppingItem> products = naverShoppingService.getSavedProductsByMall(mallName);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("저장된 상품 몰명 검색 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 저장된 상품 조회 (브랜드)
     */
    @GetMapping("/saved-products/brand")
    public ResponseEntity<List<NaverShoppingItem>> getSavedProductsByBrand(@RequestParam String brand) {
        log.info("저장된 상품 브랜드 검색 요청 - 브랜드: {}", brand);
        
        try {
            List<NaverShoppingItem> products = naverShoppingService.getSavedProductsByBrand(brand);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("저장된 상품 브랜드 검색 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 모든 저장된 상품 조회
     */
    @GetMapping("/saved-products/all")
    public ResponseEntity<List<NaverShoppingItem>> getAllSavedProducts() {
        log.info("모든 저장된 상품 조회 요청");
        
        try {
            List<NaverShoppingItem> products = naverShoppingService.getAllSavedProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("모든 저장된 상품 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 검색 기록 조회
     */
    @GetMapping("/search-history")
    public ResponseEntity<List<NaverShoppingSearch>> getSearchHistory() {
        log.info("검색 기록 조회 요청");
        
        try {
            List<NaverShoppingSearch> searchHistory = naverShoppingService.getSearchHistory();
            return ResponseEntity.ok(searchHistory);
        } catch (Exception e) {
            log.error("검색 기록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 쿼리의 검색 기록 조회
     */
    @GetMapping("/search-history/query")
    public ResponseEntity<List<NaverShoppingSearch>> getSearchHistoryByQuery(@RequestParam String query) {
        log.info("특정 쿼리 검색 기록 조회 요청 - 쿼리: {}", query);
        
        try {
            List<NaverShoppingSearch> searchHistory = naverShoppingService.getSearchHistoryByQuery(query);
            return ResponseEntity.ok(searchHistory);
        } catch (Exception e) {
            log.error("특정 쿼리 검색 기록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 고유한 몰명 목록 조회
     */
    @GetMapping("/malls")
    public ResponseEntity<List<String>> getDistinctMallNames() {
        log.info("고유한 몰명 목록 조회 요청");
        
        try {
            List<String> mallNames = naverShoppingService.getDistinctMallNames();
            return ResponseEntity.ok(mallNames);
        } catch (Exception e) {
            log.error("고유한 몰명 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 고유한 브랜드 목록 조회
     */
    @GetMapping("/brands")
    public ResponseEntity<List<String>> getDistinctBrands() {
        log.info("고유한 브랜드 목록 조회 요청");
        
        try {
            List<String> brands = naverShoppingService.getDistinctBrands();
            return ResponseEntity.ok(brands);
        } catch (Exception e) {
            log.error("고유한 브랜드 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 고유한 카테고리1 목록 조회
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getDistinctCategory1() {
        log.info("고유한 카테고리1 목록 조회 요청");
        
        try {
            List<String> categories = naverShoppingService.getDistinctCategory1();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("고유한 카테고리1 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 쿼리의 검색 횟수 조회
     */
    @GetMapping("/search-count")
    public ResponseEntity<Map<String, Object>> getSearchCountByQuery(@RequestParam String query) {
        log.info("특정 쿼리 검색 횟수 조회 요청 - 쿼리: {}", query);
        
        try {
            Long count = naverShoppingService.getSearchCountByQuery(query);
            return ResponseEntity.ok(Map.of("query", query, "count", count));
        } catch (Exception e) {
            log.error("특정 쿼리 검색 횟수 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 데이터베이스 통계 정보 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDatabaseStats() {
        log.info("데이터베이스 통계 정보 조회 요청");
        
        try {
            long totalProducts = naverShoppingService.getAllSavedProducts().size();
            long totalSearches = naverShoppingService.getSearchHistory().size();
            List<String> mallNames = naverShoppingService.getDistinctMallNames();
            List<String> brands = naverShoppingService.getDistinctBrands();
            List<String> categories = naverShoppingService.getDistinctCategory1();
            
            Map<String, Object> stats = Map.of(
                "totalProducts", totalProducts,
                "totalSearches", totalSearches,
                "totalMalls", mallNames.size(),
                "totalBrands", brands.size(),
                "totalCategories", categories.size()
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("데이터베이스 통계 정보 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 상품 업데이트 통계 조회
     */
    @GetMapping("/update-stats")
    public ResponseEntity<Map<String, Object>> getUpdateStats() {
        log.info("상품 업데이트 통계 조회 요청");
        
        try {
            Map<String, Object> updateStats = naverShoppingService.getUpdateStats();
            return ResponseEntity.ok(updateStats);
        } catch (Exception e) {
            log.error("상품 업데이트 통계 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 최근 업데이트된 상품 조회
     */
    @GetMapping("/recently-updated")
    public ResponseEntity<List<NaverShoppingItem>> getRecentlyUpdatedProducts(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("최근 업데이트된 상품 조회 요청 - limit: {}", limit);
        
        try {
            List<NaverShoppingItem> products = naverShoppingService.getRecentlyUpdatedProducts(limit);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("최근 업데이트된 상품 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 자주 검색된 상품 조회
     */
    @GetMapping("/most-searched")
    public ResponseEntity<List<NaverShoppingItem>> getMostSearchedProducts(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("자주 검색된 상품 조회 요청 - limit: {}", limit);
        
        try {
            List<NaverShoppingItem> products = naverShoppingService.getMostSearchedProducts(limit);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("자주 검색된 상품 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 상품 강제 업데이트
     */
    @PostMapping("/force-update/{productId}")
    public ResponseEntity<NaverShoppingItem> forceUpdateProduct(
            @PathVariable String productId,
            @RequestParam String searchQuery) {
        log.info("상품 강제 업데이트 요청 - ProductId: {}, SearchQuery: {}", productId, searchQuery);
        
        try {
            NaverShoppingItem updatedItem = naverShoppingService.forceUpdateProduct(productId, searchQuery);
            if (updatedItem != null) {
                return ResponseEntity.ok(updatedItem);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("상품 강제 업데이트 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}