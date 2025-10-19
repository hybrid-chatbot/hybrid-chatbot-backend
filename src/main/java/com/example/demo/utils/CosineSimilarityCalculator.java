// src/main/java/com/example/demo/utils/CosineSimilarityCalculator.java
package com.example.demo.utils;

import java.util.List;

public class CosineSimilarityCalculator {

    /**
     * 두 벡터 간의 코사인 유사도를 계산합니다.
     * @param vec1 첫 번째 벡터 (숫자 리스트)
     * @param vec2 두 번째 벡터 (숫자 리스트)
     * @return 0과 1 사이의 코사인 유사도 점수. 계산할 수 없는 경우 -1.0을 반환합니다.
     */
    public static double calculate(List<Double> vec1, List<Double> vec2) {
        // 두 벡터가 비어있거나 크기가 다르면 계산할 수 없습니다.
        if (vec1 == null || vec2 == null || vec1.isEmpty() || vec2.isEmpty() || vec1.size() != vec2.size()) {
            return -1.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            normA += Math.pow(vec1.get(i), 2);
            normB += Math.pow(vec2.get(i), 2);
        }

        // 벡터의 크기가 0이면 0으로 나누는 오류가 발생하므로, 이를 방지합니다.
        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}