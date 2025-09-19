package com.example.demo.service;

import com.example.demo.dto.AiServerRequest;
import com.example.demo.dto.AiServerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServerService {

    private final RestTemplate restTemplate;

    // application.yml 파일에 우리가 추가할 Python 서버 주소입니다.
    @Value("${ai.server.url}")
    private String aiServerUrl;

    public AiServerResponse classifyIntent(String userQuestion, List<String> intentList) {
        // Python 서버로 보낼 요청 데이터를 만듭니다.
        AiServerRequest requestPayload = AiServerRequest.builder()
                .user_question(userQuestion)
                .intent_list(intentList)
                .build();

        // Python 서버의 "/zeroshot-intent" 주소로 POST 요청을 보냅니다.
        String url = aiServerUrl + "/zeroshot-intent";
        log.info("Python AI 서버에 요청을 보냅니다. URL: {}, Payload: {}", url, requestPayload);

        try {
            // restTemplate.postForObject()는 HTTP POST 요청을 보내고, 응답을 지정된 클래스(AiServerResponse.class)로 변환해줍니다.
            AiServerResponse response = restTemplate.postForObject(url, requestPayload, AiServerResponse.class);
            log.info("Python AI 서버로부터 응답을 받았습니다: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Python AI 서버 호출 중 오류가 발생했습니다.", e);
            // 오류가 발생하면 null을 반환하거나, 예외 처리를 할 수 있습니다.
            return null;
        }
    }
}