package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 신뢰도 계산 서비스
 * 
 * 상품검색 의도분석의 신뢰도를 계산하는 로직을 담당합니다.
 * 키워드 매칭, 길이, 복잡도 등을 종합적으로 고려하여 신뢰도를 산출합니다.
 */
@Slf4j
@Service
public class ConfidenceCalculatorService {

    /**
     * 백엔드에서 의도 신뢰도 계산
     * 
     * @param query 사용자 쿼리
     * @param intentName 의도명
     * @return 계산된 신뢰도 (0.0 ~ 1.0)
     */
    public float calculateIntentConfidence(String query, String intentName) {
        String lowerQuery = query.toLowerCase();
        String lowerIntent = intentName.toLowerCase();
        
        // 다중 신뢰도 계산 방법
        float keywordConfidence = calculateKeywordConfidence(lowerQuery, lowerIntent);
        float lengthConfidence = calculateLengthConfidence(query);
        float complexityConfidence = calculateComplexityConfidence(query);
        
        // 가중 평균으로 최종 신뢰도 계산
        float finalConfidence = (keywordConfidence * 0.6f) + 
                               (lengthConfidence * 0.2f) + 
                               (complexityConfidence * 0.2f);
        
        log.debug("신뢰도 계산 - 키워드: {}, 길이: {}, 복잡도: {}, 최종: {}", 
                keywordConfidence, lengthConfidence, complexityConfidence, finalConfidence);
        
        return Math.max(0.1f, Math.min(1.0f, finalConfidence));
    }
    
    /**
     * 키워드 기반 신뢰도 계산
     */
    public float calculateKeywordConfidence(String query, String intent) {
        float confidence = 0.3f; // 기본 키워드 신뢰도
        
        // 의도별 키워드 매칭 점수 (키워드 수에 비례)
        switch (intent) {
            case "product_search":
                confidence += calculateMatchedKeywordsScore(query, getSearchKeywords());
                break;
            case "product_recommendation":
                confidence += calculateMatchedKeywordsScore(query, getRecommendationKeywords());
                break;
            case "product_filter":
                confidence += calculateMatchedKeywordsScore(query, getFilterKeywords());
                break;
            case "product_compare":
                confidence += calculateMatchedKeywordsScore(query, getCompareKeywords());
                break;
            case "brand_search":
                confidence += calculateMatchedKeywordsScore(query, getBrandKeywords());
                break;
            case "category_search":
                confidence += calculateMatchedKeywordsScore(query, getCategoryKeywords());
                break;
            case "price_range_search":
                confidence += calculateMatchedKeywordsScore(query, getPriceKeywords());
                break;
        }
        
        return Math.min(1.0f, confidence);
    }
    
    /**
     * 매칭된 키워드 수에 따른 신뢰도 점수 계산 (개선된 버전)
     * 
     * @param query 사용자 쿼리
     * @param keywords 키워드 목록
     * @return 신뢰도 점수 (0.0 ~ 0.6)
     */
    public float calculateMatchedKeywordsScore(String query, String[] keywords) {
        int matchedCount = 0;
        float weightedScore = 0.0f;
        
        // 키워드별 가중치 정의 (중요도에 따라)
        Map<String, Float> keywordWeights = getKeywordWeights(keywords);
        
        for (String keyword : keywords) {
            if (query.contains(keyword)) {
                matchedCount++;
                
                // 키워드별 가중치 적용
                float weight = keywordWeights.getOrDefault(keyword, 0.1f);
                weightedScore += weight;
            }
        }
        
        // 중복 키워드 패널티 (같은 의미의 키워드가 여러 번 매칭되는 경우)
        float duplicatePenalty = calculateDuplicatePenalty(query, keywords);
        weightedScore -= duplicatePenalty;
        
        // 키워드 밀도 보너스 (짧은 쿼리에서 많은 키워드가 매칭된 경우)
        float densityBonus = calculateDensityBonus(query, matchedCount);
        weightedScore += densityBonus;
        
        // 최종 점수 계산 (0.0 ~ 0.6)
        return Math.max(0.0f, Math.min(0.6f, weightedScore));
    }
    
    /**
     * 키워드별 가중치 정의
     */
    private Map<String, Float> getKeywordWeights(String[] keywords) {
        Map<String, Float> weights = new HashMap<>();
        
        // 핵심 키워드 (높은 가중치)
        for (String keyword : keywords) {
            if (isCoreKeyword(keyword)) {
                weights.put(keyword, 0.15f); // 핵심 키워드
            } else if (isSecondaryKeyword(keyword)) {
                weights.put(keyword, 0.12f); // 보조 키워드
            } else {
                weights.put(keyword, 0.08f); // 일반 키워드
            }
        }
        
        return weights;
    }
    
    /**
     * 핵심 키워드 판별
     */
    private boolean isCoreKeyword(String keyword) {
        String[] coreKeywords = {
            "추천", "추천해", "추천해줘", "비교", "비교해", "검색", "검색해",
            "정렬", "순으로", "필터", "필터링", "브랜드", "카테고리"
        };
        return Arrays.asList(coreKeywords).contains(keyword);
    }
    
    /**
     * 보조 키워드 판별
     */
    private boolean isSecondaryKeyword(String keyword) {
        String[] secondaryKeywords = {
            "어떤", "좋은", "나이키", "아디다스", "운동화", "신발",
            "가격", "만원", "이하", "이상", "낮은", "높은"
        };
        return Arrays.asList(secondaryKeywords).contains(keyword);
    }
    
    /**
     * 중복 키워드 패널티 계산
     */
    private float calculateDuplicatePenalty(String query, String[] keywords) {
        // 같은 의미의 키워드 그룹 정의
        String[][] duplicateGroups = {
            {"추천", "추천해", "추천해줘", "추천해주세요"},
            {"비교", "비교해", "비교해줘", "비교해주세요"},
            {"검색", "검색해", "검색해줘", "검색해주세요"},
            {"나이키", "나이키 브랜드", "나이키 제품"},
            {"아디다스", "아디다스 브랜드", "아디다스 제품"}
        };
        
        float penalty = 0.0f;
        for (String[] group : duplicateGroups) {
            int groupMatches = 0;
            for (String keyword : group) {
                if (query.contains(keyword)) {
                    groupMatches++;
                }
            }
            // 같은 그룹에서 2개 이상 매칭되면 패널티
            if (groupMatches > 1) {
                penalty += (groupMatches - 1) * 0.05f;
            }
        }
        
        return penalty;
    }
    
    /**
     * 키워드 밀도 보너스 계산
     */
    private float calculateDensityBonus(String query, int matchedCount) {
        int queryLength = query.length();
        float density = (float) matchedCount / queryLength;
        
        // 쿼리 길이 대비 키워드 밀도가 높으면 보너스
        if (density > 0.1f) { // 10% 이상
            return Math.min(0.1f, density * 0.5f);
        }
        
        return 0.0f;
    }
    
    /**
     * 길이 기반 신뢰도 계산
     */
    public float calculateLengthConfidence(String query) {
        int length = query.length();
        if (length < 3) return 0.2f;      // 너무 짧음
        if (length < 8) return 0.4f;      // 짧음
        if (length < 20) return 0.7f;     // 적당함
        if (length < 50) return 0.9f;     // 좋음
        return 0.8f;                      // 너무 김 (오히려 감소)
    }
    
    /**
     * 복잡도 기반 신뢰도 계산
     */
    public float calculateComplexityConfidence(String query) {
        // 복잡도 지표들
        int wordCount = query.split("\\s+").length;
        boolean hasQuestion = query.contains("?") || query.contains("어떤") || query.contains("무엇");
        boolean hasNumbers = query.matches(".*\\d+.*");
        boolean hasSpecialWords = query.matches(".*[가-힣]+.*");
        
        float complexity = 0.5f;
        
        // 단어 수에 따른 복잡도
        if (wordCount >= 3) complexity += 0.2f;
        if (wordCount >= 5) complexity += 0.1f;
        
        // 질문 형태는 더 구체적
        if (hasQuestion) complexity += 0.2f;
        
        // 숫자가 있으면 더 구체적
        if (hasNumbers) complexity += 0.1f;
        
        // 한국어가 있으면 더 구체적
        if (hasSpecialWords) complexity += 0.1f;
        
        return Math.min(1.0f, complexity);
    }
    
    // 키워드 배열 반환 메서드들
    private String[] getSearchKeywords() {
        return new String[]{"검색", "찾아", "보여", "검색해", "찾아줘", "보여줘", "찾기", "검색해줘"};
    }
    
    private String[] getRecommendationKeywords() {
        return new String[]{"추천", "추천해", "추천해줘", "어떤", "좋은", "추천해주세요", "어떤게", "좋을까"};
    }
    
    private String[] getFilterKeywords() {
        return new String[]{"필터", "필터링", "정렬", "순으로", "이하", "이상", "정리", "분류", "구분", "나누어"};
    }
    
    private String[] getCompareKeywords() {
        return new String[]{"비교", "비교해", "비교해줘", "vs", "대비", "차이", "비교해주세요", "대조", "대조해"};
    }
    
    private String[] getBrandKeywords() {
        return new String[]{"브랜드", "나이키", "아디다스", "푸마", "뉴발란스", "컨버스", "리복", "아식스", "미즈노", "언더아머"};
    }
    
    private String[] getCategoryKeywords() {
        return new String[]{"카테고리", "종류", "분류", "운동화", "신발", "스니커즈", "구두", "부츠", "샌들", "슬리퍼"};
    }
    
    private String[] getPriceKeywords() {
        return new String[]{"가격", "만원", "원", "비싼", "저렴", "싼", "돈", "비용", "금액", "얼마"};
    }
}
