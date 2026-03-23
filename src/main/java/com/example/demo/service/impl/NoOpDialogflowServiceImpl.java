package com.example.demo.service.impl;

import com.example.demo.service.DialogflowService;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Dialogflow가 비활성화된 환경(로컬 개발 등)에서 사용되는 NoOp 구현체.
 * AI 서버(FastAPI)로 바로 의도 분류를 위임합니다.
 */
@Service
@ConditionalOnProperty(name = "dialogflow.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class NoOpDialogflowServiceImpl implements DialogflowService {

    @Override
    public DetectIntentResponse detectIntent(String sessionId, String text, String languageCode) {
        log.info("Dialogflow 비활성화 상태 - AI 서버로 직접 의도 분류를 위임합니다. text: {}", text);
        return null;
    }
}
