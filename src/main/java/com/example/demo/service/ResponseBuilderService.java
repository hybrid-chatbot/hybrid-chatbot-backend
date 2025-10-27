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
        
        // AI 서버 신뢰도만 사용
        String confidence = String.format("%.2f", intentScore);
        
        // 분석 정보 생성 (AI 서버 신뢰도만 사용)
        ShoppingMessageResponse.ShoppingAnalysisInfo analysisInfo = ShoppingMessageResponse.ShoppingAnalysisInfo.builder()
                .intentType(intentName)
                .confidence(confidence) // AI 서버 신뢰도
                .engine(engine) // 사용된 엔진
                .analysisMethod("LLM 기반 동적 SQL 쿼리") // 분석 방법
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
     * 동적 SQL 기반 검색 응답 생성 (추적 정보 포함)
     * 
     * @param query 검색어
     * @param products 검색된 상품 목록
     * @param sortOrder 정렬 순서
     * @param analysisTrace 분석 추적 정보
     * @param intentType 의도 타입
     * @param confidence 신뢰도
     * @return 검색 응답
     */
    public ShoppingMessageResponse createSearchResponseWithTrace(
            String query, 
            List<NaverShoppingItem> products, 
            String sortOrder, 
            AnalysisTrace analysisTrace,
            String intentType,
            float confidence) {
        
        log.info("동적 SQL 기반 검색 응답 생성 - 검색어: {}, 상품수: {}, 의도: {}, 신뢰도: {}", 
                query, products.size(), intentType, confidence);
        
        try {
            // 기본 응답 메시지 생성
            String responseMessage = generateDynamicSearchMessage(query, products.size(), intentType, confidence);
            
            // 상품 데이터 변환
            List<ShoppingMessageResponse.ProductCard> productList = convertToProductList(products);
            
            // 분석 정보 생성
            ShoppingMessageResponse.ShoppingAnalysisInfo analysisInfo = ShoppingMessageResponse.ShoppingAnalysisInfo.builder()
                    .engine("dynamic_sql")
                    .intentType(intentType)
                    .confidence(String.valueOf(confidence))
                    .analysisMethod("LLM 기반 동적 SQL 쿼리")
                    .build();
            
            return ShoppingMessageResponse.builder()
                    .response(responseMessage)
                    .messageType("shopping")
                    .products(productList)
                    .analysisInfo(analysisInfo)
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
                    
        } catch (Exception e) {
            log.error("동적 SQL 기반 검색 응답 생성 중 오류 발생: {}", query, e);
            return createErrorResponse(query);
        }
    }
    
    /**
     * 동적 SQL 검색 메시지 생성
     */
    private String generateDynamicSearchMessage(String query, int productCount, String intentType, float confidence) {
        StringBuilder message = new StringBuilder();
        
        message.append("'").append(query).append("'에 대한 ");
        
        // 의도별 메시지
        switch (intentType.toLowerCase()) {
            case "product_search":
                message.append("검색 결과 ");
                break;
            case "product_recommendation":
                message.append("추천 상품 ");
                break;
            case "product_filter":
                message.append("필터링된 상품 ");
                break;
            case "product_compare":
                message.append("비교 상품 ");
                break;
            case "brand_search":
                message.append("브랜드별 상품 ");
                break;
            case "category_search":
                message.append("카테고리별 상품 ");
                break;
            case "price_range_search":
                message.append("가격대별 상품 ");
                break;
            default:
                message.append("검색 결과 ");
        }
        
        message.append(productCount).append("개를 찾았습니다.");
        
        // 신뢰도 정보 추가
        if (confidence > 0.8f) {
            message.append(" (높은 정확도)");
        } else if (confidence > 0.6f) {
            message.append(" (중간 정확도)");
        } else {
            message.append(" (기본 정확도)");
        }
        
        return message.toString();
    }
    
    /**
     * NaverShoppingItem 리스트를 ProductCard 리스트로 변환
     */
    private List<ShoppingMessageResponse.ProductCard> convertToProductList(List<NaverShoppingItem> products) {
        return products.stream()
                .map(ShoppingMessageResponse::fromNaverShoppingItem)
                .collect(java.util.stream.Collectors.toList());
    }
    
    
}
