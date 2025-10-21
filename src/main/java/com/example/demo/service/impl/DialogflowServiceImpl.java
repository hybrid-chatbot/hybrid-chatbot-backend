package com.example.demo.service.impl;

import com.google.cloud.dialogflow.v2.*;
import com.example.demo.service.DialogflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DialogflowServiceImpl implements DialogflowService {

    private final SessionsClient sessionsClient;
    private final String projectId;

    @Override
    public DetectIntentResponse detectIntent(String sessionId, String text, String languageCode) {
        try {
            SessionName session = SessionName.of(projectId, sessionId);
            TextInput.Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode);
            QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

            DetectIntentRequest request = DetectIntentRequest.newBuilder()
                    .setSession(session.toString())
                    .setQueryInput(queryInput)
                    .build();

            return sessionsClient.detectIntent(request);
        } catch (Exception e) {
            throw new RuntimeException("Dialogflow API 호출 중 오류 발생", e);
        }
    }
}