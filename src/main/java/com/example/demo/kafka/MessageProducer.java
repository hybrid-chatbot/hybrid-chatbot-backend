package com.example.demo.kafka;

import com.example.demo.dto.MessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducer {

    private static final String TOPIC = "chat-messages";
    private final KafkaTemplate<String, MessageRequest> kafkaTemplate;

    public void sendMessage(MessageRequest message) {
        log.info("Producing message: {}", message);
        this.kafkaTemplate.send(TOPIC, message);
    }
}