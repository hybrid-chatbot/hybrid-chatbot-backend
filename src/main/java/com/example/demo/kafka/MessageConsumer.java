// src/main/java/com/example/demo/kafka/MessageConsumer.java

package com.example.demo.kafka;

import com.example.demo.dto.MessageRequest;
import com.example.demo.service.ChatOrchestratorService; // ✨ '지휘자'를 import 합니다.
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

    // --- 이제 MessageConsumer는 '지휘자' 한 명만 알고 있으면 됩니다 ---
    private final ChatOrchestratorService chatOrchestratorService;

    /**
     * 채팅 메시지 소비자
     * 1) Dialogflow로 의도 분석
     * 2) 쇼핑 관련 의도면 IntentBasedSearchService로 DB 검색
     * 3) 분석/검색 결과를 포함하여 ChatService에 저장
     */
    @KafkaListener(topics = "chat-messages", groupId = "chatbot-group")
    public void consume(MessageRequest request) {
        log.info("Consumed message: {}", request);

        // 받은 메시지를 그대로 '지휘자'에게 전달하고 임무를 마칩니다.
        chatOrchestratorService.processMessage(request);
    }
}
