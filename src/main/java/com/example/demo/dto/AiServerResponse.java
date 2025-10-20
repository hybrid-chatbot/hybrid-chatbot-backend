package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiServerResponse {
    private String final_intent;
    private String bot_response;
    private String engine;
    private Float confidence; // RAG 모델의 신뢰도 (0.0 ~ 1.0)
}