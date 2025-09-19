package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class IntentResponseService {

    // LLM이 판단한 의도 이름(key)과 답변(value)을 짝지어주는 '답변 지도'.
    private static final Map<String, String> INTENT_RESPONSE_MAP = Map.of(
        "제품_구매_문의", "안녕하세요! 제품 구매와 관련하여 궁금한 점이 있으신가요? 원하시는 제품명을 말씀해주시면 재고 확인 및 구매 절차를 안내해 드릴게요.",
        "배송_상태_확인", "배송 상태를 확인해 드릴게요. 주문번호나 운송장 번호를 알려주시겠어요?",
        "기술_지원_요청", "기술적인 문제가 발생했군요. 어떤 어려움을 겪고 계신지 구체적으로 설명해주시면, 담당 부서에 전달하여 신속히 도와드리겠습니다.",
        "환불_정책_안내", "환불 정책에 대해 안내해 드립니다. 저희 제품은 구매 후 7일 이내에 미개봉 상태인 경우에만 환불이 가능합니다. 더 자세한 정보가 필요하신가요?"
        // TODO: 나중에 실제 의도와 답변에 맞춰서 수정
    );

    private static final String DEFAULT_RESPONSE = "죄송합니다, 문의하신 내용을 이해하지 못했습니다. 조금 더 자세히 설명해주시겠어요?";

    /**
     * 의도 이름에 해당하는 고정 답변을 반환합니다.
     * @param intentName 찾고자 하는 의도의 이름
     * @return 해당 의도에 매칭된 답변. 만약 해당하는 의도가 없으면 기본 답변을 반환합니다.
     */
    public String getResponseForIntent(String intentName) {
        return INTENT_RESPONSE_MAP.getOrDefault(intentName, DEFAULT_RESPONSE);
    }
}