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

import java.util.Optional;

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
    private final ChatService chatMessageService;

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

    @GetMapping("/result/{sessionId}")
    public ResponseEntity<ChatMessage> getMessageResult(@PathVariable String sessionId) {
        // 서비스에게 sessionId를 주고, DB에서 가장 최근 메시지를 찾아달라고 요청합니다.
        Optional<ChatMessage> latestMessage = chatMessageService.findLatestMessageBySessionId(sessionId);

        // 메시지가 존재하고, 그 메시지에 분석 정보가 있는지 확인합니다.
        // ✨ Dialogflow 응답 또는 쇼핑 응답 모두 처리 가능하도록 수정
        if (latestMessage.isPresent() && 
            (latestMessage.get().getDialogflowResponse() != null || 
             latestMessage.get().getAnalysisInfo() != null)) {
            // 결과가 준비되었다면, 메시지 데이터와 함께 200 OK 상태를 보냅니다.
            return ResponseEntity.ok(latestMessage.get());
        } else {
            // 아직 결과가 준비되지 않았다면, 202 Accepted 상태를 보냅니다.
            // 이것은 프론트엔드에게 "요청은 받았는데, 아직 처리 중이야"라고 알려주는 신호입니다.
            return ResponseEntity.accepted().build();
        }
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
