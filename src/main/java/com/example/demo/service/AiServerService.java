package com.example.demo.service;

import com.example.demo.dto.AiServerRequest;
import com.example.demo.dto.AiServerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
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
        log.info("🤖 Python AI 서버에 요청을 보냅니다. URL: {}", url);
        log.debug("📤 요청 페이로드: {}", requestPayload);

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AiServerRequest> requestEntity = new HttpEntity<>(requestPayload, headers);

        // 재시도 로직 (최대 3회)
        int maxRetries = 3;
        int retryDelay = 2000; // 2초

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("🔄 시도 {}/{}: Python AI 서버 호출 중...", attempt, maxRetries);
                
                ResponseEntity<AiServerResponse> responseEntity = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, AiServerResponse.class);
                
                AiServerResponse response = responseEntity.getBody();
                log.info("✅ Python AI 서버로부터 응답을 받았습니다 (시도 {}): {}", attempt, response);
                return response;
                
            } catch (HttpClientErrorException e) {
                log.error("❌ 클라이언트 오류 (시도 {}/{}): HTTP {} - {}", attempt, maxRetries, e.getStatusCode(), e.getResponseBodyAsString());
                if (attempt == maxRetries) return null;
                
            } catch (HttpServerErrorException e) {
                log.error("❌ 서버 오류 (시도 {}/{}): HTTP {} - {}", attempt, maxRetries, e.getStatusCode(), e.getResponseBodyAsString());
                if (attempt == maxRetries) return null;
                
            } catch (ResourceAccessException e) {
                log.error("❌ 연결 오류 (시도 {}/{}): {}", attempt, maxRetries, e.getMessage());
                if (attempt == maxRetries) return null;
                
            } catch (Exception e) {
                log.error("❌ 예상치 못한 오류 (시도 {}/{}): {}", attempt, maxRetries, e.getMessage(), e);
                if (attempt == maxRetries) return null;
            }
            
            // 재시도 전 대기
            if (attempt < maxRetries) {
                try {
                    int delay = retryDelay * attempt; // 점진적 지연
                    log.info("⏳ {}ms 후 재시도합니다...", delay);
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("재시도 대기 중 인터럽트 발생", ie);
                    break;
                }
            }
        }
        
        log.error("💥 Python AI 서버 호출 최종 실패 ({}회 시도 후)", maxRetries);
        return null;
    }
}