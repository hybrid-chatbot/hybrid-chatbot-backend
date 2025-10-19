package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
public class IntentRepresentativeService {

    // ✨ 최종 11개 의도를 가장 잘 대표하는 문장을 정의합니다.
    private static final Map<String, String> INTENT_REPRESENTATIVE_MAP = Map.ofEntries(
        // 그룹 A: RAG가 해결할 복잡한 의도
        Map.entry("환불절차문의_VIP혜택", "VIP 등급에 따른 특별한 환불 절차가 궁금합니다."),
        Map.entry("환불금액문의_쿠폰사용", "쿠폰을 사용해서 구매한 상품의 환불 금액을 알고 싶어요."),
        Map.entry("환불혜택문의_등급변경", "VIP 등급이 변경되었는데 이전 등급의 환불 혜택이 유효한지 궁금합니다."),
        Map.entry("콜라보상품_중복할인_문의", "특별 콜라보 상품에 여러 할인과 쿠폰을 중복해서 적용할 수 있나요?"),
        Map.entry("이벤트_중복할인_문의", "이벤트 할인과 회원 등급 할인을 동시에 적용받고 싶습니다."),
        Map.entry("콜라보상품_환불_문의", "특별 콜라보 상품의 예외적인 환불 정책에 대해 문의합니다."),

        // 그룹 B: Dialogflow가 기본적으로 학습할 단순 의도
        Map.entry("배송_조회", "배송 상태를 조회하고 싶어요."),
        Map.entry("일반_환불_문의", "구매한 상품을 환불하고 싶습니다."),
        Map.entry("일반_교환_문의", "구매한 상품을 다른 것으로 교환하고 싶습니다."),
        Map.entry("주문_수정", "제 주문 내용을 수정하고 싶습니다."),
        Map.entry("주문_취소", "제 주문을 취소하고 싶습니다.")
    );

    /**
     * 의도 이름에 해당하는 대표 문장을 찾아 반환합니다.
     * @param intentName 찾고자 하는 의도의 이름
     * @return Optional로 감싸진 대표 문장. 의도가 없으면 Optional.empty()를 반환합니다.
     */
    public Optional<String> getRepresentativeText(String intentName) {
        return Optional.ofNullable(INTENT_REPRESENTATIVE_MAP.get(intentName));
    }
}