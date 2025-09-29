package com.example.demo.kafka;

import com.example.demo.dto.MessageRequest;
import com.example.demo.model.ChatMessage;
import com.example.demo.service.ChatService;
import com.example.demo.service.DialogflowService;
import com.example.demo.service.IntentBasedSearchService;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageConsumer {

    private final DialogflowService dialogflowService;
    private final ChatService chatService;
    private final IntentBasedSearchService intentBasedSearchService;

    /**
     * 채팅 메시지 소비자
     * 1) Dialogflow로 의도 분석
     * 2) 쇼핑 관련 의도면 IntentBasedSearchService로 DB 검색
     * 3) 분석/검색 결과를 포함하여 ChatService에 저장
     */
    @KafkaListener(topics = "chat-messages", groupId = "chatbot-group")
    public void consume(MessageRequest request) {
        log.info("Consumed message: {}", request);

        try {
            // 1. Dialogflow를 호출해서 의도를 분석합니다.
            DetectIntentResponse dialogflowResponse = dialogflowService.detectIntent(
                    request.getSessionId(),
                    request.getMessage(),
                    request.getLanguageCode()
            );

            // 2. Dialogflow 결과에서 필요한 정보를 추출합니다.
            String intentName = dialogflowResponse.getQueryResult().getIntent().getDisplayName();
            float intentScore = dialogflowResponse.getQueryResult().getIntentDetectionConfidence();
            String reply = dialogflowResponse.getQueryResult().getFulfillmentText();

            // 3. 파라미터 추출 (Dialogflow에서 전달된 엔티티 정보)
            Map<String, Object> parameters = extractParameters(dialogflowResponse);

            // 4. 의도 기반 상품 검색 수행 (쇼핑 관련 의도에 한함)
            ChatMessage.ShoppingData shoppingData = null;
            if (isShoppingIntent(intentName)) {
                log.info("쇼핑 관련 의도 감지: {}, 상품 검색 시작", intentName);
                shoppingData = intentBasedSearchService.searchProductsByIntent(
                        intentName, 
                        request.getMessage(), 
                        parameters
                );
                
                // 검색 결과가 있으면 응답 메시지 업데이트
                if (shoppingData != null && shoppingData.getTotalResults() > 0) {
                    reply = generateShoppingResponse(reply, shoppingData);
                }
            }

            // 5. 봇의 답변에 포함될 AnalysisInfo 객체를 생성합니다.
            ChatMessage.AnalysisInfo analysisInfo = ChatMessage.AnalysisInfo.builder()
                    .engine("dialogflow")
                    .intentName(intentName)
                    .originalIntentName(intentName)
                    .originalIntentScore(intentScore)
                    .build();

            // 6. 봇의 답변과 분석 결과, 쇼핑 데이터를 DB에 저장합니다.
            chatService.saveMessageWithShoppingData(
                    request.getSessionId(),
                    request.getUserId(),
                    "bot",
                    reply,
                    request.getLanguageCode(),
                    analysisInfo,
                    shoppingData
            );

            log.info("메시지 처리 완료 - 의도: {}, 상품 검색 결과: {}개", 
                    intentName, 
                    shoppingData != null ? shoppingData.getTotalResults() : 0);

        } catch (Exception e) {
            log.error("Error processing consumed message: {}", request, e);
        }
    }

    /**
     * Dialogflow 응답에서 파라미터를 추출
     */
    private Map<String, Object> extractParameters(DetectIntentResponse response) {
        Map<String, Object> parameters = new HashMap<>();
        
        try {
            var parametersStruct = response.getQueryResult().getParameters();
            if (parametersStruct != null) {
                parametersStruct.getFieldsMap().forEach((key, value) -> {
                    if (value.hasStringValue()) {
                        parameters.put(key, value.getStringValue());
                    } else if (value.hasNumberValue()) {
                        parameters.put(key, (int) value.getNumberValue());
                    } else if (value.hasBoolValue()) {
                        parameters.put(key, value.getBoolValue());
                    }
                });
            }
        } catch (Exception e) {
            log.warn("파라미터 추출 중 오류 발생", e);
        }
        
        return parameters;
    }

    /**
     * 쇼핑 관련 의도인지 확인
     */
    private boolean isShoppingIntent(String intentName) {
        if (intentName == null) return false;
        
        String lowerIntent = intentName.toLowerCase();
        return lowerIntent.contains("product") || 
               lowerIntent.contains("search") || 
               lowerIntent.contains("recommend") || 
               lowerIntent.contains("filter") || 
               lowerIntent.contains("compare") ||
               lowerIntent.contains("shopping") ||
               lowerIntent.contains("buy") ||
               lowerIntent.contains("purchase");
    }

    /**
     * 쇼핑 검색 결과를 포함한 응답 메시지 생성
     */
    private String generateShoppingResponse(String originalReply, ChatMessage.ShoppingData shoppingData) {
        if (shoppingData == null || shoppingData.getTotalResults() == 0) {
            return originalReply + "\n\n죄송합니다. 요청하신 상품을 찾을 수 없습니다.";
        }

        StringBuilder response = new StringBuilder(originalReply);
        response.append("\n\n");
        response.append("총 ").append(shoppingData.getTotalResults()).append("개의 상품을 찾았습니다.");
        
        if (shoppingData.getProducts() != null && !shoppingData.getProducts().isEmpty()) {
            response.append(" 아래에서 원하시는 상품을 선택해주세요.");
        }
        
        return response.toString();
    }
}