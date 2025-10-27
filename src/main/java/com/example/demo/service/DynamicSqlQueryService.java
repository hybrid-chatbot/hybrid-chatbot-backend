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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
        sql.append("SELECT * FROM naver_shopping_items WHERE ");
        
        // 브랜드 필터 추출 (DB 기반)
        String brand = extractBrand(query);
        if (brand != null) {
            sql.append("brand LIKE :brandFilter ");
            parameters.put("brandFilter", "%" + brand + "%");
            log.info("추출된 브랜드: {}", brand);
        } else {
            // 브랜드 매칭 실패 시 일반 키워드 검색으로 폴백
            log.info("브랜드 매칭 실패 - 일반 키워드 검색으로 폴백");
            sql.append("(title LIKE :query OR brand LIKE :query) ");
            parameters.put("query", "%" + query + "%");
        }
        
        // 신뢰도에 따른 추가 검색 조건
        if (confidence > 0.8f) {
            // 높은 신뢰도: 정확한 매칭 우선
            sql.append("AND (title LIKE :exactTitle OR brand LIKE :exactBrand) ");
            parameters.put("exactTitle", "%" + query + "%");
            parameters.put("exactBrand", "%" + query + "%");
        } else if (confidence > 0.6f) {
            // 중간 신뢰도: 키워드 분리 검색
            String[] keywords = extractKeywords(query);
            if (keywords.length > 1) {
                sql.append("AND (");
                for (int i = 0; i < keywords.length; i++) {
                    if (i > 0) sql.append(" AND ");
                    sql.append("(title LIKE :keyword").append(i).append(" OR brand LIKE :keyword").append(i).append(")");
                    parameters.put("keyword" + i, "%" + keywords[i] + "%");
                }
                sql.append(")");
            }
        }
        
        // 정렬 방식 추출
        String sortOrder = extractSortOrder(query);
        sql.append(" ORDER BY price ").append(sortOrder);
        log.info("추출된 정렬 방식: {}", sortOrder);
        
        // 개수 추출
        Integer count = extractCount(query);
        sql.append(" LIMIT ").append(count);
        log.info("추출된 개수: {}", count);
        
        return new DynamicQueryResult(sql.toString(), parameters, "product_search");
    }
    
    /**
     * 상품 추천 쿼리 생성
     */
    private DynamicQueryResult generateRecommendationQuery(String query, float confidence) {
        log.info("상품 추천 쿼리 생성 - 신뢰도: {}", confidence);
        
        StringBuilder sql = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        
        sql.append("SELECT * FROM naver_shopping_items WHERE ");
        
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
        
        sql.append("SELECT * FROM naver_shopping_items WHERE ");
        
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
        
        sql.append("SELECT * FROM naver_shopping_items WHERE ");
        
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
            sql.append("SELECT * FROM naver_shopping_items WHERE brand LIKE :brand ");
            parameters.put("brand", "%" + brand + "%");
        } else {
            sql.append("SELECT * FROM naver_shopping_items WHERE brand LIKE :query ");
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
        
        sql.append("SELECT * FROM naver_shopping_items WHERE ");
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
        
        sql.append("SELECT * FROM naver_shopping_items WHERE ");
        
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
        
        sql.append("SELECT * FROM naver_shopping_items WHERE ");
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
     * @param parameters 쿼리 파라미터
     * @return 검색된 상품 목록
     */
    @Transactional(readOnly = true)
    public List<NaverShoppingItem> executeNativeQuery(String sql, Map<String, Object> parameters) {
        try {
            log.info("네이티브 SQL 쿼리 실행: {}", sql);
            log.info("파라미터: {}", parameters);
            
            Query query = entityManager.createNativeQuery(sql, NaverShoppingItem.class);
            
            // 파라미터 바인딩
            if (parameters != null) {
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
            }
            
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
     * 검색어에서 브랜드 추출 (브랜드 검색 → 결과 없으면 null 반환하여 일반 검색으로 폴백)
     */
    private String extractBrand(String query) {
        // DB에서 실제 브랜드 목록 조회
        List<String> existingBrands = getBrandsFromDatabase();
        
        // 입력된 키워드의 언어 구분
        boolean isKoreanInput = isKoreanText(query);
        log.info("입력 언어 구분: {} (한글: {})", query, isKoreanInput);
        
        String matchedBrand = null;
        
        if (isKoreanInput) {
            // 한글 입력 → 한글 브랜드 검색
            matchedBrand = findKoreanBrand(query, existingBrands);
            if (matchedBrand == null) {
                // 한글 브랜드 검색 실패 → null 반환하여 일반 키워드 검색으로 폴백
                log.info("한글 브랜드 검색 실패 → 일반 키워드 검색으로 폴백");
                return null;
            }
            log.info("브랜드 매칭 성공: {} → {}", query, matchedBrand);
            return matchedBrand;
        } else {
            // 영어 입력 → 영어 브랜드 검색
            matchedBrand = findEnglishBrand(query, existingBrands);
            if (matchedBrand == null) {
                // 영어 브랜드 검색 실패 → null 반환하여 일반 키워드 검색으로 폴백
                log.info("영어 브랜드 검색 실패 → 일반 키워드 검색으로 폴백");
                return null;
            }
            log.info("브랜드 매칭 성공: {} → {}", query, matchedBrand);
            return matchedBrand;
        }
    }
    
    /**
     * 입력된 텍스트가 한글인지 구분
     */
    private boolean isKoreanText(String text) {
        // 한글 문자 범위: \uAC00-\uD7AF (완성형 한글)
        // 한글 자모 범위: \u1100-\u11FF (한글 자모)
        // 한글 호환 자모 범위: \u3130-\u318F (한글 호환 자모)
        return text.matches(".*[\\uAC00-\\uD7AF\\u1100-\\u11FF\\u3130-\\u318F].*");
    }
    
    
    /**
     * 한글 브랜드 매칭
     */
    private String findKoreanBrand(String query, List<String> existingBrands) {
        for (String brand : existingBrands) {
            if (brand != null && !brand.trim().isEmpty()) {
                // 정확한 매칭 (대소문자 무시)
                if (query.toLowerCase().contains(brand.toLowerCase())) {
                    return brand;
                }
                
                // 부분 매칭 (브랜드명이 긴 경우)
                if (brand.toLowerCase().contains(query.toLowerCase()) && 
                    query.length() >= 2) {
                    return brand;
                }
            }
        }
        return null;
    }
    
    /**
     * 영어 브랜드 매칭 (직접 매칭만)
     */
    private String findEnglishBrand(String query, List<String> existingBrands) {
        // 영어 브랜드 직접 매칭
        for (String brand : existingBrands) {
            if (brand != null && !brand.trim().isEmpty()) {
                // 정확한 매칭 (대소문자 무시)
                if (query.toLowerCase().contains(brand.toLowerCase())) {
                    return brand;
                }
                
                // 부분 매칭 (브랜드명이 긴 경우)
                if (brand.toLowerCase().contains(query.toLowerCase()) && 
                    query.length() >= 2) {
                    return brand;
                }
            }
        }
        
        return null;
    }
    
    
    // 브랜드 목록 캐시 (메모리에 저장)
    private List<String> cachedBrands = null;
    
    /**
     * DB에서 실제 브랜드 목록 조회 (캐싱 적용)
     */
    private List<String> getBrandsFromDatabase() {
        // 캐시가 있으면 재사용
        if (cachedBrands != null) {
            log.debug("캐시된 브랜드 목록 사용 ({}개)", cachedBrands.size());
            return cachedBrands;
        }
        
        // 1. DB에서 브랜드 조회
        String sql = "SELECT DISTINCT brand FROM naver_shopping_items WHERE brand IS NOT NULL AND brand != '' ORDER BY brand";
        
        List<String> dbBrands = new ArrayList<>();
        try {
            Query query = entityManager.createNativeQuery(sql);
            @SuppressWarnings("unchecked")
            List<String> brands = query.getResultList();
            dbBrands = brands;
            log.info("DB에서 조회된 브랜드 수: {}", dbBrands.size());
        } catch (Exception e) {
            log.error("브랜드 목록 조회 중 오류 발생", e);
        }
        
        // 2. 하드코딩된 일반 브랜드 목록 추가 (DB에 없어도 인식 가능)
        List<String> commonBrands = List.of(
            "나이키", "nike", "아디다스", "adidas", "퓨마", "puma",
            "뉴발란스", "new balance", "컨버스", "converse",
            "반스", "vans", "조던", "jordan", "언더아머", "under armour",
            "아식스", "asics", "뉴에라", "new era",
            "아이폰", "iphone", "삼성", "samsung", "갤럭시", "galaxy",
            "LG", "lg", "샤오미", "xiaomi", "화웨이", "huawei"
        );
        
        // 3. DB 브랜드 + 일반 브랜드 합치기
        List<String> allBrands = new ArrayList<>(dbBrands);
        for (String brand : commonBrands) {
            if (!allBrands.contains(brand)) {
                allBrands.add(brand);
            }
        }
        
        log.info("전체 브랜드 수: {} (DB: {}, 추가: {})", 
                allBrands.size(), dbBrands.size(), allBrands.size() - dbBrands.size());
        
        // 캐시에 저장
        cachedBrands = allBrands;
        return allBrands;
    }
    
    /**
     * 검색어에서 개수 추출
     */
    private Integer extractCount(String query) {
        // 다양한 패턴 시도
        String[] patterns = {
            "(\\d+)개",           // "5개"
            "(\\d+)개씩",         // "5개씩"
            "(\\d+)개만",         // "5개만"
            "(\\d+)개 정도",      // "5개 정도"
            "(\\d+)개 정도로",    // "5개 정도로"
            "(\\d+)개 정도만",    // "5개 정도만"
            "(\\d+)개만큼",       // "5개만큼"
            "(\\d+)개 정도만큼",  // "5개 정도만큼"
            "(\\d+)개 정도만큼만", // "5개 정도만큼만"
            "(\\d+)개 정도만큼만큼" // "5개 정도만큼만큼"
        };
        
        for (String patternStr : patterns) {
            try {
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(query);
                
                if (matcher.find()) {
                    int count = Integer.parseInt(matcher.group(1));
                    
                    // 유효 범위 체크 (1~100개)
                    if (count > 0 && count <= 100) {
                        log.info("추출된 개수: {} (패턴: {})", count, patternStr);
                        return count;
                    } else {
                        log.warn("개수가 범위를 벗어남: {} (패턴: {})", count, patternStr);
                        return 5; // 기본값
                    }
                }
            } catch (NumberFormatException e) {
                log.error("숫자 변환 오류 (패턴: {}): {}", patternStr, e.getMessage());
                continue; // 다음 패턴 시도
            } catch (Exception e) {
                log.error("개수 추출 중 오류 발생 (패턴: {}): {}", patternStr, e.getMessage());
                continue; // 다음 패턴 시도
            }
        }
        
        log.info("개수 추출 실패 - 기본값 사용: 20");
        return 5; // 기본값
    }
    
    /**
     * 검색어에서 정렬 방식 추출
     */
    private String extractSortOrder(String query) {
        if (query.contains("최저가") || query.contains("낮은 가격") || query.contains("저렴한") || 
            query.contains("싼") || query.contains("최소")) {
            return "ASC";
        } else if (query.contains("최고가") || query.contains("높은 가격") || query.contains("비싼") || 
                   query.contains("최대") || query.contains("고가")) {
            return "DESC";
        }
        return "ASC"; // 기본값
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
        private final String message;
        
        public DynamicQueryResult(String sql, Map<String, Object> parameters, String intentType) {
            this.sql = sql;
            this.parameters = parameters;
            this.intentType = intentType;
            this.message = null;
        }
        
        public DynamicQueryResult(String sql, Map<String, Object> parameters, String intentType, String message) {
            this.sql = sql;
            this.parameters = parameters;
            this.intentType = intentType;
            this.message = message;
        }
        
        public String getSql() { return sql; }
        public Map<String, Object> getParameters() { return parameters; }
        public String getIntentType() { return intentType; }
        public String getMessage() { return message; }
    }
}
