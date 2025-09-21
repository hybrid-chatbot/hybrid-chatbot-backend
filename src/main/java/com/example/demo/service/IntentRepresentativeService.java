// src/main/java/com/example/demo/service/IntentRepresentativeService.java
package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
public class IntentRepresentativeService {

    // 각 의도를 가장 잘 대표하는 문장을 정의합니다.
    private static final Map<String, String> INTENT_REPRESENTATIVE_MAP = Map.of(
        "배송_조회", "배송 상태를 조회하고 싶어요.",
        "단순_환불_문의", "구매한 상품을 환불하고 싶습니다.",
        "주문_취소", "제 주문을 취소하고 싶습니다.",
        "쿠폰_및_세일_중복적용_문의", "쿠폰과 세일 할인을 같이 적용할 수 있나요?",
        "특별세일_상품_환불_문의", "특별 할인 기간에 구매한 상품을 환불할 수 있는지 궁금합니다."
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