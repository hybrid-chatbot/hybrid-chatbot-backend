package com.example.demo.service.impl;

import com.google.cloud.dialogflow.v2.*;
import com.example.demo.service.DialogflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DialogflowServiceImpl implements DialogflowService {

    private final SessionsClient sessionsClient;
    private final String projectId;

    @Override
    public DetectIntentResponse detectIntent(String sessionId, String text, String languageCode) {
        try {

            log.info("Dialogflow detectIntent called with sessionId: {}, text: {}, languageCode: {}", sessionId, text, languageCode);
            SessionName session = SessionName.of(projectId, sessionId);
            TextInput.Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode);
            QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

            DetectIntentRequest request = DetectIntentRequest.newBuilder()
                    .setSession(session.toString())
                    .setQueryInput(queryInput)
                    .build();

            return sessionsClient.detectIntent(request);
        } catch (Exception e) {
            log.error("Error during Dialogflow API call", e);
            throw new RuntimeException("Dialogflow API 호출 중 오류 발생", e);
        }
    }
}