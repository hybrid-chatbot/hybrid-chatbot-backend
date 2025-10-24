package com.example.demo.service;

import com.example.demo.dto.AiServerResponse;
import com.example.navershopping.entity.NaverShoppingItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.*;

/**
 * RAG 모델 의도분석 결과를 기반으로 동적 SQL 쿼리를 생성하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicSqlQueryService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * LLM 분석 결과를 기반으로 동적 SQL 쿼리 생성
     * 
     * @param query 사용자 검색어
     * @param aiResponse LLM 분석 결과
     * @return 생성된 SQL 쿼리와 파라미터
     */
    public DynamicQueryResult generateDynamicQuery(String query, AiServerResponse aiResponse) {
        log.info("동적 SQL 쿼리 생성 시작 - 검색어: {}, 의도: {}", query, 
                aiResponse != null ? aiResponse.getFinal_intent() : "unknown");
        
        if (aiResponse == null) {
            return generateFallbackQuery(query);
        }
        
        String intent = aiResponse.getFinal_intent();
        float confidence = aiResponse.getConfidence() != null ? aiResponse.getConfidence() : 0.0f;
        
        // 의도별 동적 쿼리 생성
        switch (intent.toLowerCase()) {
            case "product_search":
                return generateProductSearchQuery(query, confidence);
            case "product_recommendation":
                return generateRecommendationQuery(query, confidence);
            case "product_filter":
                return generateFilterQuery(query, confidence);
            case "product_compare":
                return generateCompareQuery(query, confidence);
            case "brand_search":
                return generateBrandSearchQuery(query, confidence);
            case "category_search":
                return generateCategorySearchQuery(query, confidence);
            case "price_range_search":
                return generatePriceRangeQuery(query, confidence);
            default:
                log.warn("알 수 없는 의도: {}, 기본 검색 쿼리 생성", intent);
                return generateFallbackQuery(query);
        }
    }
    
    /**
     * 일반 상품 검색 쿼리 생성
     */
    private DynamicQueryResult generateProductSearchQuery(String query, float confidence) {
        log.info("일반 상품 검색 쿼리 생성 - 신뢰도: {}", confidence);
        
        StringBuilder sql = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        
        // 기본 검색 조건
        sql.append("SELECT * FROM naver_shopping_item WHERE ");
        
        // 신뢰도에 따른 검색 전략
        if (confidence > 0.8f) {
            // 높은 신뢰도: 정확한 매칭 우선
            sql.append("(title LIKE :exactTitle OR brand LIKE :exactBrand) ");
            parameters.put("exactTitle", "%" + query + "%");
            parameters.put("exactBrand", "%" + query + "%");
        } else if (confidence > 0.6f) {
            // 중간 신뢰도: 키워드 분리 검색
            String[] keywords = extractKeywords(query);
            sql.append("(");
            for (int i = 0; i < keywords.length; i++) {
                if (i > 0) sql.append(" AND ");
                sql.append("(title LIKE :keyword").append(i).append(" OR brand LIKE :keyword").append(i).append(")");
                parameters.put("keyword" + i, "%" + keywords[i] + "%");
            }
            sql.append(")");
        } else {
            // 낮은 신뢰도: 유연한 검색
            sql.append("(title LIKE :flexibleTitle OR brand LIKE :flexibleBrand OR category1 LIKE :flexibleCategory) ");
            parameters.put("flexibleTitle", "%" + query + "%");
            parameters.put("flexibleBrand", "%" + query + "%");
            parameters.put("flexibleCategory", "%" + query + "%");
        }
        
        // 정렬 조건
        sql.append(" ORDER BY ");
        if (confidence > 0.7f) {
            sql.append("CASE WHEN title LIKE :orderTitle THEN 1 ELSE 2 END, ");
            parameters.put("orderTitle", "%" + query + "%");
        }
        sql.append("price ASC LIMIT 20");
        
        return new DynamicQueryResult(sql.toString(), parameters, "product_search");
    }
    
    /**
     * 상품 추천 쿼리 생성
     */
    private DynamicQueryResult generateRecommendationQuery(String query, float confidence) {
        log.info("상품 추천 쿼리 생성 - 신뢰도: {}", confidence);
        
        StringBuilder sql = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        
        sql.append("SELECT * FROM naver_shopping_item WHERE ");
        
        // 추천 로직: 인기 상품 + 관련 상품
        if (confidence > 0.7f) {
            sql.append("(title LIKE :title OR brand LIKE :brand) AND ");
            parameters.put("title", "%" + query + "%");
            parameters.put("brand", "%" + query + "%");
        }
        
        sql.append("(review_count > 10 OR rating > 4.0) ");
        sql.append("ORDER BY rating DESC, review_count DESC LIMIT 15");
        
        return new DynamicQueryResult(sql.toString(), parameters, "product_recommendation");
    }
    
    /**
     * 상품 필터링 쿼리 생성
     */
    private DynamicQueryResult generateFilterQuery(String query, float confidence) {
        log.info("상품 필터링 쿼리 생성 - 신뢰도: {}", confidence);
        
        StringBuilder sql = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        
        sql.append("SELECT * FROM naver_shopping_item WHERE ");
        
        // 기본 검색 조건
        sql.append("(title LIKE :title OR brand LIKE :brand) ");
        parameters.put("title", "%" + query + "%");
        parameters.put("brand", "%" + query + "%");
        
        // 가격 필터 추출
        PriceRange priceRange = extractPriceRange(query);
        if (priceRange != null) {
            sql.append("AND price BETWEEN :minPrice AND :maxPrice ");
            parameters.put("minPrice", priceRange.minPrice);
            parameters.put("maxPrice", priceRange.maxPrice);
        }
        
        // 브랜드 필터 추출
        String brand = extractBrand(query);
        if (brand != null) {
            sql.append("AND brand LIKE :brandFilter ");
            parameters.put("brandFilter", "%" + brand + "%");
        }
        
        sql.append("ORDER BY price ASC LIMIT 20");
        
        return new DynamicQueryResult(sql.toString(), parameters, "product_filter");
    }
    
    /**
     * 상품 비교 쿼리 생성
     */
    private DynamicQueryResult generateCompareQuery(String query, float confidence) {
        log.info("상품 비교 쿼리 생성 - 신뢰도: {}", confidence);
        
        StringBuilder sql = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        
        sql.append("SELECT * FROM naver_shopping_item WHERE ");
        
        // 비교 대상 상품들 추출
        String[] compareItems = extractCompareItems(query);
        sql.append("(");
        for (int i = 0; i < compareItems.length; i++) {
            if (i > 0) sql.append(" OR ");
            sql.append("title LIKE :compareItem").append(i);
            parameters.put("compareItem" + i, "%" + compareItems[i] + "%");
        }
        sql.append(") ");
        
        sql.append("ORDER BY price ASC LIMIT 10");
        
        return new DynamicQueryResult(sql.toString(), parameters, "product_compare");
    }
    
    /**
     * 브랜드별 검색 쿼리 생성
     */
    private DynamicQueryResult generateBrandSearchQuery(String query, float confidence) {
        log.info("브랜드별 검색 쿼리 생성 - 신뢰도: {}", confidence);
        
        StringBuilder sql = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        
        String brand = extractBrand(query);
        if (brand != null) {
            sql.append("SELECT * FROM naver_shopping_item WHERE brand LIKE :brand ");
            parameters.put("brand", "%" + brand + "%");
        } else {
            sql.append("SELECT * FROM naver_shopping_item WHERE brand LIKE :query ");
            parameters.put("query", "%" + query + "%");
        }
        
        sql.append("ORDER BY price ASC LIMIT 20");
        
        return new DynamicQueryResult(sql.toString(), parameters, "brand_search");
    }
    
    /**
     * 카테고리별 검색 쿼리 생성
     */
    private DynamicQueryResult generateCategorySearchQuery(String query, float confidence) {
        log.info("카테고리별 검색 쿼리 생성 - 신뢰도: {}", confidence);
        
        StringBuilder sql = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        
        sql.append("SELECT * FROM naver_shopping_item WHERE ");
        sql.append("(category1 LIKE :category OR category2 LIKE :category) ");
        parameters.put("category", "%" + query + "%");
        
        sql.append("ORDER BY price ASC LIMIT 20");
        
        return new DynamicQueryResult(sql.toString(), parameters, "category_search");
    }
    
    /**
     * 가격대별 검색 쿼리 생성
     */
    private DynamicQueryResult generatePriceRangeQuery(String query, float confidence) {
        log.info("가격대별 검색 쿼리 생성 - 신뢰도: {}", confidence);
        
        StringBuilder sql = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        
        sql.append("SELECT * FROM naver_shopping_item WHERE ");
        
        // 기본 검색 조건
        sql.append("(title LIKE :title OR brand LIKE :brand) ");
        parameters.put("title", "%" + query + "%");
        parameters.put("brand", "%" + query + "%");
        
        // 가격 범위 추출
        PriceRange priceRange = extractPriceRange(query);
        if (priceRange != null) {
            sql.append("AND price BETWEEN :minPrice AND :maxPrice ");
            parameters.put("minPrice", priceRange.minPrice);
            parameters.put("maxPrice", priceRange.maxPrice);
        }
        
        sql.append("ORDER BY price ASC LIMIT 20");
        
        return new DynamicQueryResult(sql.toString(), parameters, "price_range_search");
    }
    
    /**
     * 폴백 쿼리 생성 (의도분석 실패 시)
     */
    private DynamicQueryResult generateFallbackQuery(String query) {
        log.info("폴백 쿼리 생성 - 검색어: {}", query);
        
        StringBuilder sql = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        
        sql.append("SELECT * FROM naver_shopping_item WHERE ");
        sql.append("(title LIKE :title OR brand LIKE :brand OR category1 LIKE :category) ");
        parameters.put("title", "%" + query + "%");
        parameters.put("brand", "%" + query + "%");
        parameters.put("category", "%" + query + "%");
        sql.append("ORDER BY price ASC LIMIT 20");
        
        return new DynamicQueryResult(sql.toString(), parameters, "fallback");
    }
    
    /**
     * 네이티브 SQL 쿼리 실행
     * 
     * @param sql 실행할 SQL 쿼리
     * @return 검색된 상품 목록
     */
    @Transactional(readOnly = true)
    public List<NaverShoppingItem> executeNativeQuery(String sql) {
        try {
            log.info("네이티브 SQL 쿼리 실행: {}", sql);
            
            Query query = entityManager.createNativeQuery(sql, NaverShoppingItem.class);
            @SuppressWarnings("unchecked")
            List<NaverShoppingItem> results = query.getResultList();
            
            log.info("네이티브 SQL 쿼리 실행 완료 - {}개 결과", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("네이티브 SQL 쿼리 실행 실패: {}", sql, e);
            return new ArrayList<>();
        }
    }
    
    // ========== 헬퍼 메서드들 ==========
    
    /**
     * 검색어에서 키워드 추출
     */
    private String[] extractKeywords(String query) {
        return query.split("\\s+");
    }
    
    /**
     * 검색어에서 가격 범위 추출
     */
    private PriceRange extractPriceRange(String query) {
        // "10만원 이하", "5만원~10만원" 등의 패턴 매칭
        // 실제 구현에서는 더 정교한 패턴 매칭 필요
        return null; // 임시로 null 반환
    }
    
    /**
     * 검색어에서 브랜드 추출
     */
    private String extractBrand(String query) {
        String[] brands = {"나이키", "nike", "아디다스", "adidas", "퓨마", "puma", "뉴발란스", "new balance"};
        for (String brand : brands) {
            if (query.toLowerCase().contains(brand.toLowerCase())) {
                return brand;
            }
        }
        return null;
    }
    
    /**
     * 비교 대상 상품들 추출
     */
    private String[] extractCompareItems(String query) {
        // "나이키 vs 아디다스" 형태의 비교 문장에서 상품 추출
        return query.split("\\s+(vs|VS|vs\\.|VS\\.|대|와|과)\\s+");
    }
    
    /**
     * 가격 범위 정보를 담는 내부 클래스
     */
    private static class PriceRange {
        final int minPrice;
        final int maxPrice;
        
        @SuppressWarnings("unused")
        PriceRange(int minPrice, int maxPrice) {
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
    }
    
    /**
     * 동적 쿼리 결과를 담는 클래스
     */
    public static class DynamicQueryResult {
        private final String sql;
        private final Map<String, Object> parameters;
        private final String intentType;
        
        public DynamicQueryResult(String sql, Map<String, Object> parameters, String intentType) {
            this.sql = sql;
            this.parameters = parameters;
            this.intentType = intentType;
        }
        
        public String getSql() { return sql; }
        public Map<String, Object> getParameters() { return parameters; }
        public String getIntentType() { return intentType; }
    }
}
