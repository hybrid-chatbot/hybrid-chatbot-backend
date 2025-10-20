package com.example.demo.service;

import com.example.demo.dto.AiServerResponse;
import com.example.demo.dto.AnalysisTrace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG 분석 서비스
 * 
 * RAG 모델을 통한 상품검색 의도분석을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagAnalysisService {

    private final AiServerService aiServerService;
    private final ConfidenceCalculatorService confidenceCalculatorService;

    /**
     * RAG 모델을 통한 상품검색 의도분석
     * 
     * @param query 사용자 쿼리
     * @param traceBuilder 분석 추적 빌더
     * @return RAG 분석 결과
     */
    public AiServerResponse performRagAnalysis(String query, AnalysisTrace.Builder traceBuilder) {
        log.info("RAG 모델 상품검색 의도분석 시작: {}", query);
        
        // 상품검색용 의도 목록 (네이버 API에서 실제 제공하는 데이터만 사용)
        List<String> shoppingIntents = List.of(
            "product_search",           // 일반 상품 검색
            "product_recommendation",   // 상품 추천
            "product_filter",          // 상품 필터링
            "product_compare",         // 상품 비교
            "brand_search",            // 브랜드별 검색
            "category_search",         // 카테고리별 검색
            "price_range_search"       // 가격대별 검색
        );
        
        try {
            AiServerResponse ragResponse = aiServerService.classifyIntent(query, shoppingIntents);
            if (ragResponse != null) {
                // 신뢰도 검증 및 보정
                float aiServerConfidence = ragResponse.getConfidence() != null ? 
                    ragResponse.getConfidence() : 0.0f;
                float backendConfidence = confidenceCalculatorService.calculateIntentConfidence(query, ragResponse.getFinal_intent());
                
                // AI 서버 신뢰도와 백엔드 신뢰도 비교
                float confidenceDifference = Math.abs(aiServerConfidence - backendConfidence);
                
                if (ragResponse.getConfidence() == null) {
                    // AI 서버에서 신뢰도가 없으면 백엔드 신뢰도 사용
                    ragResponse.setConfidence(backendConfidence);
                    log.info("AI 서버 신뢰도 없음 - 백엔드 신뢰도 사용: {}", backendConfidence);
                } else if (confidenceDifference > 0.3f) {
                    // 신뢰도 차이가 크면 백엔드 신뢰도로 보정
                    float adjustedConfidence = (aiServerConfidence + backendConfidence) / 2.0f;
                    ragResponse.setConfidence(adjustedConfidence);
                    log.warn("신뢰도 차이 큼 (AI: {}, 백엔드: {}) - 조정된 신뢰도 사용: {}", 
                            aiServerConfidence, backendConfidence, adjustedConfidence);
                } else {
                    // 신뢰도 차이가 적으면 AI 서버 신뢰도 사용
                    log.info("AI 서버 신뢰도 사용: {} (백엔드: {}, 차이: {})", 
                            aiServerConfidence, backendConfidence, confidenceDifference);
                }
                
                traceBuilder.ragFinalIntent(ragResponse.getFinal_intent());
                traceBuilder.ragConfidence((double) ragResponse.getConfidence());
                traceBuilder.finalEngine(ragResponse.getEngine());
                log.info("RAG 상품검색 의도분석 완료 - 의도: {}, 신뢰도: {}, 엔진: {}", 
                        ragResponse.getFinal_intent(), ragResponse.getConfidence(), ragResponse.getEngine());
            } else {
                log.warn("RAG 분석 실패 - 기본 검색 의도 사용");
                traceBuilder.ragFinalIntent("product_search");
                traceBuilder.ragConfidence(0.5);
                traceBuilder.finalEngine("fallback");
            }
            return ragResponse;
        } catch (Exception e) {
            log.error("RAG 상품검색 의도분석 중 오류 발생: {}", query, e);
            traceBuilder.ragFinalIntent("product_search");
            traceBuilder.ragConfidence(0.3);
            traceBuilder.finalEngine("error_fallback");
            return null;
        }
    }
}
