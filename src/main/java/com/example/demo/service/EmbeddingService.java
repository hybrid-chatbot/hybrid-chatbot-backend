// src/main/java/com/example/demo/service/EmbeddingService.java
package com.example.demo.service;

import com.example.demo.dto.EmbeddingRequest;
import com.example.demo.dto.EmbeddingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    /**
     * Python AI 서버에 텍스트 리스트를 보내고 임베딩 벡터를 받아옵니다.
     * @param texts 임베딩을 원하는 텍스트 리스트
     * @return 각 텍스트에 대한 임베딩 벡터 리스트
     */
    @Cacheable("embeddings")
    public List<List<Double>> getEmbeddings(List<String> texts) {
        EmbeddingRequest requestPayload = EmbeddingRequest.builder()
                .texts(texts)
                .build();

        String url = aiServerUrl + "/embed";
        // log.info("Python AI 서버에 임베딩을 요청합니다. URL: {}", url);

        try {
            EmbeddingResponse response = restTemplate.postForObject(url, requestPayload, EmbeddingResponse.class);
            if (response != null && response.getEmbeddings() != null) {
                // log.info("Python AI 서버로부터 {}개의 임베딩 벡터를 받았습니다.", response.getEmbeddings().size());
                return response.getEmbeddings();
            }
            return null;
        } catch (Exception e) {
            // log.error("Python AI 서버 임베딩 호출 중 오류가 발생했습니다.", e);
            return null;
        }
    }
}