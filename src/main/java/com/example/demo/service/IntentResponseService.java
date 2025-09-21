package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class IntentResponseService {

    // ✨ 실험에 사용할 5개 의도 전체에 대한 답변을 정의합니다.
    private static final Map<String, String> INTENT_RESPONSE_MAP = Map.of(
        // --- Dialogflow가 학습할 단순 의도 3개 ---
        "배송_조회", "배송 상태를 확인해 드릴게요. 주문번호나 운송장 번호를 알려주시겠어요?",
        "단순_환불_문의", "환불 정책에 대해 안내해 드립니다. 저희 제품은 구매 후 14일 이내에 환불이 가능합니다. (단, 특별 할인 상품 등 일부 예외가 있을 수 있습니다.)",
        "주문_취소", "주문 취소를 도와드리겠습니다. 취소하려는 주문의 주문번호를 알려주세요.",

        // --- RAG가 해결할 복합 의도 2개 ---
        "쿠폰_및_세일_중복적용_문의", "고객님, 여러 혜택의 중복 적용에 대해 문의주셨군요! 저희 정책상 중복 적용 여부는 프로모션별로 상이할 수 있어, 확인 후 정확한 안내를 도와드리겠습니다.",
        "특별세일_상품_환불_문의", "특별 세일 기간에 구매하신 상품의 환불에 대해 문의주셨군요. 해당 상품은 저희 특별 규정에 따라 현금 환불은 어렵지만, 다른 상품으로의 교환은 가능합니다. 교환 절차를 안내해 드릴까요?"
    );

    // Default Fallback Intent에 대한 답변도 추가해줍니다.
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