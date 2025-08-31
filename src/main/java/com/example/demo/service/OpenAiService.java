package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${openai.model:gpt-4o}")
    private String model;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1/chat/completions")
            .defaultHeader("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"))
            .build();

    public Mono<String> getChatCompletion(String userMessage) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", userMessage)
                )
        );

        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    int start = response.indexOf("\"content\":\"") + 11;
                    int end = response.indexOf("\"", start);
                    if (start > 10 && end > start) {
                        return response.substring(start, end)
                                .replace("\n", "")
                                .replace("\"", "");
                    }
                    else {
                        return "답변을 가져오지 못했습니다.";
                    }
                });
    }
}