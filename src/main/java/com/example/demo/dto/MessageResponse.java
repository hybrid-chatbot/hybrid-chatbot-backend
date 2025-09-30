
package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageResponse {
    private String userId;
    private String response; // 최종 응답 메시지
    private AnalysisTrace analysisTrace; // 분석 추적 정보
}
