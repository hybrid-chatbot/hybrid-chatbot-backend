// src/main/java/com/example/demo/dto/AnalysisTrace.java
package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisTrace {

    // Dialogflow의 1차 분석 결과
    private String dialogflowIntent;
    private double dialogflowScore;

    // 2차 안전망 검증 결과 (해당 시)
    private Double similarityScore; // Double을 사용하여 null 값을 표현할 수 있도록 합니다.
    private String safetyNetJudgement; // 예: "RAG 호출 결정"

    // RAG의 최종 분석 결과 (해당 시)
    private String ragFinalIntent;
    private List<String> retrievedDocuments; // 참고한 지식 문서

    // 최종적으로 선택된 엔진
    private String finalEngine;

    // Builder 패턴 구현
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String dialogflowIntent;
        private double dialogflowScore;
        private Double similarityScore;
        private String safetyNetJudgement;
        private String ragFinalIntent;
        private List<String> retrievedDocuments;
        private String finalEngine;

        public Builder dialogflowIntent(String dialogflowIntent) {
            this.dialogflowIntent = dialogflowIntent;
            return this;
        }

        public Builder dialogflowScore(double dialogflowScore) {
            this.dialogflowScore = dialogflowScore;
            return this;
        }

        public Builder similarityScore(Double similarityScore) {
            this.similarityScore = similarityScore;
            return this;
        }

        public Builder safetyNetJudgement(String safetyNetJudgement) {
            this.safetyNetJudgement = safetyNetJudgement;
            return this;
        }

        public Builder ragFinalIntent(String ragFinalIntent) {
            this.ragFinalIntent = ragFinalIntent;
            return this;
        }

        public Builder retrievedDocuments(List<String> retrievedDocuments) {
            this.retrievedDocuments = retrievedDocuments;
            return this;
        }

        public Builder finalEngine(String finalEngine) {
            this.finalEngine = finalEngine;
            return this;
        }

        public AnalysisTrace build() {
            return new AnalysisTrace(dialogflowIntent, dialogflowScore, similarityScore, 
                    safetyNetJudgement, ragFinalIntent, retrievedDocuments, finalEngine);
        }
    }
}