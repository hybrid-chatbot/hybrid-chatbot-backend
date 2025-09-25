// src/main/java/com/example/demo/service/ChatOrchestratorService.java

package com.example.demo.service;

import com.example.demo.dto.AiServerResponse;
import com.example.demo.dto.MessageRequest;
import com.example.demo.model.ChatMessage;
import com.example.demo.utils.CosineSimilarityCalculator;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatOrchestratorService {

    // 임계치 상수
    private static final double DIALOGFLOW_SCORE_THRESHOLD = 0.6;
    private static final double SIMILARITY_SCORE_THRESHOLD = 0.0; // 의미 유사도 기준 점수

    // 서비스 의존성 주입
    private final DialogflowService dialogflowService;
    private final AiServerService aiServerService;
    private final IntentResponseService intentResponseService;
    private final ChatService chatService;
    private final EmbeddingService embeddingService; // 
    private final IntentRepresentativeService intentRepresentativeService; //  문장 관리자 전문가

    public void processMessage(MessageRequest request) {
        try {
            DetectIntentResponse dialogflowResponse = dialogflowService.detectIntent(
                    request.getSessionId(),
                    request.getMessage(),
                    request.getLanguageCode()
            );

            String originalIntentName = dialogflowResponse.getQueryResult().getIntent().getDisplayName();
            float intentScore = dialogflowResponse.getQueryResult().getIntentDetectionConfidence();
            String dialogflowReply = dialogflowResponse.getQueryResult().getFulfillmentText();

            // --- ✨ 여기가 바로 새로 추가된 '교차 검증 안전망' 로직입니다! ---
            // 1. Dialogflow 점수가 높은가?
            if (intentScore >= DIALOGFLOW_SCORE_THRESHOLD) {
                // 2. 점수가 높더라도, 정말 믿을 수 있는지 2차 검증을 수행한다.
                if (isSemanticallySimilar(request.getMessage(), originalIntentName)) {
                    // 3. 2차 검증까지 통과하면, Dialogflow의 답변을 신뢰한다.
                    log.info("Dialogflow 점수가 높고(score: {}), 의미 유사도 검증 통과. Dialogflow 응답 사용.", intentScore);
                    handleHighConfidenceIntent(request, originalIntentName, intentScore, dialogflowReply);
                } else {
                    // 4. Dialogflow 점수는 높았지만, 의미가 다르다고 판단되면 RAG 전문가에게 넘긴다.
                    log.warn("Dialogflow 점수는 높았지만(score: {}), 의미 유사도가 낮아 RAG를 호출합니다.", intentScore);
                    handleLowConfidenceIntent(request, originalIntentName, intentScore);
                }
            } else {
                // 5. Dialogflow 점수 자체가 낮으면, 원래대로 RAG 전문가에게 넘긴다.
                handleLowConfidenceIntent(request, originalIntentName, intentScore);
            }

        } catch (Exception e) {
            log.error("메시지 처리 중 오류가 발생했습니다: {}", request, e);
        }
    }

    /**
     * 사용자의 질문과 Dialogflow가 예측한 의도의 대표 문장 간의 의미 유사도를 검증합니다.
     * @return 의미적으로 유사하면 true, 그렇지 않으면 false
     */
    private boolean isSemanticallySimilar(String userQuestion, String intentName) {
        // 1. '문장 관리자'에게 의도의 대표 문장을 물어봅니다.
        Optional<String> representativeTextOpt = intentRepresentativeService.getRepresentativeText(intentName);
        if (representativeTextOpt.isEmpty()) {
            return true; // 대표 문장이 없으면 검증을 통과시킵니다.
        }
        String representativeText = representativeTextOpt.get();

        // 2. '번역기 전문가'에게 두 문장의 번역(임베딩)을 요청합니다.
        List<List<Double>> embeddings = embeddingService.getEmbeddings(List.of(userQuestion, representativeText));
        if (embeddings == null || embeddings.size() < 2) {
            return false; // 임베딩 실패 시, 안전을 위해 검증 실패로 처리합니다.
        }

        // 3. '계산기'를 사용해 두 벡터의 유사도를 계산합니다.
        double similarity = CosineSimilarityCalculator.calculate(embeddings.get(0), embeddings.get(1));
        log.info("의미 유사도 검증 결과: '{}' vs '{}' = {}", userQuestion, representativeText, similarity);

        // 4. 계산된 점수가 우리가 정한 기준보다 높은지 확인하여 결과를 반환합니다.
        return similarity >= SIMILARITY_SCORE_THRESHOLD;
    }

    // handleLowConfidenceIntent(...)와 handleHighConfidenceIntent(...) 메서드는 이전과 동일합니다.
    // (이하 생략)
    private void handleLowConfidenceIntent(MessageRequest request, String originalIntentName, float originalIntentScore) {
        log.info("Dialogflow 점수가 낮거나(score: {}), 의미 유사도 검증 실패. Python AI 서버 호출 시작.", originalIntentScore);
        List<String> allIntents = List.of(
            // 그룹 A: RAG가 해결할 복잡한 의도
            "환불절차문의_VIP혜택",
            "환불금액문의_쿠폰사용",
            "환불혜택문의_등급변경",
            "콜라보상품_중복할인_문의",
            "이벤트_중복할인_문의",
            "콜라보상품_환불_문의",
            // 그룹 B: Dialogflow가 기본적으로 학습할 단순 의도
            "배송_조회",
            "일반_환불_문의",
            "일반_교환_문의",
            "주문_수정",
            "주문_취소"
        );
        AiServerResponse aiResponse = aiServerService.classifyIntent(request.getMessage(), allIntents);
        if (aiResponse != null) {
            String finalIntent = aiResponse.getFinal_intent();
            String fixedResponse = intentResponseService.getResponseForIntent(finalIntent);
            ChatMessage.AnalysisInfo analysisInfo = ChatMessage.AnalysisInfo.builder()
                    .engine(aiResponse.getEngine()).intentName(finalIntent)
                    .originalIntentName(originalIntentName).originalIntentScore(originalIntentScore)
                    .build();
            chatService.saveMessage(
                    request.getSessionId(), request.getUserId(), "bot",
                    fixedResponse, request.getLanguageCode(), analysisInfo
            );
        } else {
            log.warn("Python AI 서버 호출에 실패했습니다. Dialogflow의 응답으로 대체합니다.");
            handleHighConfidenceIntent(request, originalIntentName, originalIntentScore, "죄송해요, 지금은 AI 서버에 문제가 있어 답변을 드릴 수 없어요.");
        }
    }
    private void handleHighConfidenceIntent(MessageRequest request, String intentName, float intentScore, String reply) {
        ChatMessage.AnalysisInfo analysisInfo = ChatMessage.AnalysisInfo.builder()
                .engine("dialogflow").intentName(intentName)
                .originalIntentName(intentName).originalIntentScore(intentScore)
                .build();
        chatService.saveMessage(
                request.getSessionId(), request.getUserId(), "bot",
                reply, request.getLanguageCode(), analysisInfo
        );
    }
}