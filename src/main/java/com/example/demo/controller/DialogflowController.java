package com.example.demo.controller;

import com.example.demo.service.DialogflowService;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dialogflow")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8501", "http://localhost:8051", "http://localhost:5173"})
public class DialogflowController {

    private final DialogflowService dialogflowService;

    @PostMapping("/detect-intent")
    public ResponseEntity<?> detectIntent(
            @RequestParam String sessionId,
            @RequestParam String text,
            @RequestParam(defaultValue = "ko") String languageCode) {

        System.out.println("Received request - sessionId: " + sessionId + ", text: " + text);
        DetectIntentResponse response = dialogflowService.detectIntent(sessionId, text, languageCode);

        // fulfillmentText 등 필요한 정보만 추출
        String fulfillmentText = response.getQueryResult().getFulfillmentText();
        String intentName = response.getQueryResult().getIntent().getDisplayName();
        float intentScore = response.getQueryResult().getIntentDetectionConfidence();

        // 결과를 Map에 담아 반환
        Map<String, Object> result = new HashMap<>();
        result.put("fulfillmentText", fulfillmentText);
        result.put("intentName", intentName);
        result.put("intentScore", intentScore);

        return ResponseEntity.ok(result);
    }
}