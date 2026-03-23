package com.example.demo.kafka;

import com.example.demo.dto.MessageRequest;
import com.example.demo.service.ChatOrchestratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageProducer {

    private static final String TOPIC = "chat-messages";

    @Autowired(required = false)
    private KafkaTemplate<String, MessageRequest> kafkaTemplate;

    @Autowired
    private ChatOrchestratorService chatOrchestratorService;

    public void sendMessage(MessageRequest message) {
        if (kafkaTemplate != null) {
            log.info("Producing message via Kafka: {}", message);
            this.kafkaTemplate.send(TOPIC, message);
        } else {
            log.info("Kafka 비활성화 - 직접 메시지 처리: {}", message);
            chatOrchestratorService.processMessage(message);
        }
    }
}