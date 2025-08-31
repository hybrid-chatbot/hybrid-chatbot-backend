package com.example.demo.service;

import com.google.cloud.dialogflow.v2.DetectIntentResponse;

public interface DialogflowService {
    DetectIntentResponse detectIntent(String sessionId, String text, String languageCode);
}