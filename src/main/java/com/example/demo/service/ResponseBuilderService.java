package com.example.demo.service;

import com.example.demo.dto.AnalysisTrace;
import com.example.demo.dto.ShoppingMessageResponse;
import com.example.navershopping.entity.NaverShoppingItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 응답 생성 서비스
 * 
 * 상품 검색 결과를 채팅 UI용 응답 형태로 변환하는 로직을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResponseBuilderService {

    private final ConfidenceCalculatorService confidenceCalculatorService;

    /**
     * 검색 응답 생성
     * 
     * 검색된 상품들을 채팅 UI에서 표시할 수 있는 응답 형태로 변환합니다.
     * 
     * @param query 검색 키워드
     * @param products 검색된 상품 목록
     * @return 채팅 UI용 응답 객체
     */
    public ShoppingMessageResponse createSearchResponse(String query, List<NaverShoppingItem> products) {
        return createSearchResponse(query, products, null);
    }

    /**
     * 검색 응답 생성 (정렬 정보 포함)
     * 
     * 검색된 상품들을 채팅 UI용 응답 형태로 변환합니다.
     * 
     * @param query 검색 키워드
     * @param products 검색된 상품 목록
     * @param sortOrder 정렬 순서 ("asc": 오름차순, "desc": 내림차순, null: 기본)
     * @return 채팅 UI용 응답 객체
     */
    public ShoppingMessageResponse createSearchResponse(String query, List<NaverShoppingItem> products, String sortOrder) {
        // 사용자에게 보여줄 메시지 생성
        String responseMessage = String.format("'%s' 검색 결과 %d개의 상품을 찾았습니다.", query, products.size());
        
        // 정렬 정보가 있으면 메시지에 추가
        if (sortOrder != null) {
            String sortMessage = "desc".equalsIgnoreCase(sortOrder) ? "높은 가격순" : "낮은 가격순";
            responseMessage += " (" + sortMessage + "으로 정렬)";
        }
        
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
                .sortOrder(sortOrder) // 정렬 순서
                .sortType(sortOrder != null ? "price" : null) // 정렬 타입
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
    public ShoppingMessageResponse createRecommendationResponse(String type, List<NaverShoppingItem> products) {
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
    public ShoppingMessageResponse createErrorResponse(String query) {
        return ShoppingMessageResponse.builder()
                .response("죄송합니다. '" + query + "' 검색 중 오류가 발생했습니다.") // 에러 메시지
                .messageType("text") // 일반 텍스트 메시지
                .products(new ArrayList<>()) // 빈 상품 목록
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) // 현재 시간
                .build();
    }

    /**
     * 의도분석 정보를 포함한 검색 응답 생성
     */
    public ShoppingMessageResponse createSearchResponse(String query, List<NaverShoppingItem> products, 
                                                       String sortOrder, String responseMessage, 
                                                       String intentName, float intentScore) {
        log.info("의도분석 기반 검색 응답 생성 - 의도: {}, 상품 수: {}", intentName, products.size());
        
        // 상품 정보를 ProductCard 형태로 변환
        List<ShoppingMessageResponse.ProductCard> productCards = products.stream()
                .map(ShoppingMessageResponse::fromNaverShoppingItem)
                .collect(Collectors.toList());
        
        // 분석 정보 생성 (기존 방식 사용)
        ShoppingMessageResponse.ShoppingAnalysisInfo analysisInfo = ShoppingMessageResponse.ShoppingAnalysisInfo.builder()
                .intentType(intentName)
                .confidence(String.valueOf(intentScore))
                .originalQuery(query)
                .totalResults(products.size())
                .build();
        
        return ShoppingMessageResponse.builder()
                .response(responseMessage)
                .products(productCards)
                .analysisInfo(analysisInfo)
                .build();
    }

    /**
     * 분석 추적 정보를 포함한 검색 응답 생성
     */
    public ShoppingMessageResponse createSearchResponseWithTrace(String query, List<NaverShoppingItem> products, 
                                                                String sortOrder, String responseMessage, 
                                                                String intentName, float intentScore, 
                                                                String engine, AnalysisTrace trace) {
        log.info("추적 정보를 포함한 검색 응답 생성 - 의도: {}, 엔진: {}, 상품 수: {}", intentName, engine, products.size());
        
        // 상품 정보를 ProductCard 형태로 변환
        List<ShoppingMessageResponse.ProductCard> productCards = products.stream()
                .map(ShoppingMessageResponse::fromNaverShoppingItem)
                .collect(Collectors.toList());
        
        // 하이브리드 신뢰도 분석 정보 추출
        String aiServerConfidence = trace.getRagConfidence() != null ? 
            String.format("%.2f", trace.getRagConfidence()) : "N/A";
        String backendConfidence = String.format("%.2f", intentScore);
        String confidenceSource = determineConfidenceSource(trace, intentScore);
        String analysisMethod = determineAnalysisMethod(trace, intentScore);
        
        // 분석 정보 생성 (하이브리드 신뢰도 정보 포함)
        ShoppingMessageResponse.ShoppingAnalysisInfo analysisInfo = ShoppingMessageResponse.ShoppingAnalysisInfo.builder()
                .intentType(intentName)
                .confidence(String.format("%.2f", intentScore)) // 최종 신뢰도
                .aiServerConfidence(aiServerConfidence) // AI 서버 신뢰도
                .backendConfidence(backendConfidence) // 백엔드 신뢰도
                .confidenceSource(confidenceSource) // 신뢰도 소스
                .engine(engine) // 사용된 엔진
                .analysisMethod(analysisMethod) // 분석 방법
                .originalQuery(query)
                .totalResults(products.size())
                .build();
        
        return ShoppingMessageResponse.builder()
                .response(responseMessage)
                .products(productCards)
                .analysisInfo(analysisInfo)
                .build();
    }

    /**
     * 신뢰도 소스 결정
     */
    private String determineConfidenceSource(AnalysisTrace trace, float finalConfidence) {
        if (trace.getRagConfidence() == null) {
            return "backend"; // AI 서버 신뢰도가 없으면 백엔드
        }
        
        float aiConfidence = trace.getRagConfidence().floatValue();
        float backendConfidence = finalConfidence;
        float difference = Math.abs(aiConfidence - backendConfidence);
        
        if (difference > 0.3f) {
            return "hybrid"; // 차이가 크면 하이브리드
        } else {
            return "ai_server"; // 차이가 적으면 AI 서버
        }
    }
    
    /**
     * 분석 방법 결정
     */
    private String determineAnalysisMethod(AnalysisTrace trace, float finalConfidence) {
        if (trace.getRagConfidence() == null) {
            return "keyword_matching"; // AI 서버 신뢰도가 없으면 키워드 매칭
        }
        
        String engine = trace.getFinalEngine();
        if ("rag".equals(engine)) {
            return "deep_learning"; // RAG 모델 사용
        } else if ("hybrid".equals(determineConfidenceSource(trace, finalConfidence))) {
            return "hybrid"; // 하이브리드 방식
        } else {
            return "keyword_matching"; // 기본적으로 키워드 매칭
        }
    }
}
