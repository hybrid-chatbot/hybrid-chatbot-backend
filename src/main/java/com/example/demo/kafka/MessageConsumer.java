package com.example.demo.kafka;

import com.example.demo.dto.MessageRequest;
import com.example.demo.model.ChatMessage;
import com.example.demo.service.ChatService;
import com.example.demo.service.DialogflowService;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageConsumer {

    private final DialogflowService dialogflowService;
    private final ChatService chatService;

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

            // 3. 봇의 답변에 포함될 AnalysisInfo 객체를 생성합니다.
            ChatMessage.AnalysisInfo analysisInfo = ChatMessage.AnalysisInfo.builder()
                    .engine("dialogflow")
                    .intentName(intentName)
                    .originalIntentName(intentName)
                    .originalIntentScore(intentScore)
                    .build();

            // 4. 봇의 답변과 분석 결과를 DB에 저장합니다.
            chatService.saveMessage(
                    request.getSessionId(),
                    request.getUserId(),
                    "bot",
                    reply,
                    request.getLanguageCode(),
                    analysisInfo
            );
        } catch (Exception e) {
            log.error("Error processing consumed message: {}", request, e);
        }
    }
}