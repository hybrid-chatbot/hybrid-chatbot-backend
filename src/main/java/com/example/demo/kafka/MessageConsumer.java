// src/main/java/com/example/demo/kafka/MessageConsumer.java

package com.example.demo.kafka;

import com.example.demo.dto.MessageRequest;
import com.example.demo.service.ChatOrchestratorService; // ✨ '지휘자'를 import 합니다.
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageConsumer {

    // --- 이제 MessageConsumer는 '지휘자' 한 명만 알고 있으면 됩니다 ---
    private final ChatOrchestratorService chatOrchestratorService;

    @KafkaListener(topics = "chat-messages", groupId = "chatbot-group")
    public void consume(MessageRequest request) {
        log.info("Consumed message: {}", request);

        // 받은 메시지를 그대로 '지휘자'에게 전달하고 임무를 마칩니다.
        chatOrchestratorService.processMessage(request);
    }
}