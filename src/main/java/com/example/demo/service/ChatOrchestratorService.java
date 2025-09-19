package com.example.demo.service;

import com.example.demo.dto.AiServerResponse;
import com.example.demo.dto.MessageRequest;
import com.example.demo.model.ChatMessage;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatOrchestratorService {

    private static final double INTENT_SCORE_THRESHOLD = 0.8;

    // --- 필요한 모든 전문가(서비스)들을 불러옵니다 ---
    private final DialogflowService dialogflowService;
    private final AiServerService aiServerService;
    private final IntentResponseService intentResponseService; // 답변 전문가
    private final ChatService chatService; // DB 저장 전문가

    /**
     * 사용자의 메시지를 받아 챗봇 응답을 생성하고 저장하는 전체 과정을 지휘합니다.
     * @param request 사용자가 보낸 메시지 요청
     */
    public void processMessage(MessageRequest request) {
        try {
            // 1. Dialogflow를 호출해서 1차 분석을 합니다.
            DetectIntentResponse dialogflowResponse = dialogflowService.detectIntent(
                    request.getSessionId(),
                    request.getMessage(),
                    request.getLanguageCode()
            );

            String originalIntentName = dialogflowResponse.getQueryResult().getIntent().getDisplayName();
            float intentScore = dialogflowResponse.getQueryResult().getIntentDetectionConfidence();
            String dialogflowReply = dialogflowResponse.getQueryResult().getFulfillmentText();

            // 2. 점수를 기준으로 분기 처리를 합니다.
            if (intentScore < INTENT_SCORE_THRESHOLD) {
                // 2-A. 점수가 낮으면 AI 서버에게 다시 물어봅니다.
                handleLowConfidenceIntent(request, originalIntentName, intentScore);
            } else {
                // 2-B. 점수가 높으면 Dialogflow의 답변을 그대로 사용합니다.
                handleHighConfidenceIntent(request, originalIntentName, intentScore, dialogflowReply);
            }

        } catch (Exception e) {
            log.error("메시지 처리 중 오류가 발생했습니다: {}", request, e);
            // TODO: 오류 발생 시 사용자에게 보낼 실패 메시지를 저장하는 로직을 추가할 수 있습니다.
        }
    }

    /**
     * Dialogflow의 의도 점수가 낮을 때의 처리 과정을 담당합니다.
     */
    private void handleLowConfidenceIntent(MessageRequest request, String originalIntentName, float originalIntentScore) {
        log.info("Dialogflow 점수가 낮음 ({}). Python AI 서버 호출을 시작합니다.", originalIntentScore);
        
        List<String> allIntents = List.of("제품_구매_문의", "배송_상태_확인", "기술_지원_요청", "환불_정책_안내"); // 임시 목록
        AiServerResponse aiResponse = aiServerService.classifyIntent(request.getMessage(), allIntents);

        if (aiResponse != null) {
            // AI 서버가 성공적으로 응답했을 경우
            String finalIntent = aiResponse.getFinal_intent();
            String fixedResponse = intentResponseService.getResponseForIntent(finalIntent); // 답변 전문가에게 답변을 물어봅니다.

            ChatMessage.AnalysisInfo analysisInfo = ChatMessage.AnalysisInfo.builder()
                    .engine(aiResponse.getEngine())
                    .intentName(finalIntent)
                    .originalIntentName(originalIntentName)
                    .originalIntentScore(originalIntentScore)
                    .build();

            chatService.saveMessage(
                    request.getSessionId(), request.getUserId(), "bot",
                    fixedResponse, request.getLanguageCode(), analysisInfo
            );
        } else {
            // AI 서버 호출에 실패했을 경우, Dialogflow의 답변이라도 대신 사용합니다 (Fallback).
            log.warn("Python AI 서버 호출에 실패했습니다. Dialogflow의 응답으로 대체합니다.");
            handleHighConfidenceIntent(request, originalIntentName, originalIntentScore, "죄송해요, 지금은 AI 서버에 문제가 있어 답변을 드릴 수 없어요.");
        }
    }

    /**
     * Dialogflow의 의도 점수가 높을 때의 처리 과정을 담당합니다.
     */
    private void handleHighConfidenceIntent(MessageRequest request, String intentName, float intentScore, String reply) {
        log.info("Dialogflow 점수가 높음 ({}). 기존 로직을 실행합니다.", intentScore);
        
        ChatMessage.AnalysisInfo analysisInfo = ChatMessage.AnalysisInfo.builder()
                .engine("dialogflow")
                .intentName(intentName)
                .originalIntentName(intentName)
                .originalIntentScore(intentScore)
                .build();
                
        chatService.saveMessage(
                request.getSessionId(), request.getUserId(), "bot",
                reply, request.getLanguageCode(), analysisInfo
        );
    }
}