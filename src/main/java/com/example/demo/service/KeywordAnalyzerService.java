package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 키워드 분석 서비스
 * 
 * 사용자 쿼리에서 키워드를 분석하고 가격 정렬 요청을 감지하는 로직을 담당합니다.
 */
@Slf4j
@Service
public class KeywordAnalyzerService {

    /**
     * 가격순 정렬 요청 감지
     * 
     * 사용자 메시지에서 가격 정렬 관련 키워드를 감지하여 정렬 순서를 반환합니다.
     * 
     * @param message 사용자 메시지
     * @return 정렬 순서 ("asc": 오름차순, "desc": 내림차순, null: 정렬 요청 없음)
     */
    public String detectPriceSortRequest(String message) {
        if (message == null) return null;
        
        String lowerMessage = message.toLowerCase();
        
        // 오름차순 (낮은 가격순) 키워드 - 직접적인 키워드 먼저 체크
        if (lowerMessage.contains("최저가순") || lowerMessage.contains("최저가 순") ||
            lowerMessage.contains("낮은가격순") || lowerMessage.contains("낮은 가격순") ||
            lowerMessage.contains("저렴한가격순") || lowerMessage.contains("저렴한 가격순") ||
            lowerMessage.contains("싼가격순") || lowerMessage.contains("싼 가격순") ||
            lowerMessage.contains("오름차순")) {
            return "asc";
        }
        
        // 내림차순 (높은 가격순) 키워드 - 직접적인 키워드 먼저 체크
        if (lowerMessage.contains("최고가순") || lowerMessage.contains("최고가 순") ||
            lowerMessage.contains("높은가격순") || lowerMessage.contains("높은 가격순") ||
            lowerMessage.contains("비싼가격순") || lowerMessage.contains("비싼 가격순") ||
            lowerMessage.contains("내림차순")) {
            return "desc";
        }
        
        // 복합 키워드 체크
        if (lowerMessage.contains("가격순") || lowerMessage.contains("가격 순")) {
            if (lowerMessage.contains("낮은") || lowerMessage.contains("저렴한") || 
                lowerMessage.contains("싼") || lowerMessage.contains("최저가") ||
                lowerMessage.contains("asc")) {
                return "asc";
            }
            if (lowerMessage.contains("높은") || lowerMessage.contains("비싼") || 
                lowerMessage.contains("최고가") || lowerMessage.contains("desc")) {
                return "desc";
            }
        }
        
        // 직접적인 정렬 요청
        if (lowerMessage.contains("정렬") || lowerMessage.contains("sort")) {
            if (lowerMessage.contains("낮은") || lowerMessage.contains("저렴한") || 
                lowerMessage.contains("싼") || lowerMessage.contains("최저가") ||
                lowerMessage.contains("asc")) {
                return "asc";
            }
            if (lowerMessage.contains("높은") || lowerMessage.contains("비싼") || 
                lowerMessage.contains("최고가") || lowerMessage.contains("desc")) {
                return "desc";
            }
        }
        
        // 추가 패턴들
        String[] ascPatterns = {
            "가격낮은순", "가격 낮은순", "가격낮은순으로", "가격 낮은순으로",
            "가격낮은", "가격 낮은", "가격낮게", "가격 낮게",
            "저렴한순", "저렴한 순", "저렴한순으로", "저렴한 순으로",
            "싼순", "싼 순", "싼순으로", "싼 순으로",
            "낮은순", "낮은 순", "낮은순으로", "낮은 순으로"
        };
        
        String[] descPatterns = {
            "가격높은순", "가격 높은순", "가격높은순으로", "가격 높은순으로",
            "가격높은", "가격 높은", "가격높게", "가격 높게",
            "비싼순", "비싼 순", "비싼순으로", "비싼 순으로",
            "높은순", "높은 순", "높은순으로", "높은 순으로"
        };
        
        // 오름차순 패턴 체크
        for (String pattern : ascPatterns) {
            if (lowerMessage.contains(pattern)) {
                return "asc";
            }
        }
        
        // 내림차순 패턴 체크
        for (String pattern : descPatterns) {
            if (lowerMessage.contains(pattern)) {
                return "desc";
            }
        }
        
        // 보여줘 + 가격 관련 키워드
        if (lowerMessage.contains("보여줘") || lowerMessage.contains("보여주세요") || 
            lowerMessage.contains("보여") || lowerMessage.contains("보기")) {
            if (lowerMessage.contains("낮은") || lowerMessage.contains("저렴한") || 
                lowerMessage.contains("싼") || lowerMessage.contains("최저가") ||
                lowerMessage.contains("낮게")) {
                return "asc";
            }
            if (lowerMessage.contains("높은") || lowerMessage.contains("비싼") || 
                lowerMessage.contains("최고가") || lowerMessage.contains("높게")) {
                return "desc";
            }
        }
        
        return null; // 정렬 요청 없음
    }

    /**
     * 정렬 키워드 제거
     * 
     * 사용자 메시지에서 정렬 관련 키워드를 제거하여 순수한 검색어만 추출합니다.
     * 
     * @param message 원본 메시지
     * @return 정렬 키워드가 제거된 검색어
     */
    public String removeSortKeywords(String message) {
        if (message == null) return null;
        
        String result = message;
        
        // 정렬 관련 키워드들을 제거
        String[] sortKeywords = {
            "가격순으로", "가격 순으로", "가격순", "가격 순",
            "낮은가격순", "낮은 가격순", "저렴한가격순", "저렴한 가격순", 
            "싼가격순", "싼 가격순", "최저가순", "최저가 순", "오름차순",
            "높은가격순", "높은 가격순", "비싼가격순", "비싼 가격순", 
            "최고가순", "최고가 순", "내림차순",
            "가격낮은순", "가격 낮은순", "가격낮은순으로", "가격 낮은순으로",
            "가격낮은", "가격 낮은", "가격낮게", "가격 낮게",
            "가격높은순", "가격 높은순", "가격높은순으로", "가격 높은순으로",
            "가격높은", "가격 높은", "가격높게", "가격 높게",
            "저렴한순", "저렴한 순", "저렴한순으로", "저렴한 순으로",
            "싼순", "싼 순", "싼순으로", "싼 순으로",
            "비싼순", "비싼 순", "비싼순으로", "비싼 순으로",
            "낮은순", "낮은 순", "낮은순으로", "낮은 순으로",
            "높은순", "높은 순", "높은순으로", "높은 순으로",
            "정렬해줘", "정렬해", "정렬", "sort"
        };
        
        for (String keyword : sortKeywords) {
            result = result.replaceAll("(?i)" + keyword, "").trim();
        }
        
        return result.isEmpty() ? message : result;
    }

    /**
     * 검색 키워드 목록 반환
     */
    public String[] getSearchKeywords() {
        return new String[]{"검색", "찾아", "보여", "검색해", "찾아줘", "보여줘", "찾기", "검색해줘"};
    }
    
    /**
     * 추천 키워드 목록 반환
     */
    public String[] getRecommendationKeywords() {
        return new String[]{"추천", "추천해", "추천해줘", "어떤", "좋은", "추천해주세요", "어떤게", "좋을까"};
    }
    
    /**
     * 필터 키워드 목록 반환
     */
    public String[] getFilterKeywords() {
        return new String[]{"필터", "필터링", "정렬", "순으로", "이하", "이상", "정리", "분류", "구분", "나누어"};
    }
    
    /**
     * 비교 키워드 목록 반환
     */
    public String[] getCompareKeywords() {
        return new String[]{"비교", "비교해", "비교해줘", "vs", "대비", "차이", "비교해주세요", "대조", "대조해"};
    }
    
    /**
     * 브랜드 키워드 목록 반환
     */
    public String[] getBrandKeywords() {
        return new String[]{"브랜드", "나이키", "아디다스", "푸마", "뉴발란스", "컨버스", "리복", "아식스", "미즈노", "언더아머"};
    }
    
    /**
     * 카테고리 키워드 목록 반환
     */
    public String[] getCategoryKeywords() {
        return new String[]{"카테고리", "종류", "분류", "운동화", "신발", "스니커즈", "구두", "부츠", "샌들", "슬리퍼"};
    }
    
    /**
     * 가격 키워드 목록 반환
     */
    public String[] getPriceKeywords() {
        return new String[]{"가격", "만원", "원", "비싼", "저렴", "싼", "돈", "비용", "금액", "얼마"};
    }

    /**
     * 검색 키워드 포함 여부 확인
     */
    public boolean containsSearchKeywords(String query) {
        return query.contains("검색") || query.contains("찾아") || query.contains("보여") || 
               query.contains("검색해") || query.contains("찾아줘");
    }
    
    /**
     * 추천 키워드 포함 여부 확인
     */
    public boolean containsRecommendationKeywords(String query) {
        return query.contains("추천") || query.contains("추천해") || query.contains("추천해줘") ||
               query.contains("어떤") || query.contains("좋은") || query.contains("추천해주세요");
    }
    
    /**
     * 필터 키워드 포함 여부 확인
     */
    public boolean containsFilterKeywords(String query) {
        return query.contains("필터") || query.contains("필터링") || query.contains("정렬") ||
               query.contains("순으로") || query.contains("이하") || query.contains("이상");
    }
    
    /**
     * 비교 키워드 포함 여부 확인
     */
    public boolean containsCompareKeywords(String query) {
        return query.contains("비교") || query.contains("비교해") || query.contains("비교해줘") ||
               query.contains("vs") || query.contains("대비") || query.contains("차이");
    }
    
    /**
     * 브랜드 키워드 포함 여부 확인
     */
    public boolean containsBrandKeywords(String query) {
        return query.contains("브랜드") || query.contains("나이키") || query.contains("아디다스") ||
               query.contains("푸마") || query.contains("뉴발란스") || query.contains("컨버스");
    }
    
    /**
     * 카테고리 키워드 포함 여부 확인
     */
    public boolean containsCategoryKeywords(String query) {
        return query.contains("카테고리") || query.contains("종류") || query.contains("분류") ||
               query.contains("운동화") || query.contains("신발") || query.contains("스니커즈");
    }
    
    /**
     * 가격 키워드 포함 여부 확인
     */
    public boolean containsPriceKeywords(String query) {
        return query.contains("가격") || query.contains("만원") || query.contains("원") ||
               query.contains("비싼") || query.contains("저렴") || query.contains("싼");
    }
}
