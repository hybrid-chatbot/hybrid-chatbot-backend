// src/main/java/com/example/demo/dto/EmbeddingResponse.java
package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingResponse {
    private List<List<Double>> embeddings;
}