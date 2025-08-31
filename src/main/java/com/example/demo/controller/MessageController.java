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
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Validated
@Slf4j
@CrossOrigin(origins = {"http://localhost:8501", "https://hybrid-chatbot-frontend.vercel.app/"})
public class MessageController {

    private final OpenAiService openAiService;
    private final ChatService chatService;
    private final DialogflowService dialogflowService;

    @PostMapping("/receive")
    public Mono<ResponseEntity<MessageResponse>> receiveMessage(@Valid @RequestBody MessageRequest request) {
    
        log.info("Received message request: {}", request);
    
        // 1. 사용자 메시지를 먼저 DB에 저장합니다. (이때 analysisInfo는 null 입니다)
    chatService.saveMessage(
        request.getSessionId(),
        request.getUserId(),
        "user",
        request.getMessage(),
        request.getLanguageCode(),
        null // analysisInfo
    );

    // 2. Dialogflow를 호출해서 의도를 분석합니다.
    DetectIntentResponse dialogflowResponse = dialogflowService.detectIntent(
        request.getSessionId(),
        request.getMessage(),
        request.getLanguageCode()
    );
        log.info("Dialogflow response: {}", dialogflowResponse);

    // 3. Dialogflow 결과에서 필요한 정보를 추출합니다.
    String intentName = dialogflowResponse.getQueryResult().getIntent().getDisplayName();
    float intentScore = dialogflowResponse.getQueryResult().getIntentDetectionConfidence();
    String reply = dialogflowResponse.getQueryResult().getFulfillmentText();

    // 4. 봇의 답변에 포함될 AnalysisInfo 객체를 생성합니다.
    // (LLM 호출이 없는 경우, original 정보와 최종 intent 정보는 동일합니다)
    ChatMessage.AnalysisInfo analysisInfo = ChatMessage.AnalysisInfo.builder()
        .engine("dialogflow")
        .intentName(intentName)
        .originalIntentName(intentName)
        .originalIntentScore(intentScore)
        .build();

    // 5. 봇의 답변과 분석 결과를 DB에 저장합니다.
    chatService.saveMessage(
        request.getSessionId(),
        request.getUserId(),
        "bot",
        reply, // 봇의 답변 메시지
        request.getLanguageCode(),
        analysisInfo // 방금 만든 분석 정보
    );

    // 6. 최종 응답 객체를 만들어 프론트엔드로 보냅니다.
    MessageResponse finalResponse = MessageResponse.builder()
        .userId(request.getUserId())
        .response(reply)
        .build();
    
    // 7. 'Mono'로 감싸서 비동기적으로 반환합니다.
    return Mono.just(ResponseEntity.ok(finalResponse));

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
