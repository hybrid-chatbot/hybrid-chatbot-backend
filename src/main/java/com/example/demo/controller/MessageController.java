package com.example.demo.controller;

import com.example.demo.dto.MessageRequest;
import com.example.demo.dto.MessageResponse;
import com.example.demo.service.ChatService;
import com.example.demo.model.ChatMessage;
import com.example.demo.service.OpenAiService;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;

import lombok.extern.slf4j.Slf4j;

import com.example.demo.service.DialogflowService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.springframework.http.ResponseEntity;
import java.util.List;
import com.example.demo.kafka.MessageProducer;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Validated
@Slf4j
@CrossOrigin(origins = {"http://localhost:8501", "https://hybrid-chatbot-frontend.vercel.app/"})
public class MessageController {

    private final OpenAiService openAiService;
    private final ChatService chatService;
    private final MessageProducer messageProducer;

    @PostMapping("/receive")
    public Mono<ResponseEntity<MessageResponse>> receiveMessage(@Valid @RequestBody MessageRequest request) {
        log.info("Received message request: {}", request);

        // 1. 사용자 메시지를 먼저 DB에 저장합니다.
        chatService.saveMessage(
                request.getSessionId(),
                request.getUserId(),
                "user",
                request.getMessage(),
                request.getLanguageCode(),
                null
        );

        // 2. Kafka로 메시지를 전송합니다. (이제 직접 처리하지 않아요!)
        messageProducer.sendMessage(request);

        // 3. "접수되었습니다" 라는 의미의 응답을 즉시 보냅니다.
        MessageResponse response = MessageResponse.builder()
                .userId(request.getUserId())
                .response("Message received and is being processed.") // 임시 응답 메시지
                .build();

        return Mono.just(ResponseEntity.ok(response));
    }


    @GetMapping("/send")
    public ResponseEntity<List<ChatMessage>> sendMessages(@RequestParam String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build(); // ✅ 400 Bad Request 반환
        }
        List<ChatMessage> messages = chatService.getRecentMessagesByUserId(userId);
        return ResponseEntity.ok(messages);
    }
}
