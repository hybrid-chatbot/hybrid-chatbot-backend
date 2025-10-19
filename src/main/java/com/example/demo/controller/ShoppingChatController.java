package com.example.demo.controller;

import com.example.demo.dto.MessageRequest;
import com.example.demo.dto.ShoppingMessageResponse;
import com.example.demo.model.ChatMessage;
import com.example.demo.service.ChatService;
import com.example.demo.service.SimpleShoppingService;
import com.example.navershopping.entity.NaverShoppingItem;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 쇼핑 챗봇 컨트롤러
 * 
 * 이 컨트롤러는 쇼핑몰 챗봇의 REST API 엔드포인트를 제공합니다:
 * - 사용자 채팅 메시지 처리
 * - 상품 검색 (키워드, 브랜드, 카테고리, 가격별)
 * - 인기 상품 및 최신 상품 조회
 * - 상품 상세 정보 조회
 * 
 * 주요 특징:
 * - CORS 설정으로 프론트엔드와 통신 가능
 * - 모든 요청/응답에 대한 로깅
 * - 에러 처리 및 적절한 HTTP 상태 코드 반환
 */
@Slf4j
@RestController
@RequestMapping("/api/shopping-chat") // API 기본 경로
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8501", "https://hybrid-chatbot-frontend.vercel.app/"}) // CORS 설정
public class ShoppingChatController {

    // 쇼핑 서비스 (상품 검색, 추천 등)
    private final SimpleShoppingService simpleShoppingService;
    
    // 채팅 서비스 (메시지 저장, 조회 등)
    private final ChatService chatService;

    @PostMapping("/search")
    public ResponseEntity<ShoppingMessageResponse> searchProducts(@Valid @RequestBody ShoppingRequest request) {
        log.info("쇼핑 검색 요청: {}", request);

        try {
            ShoppingMessageResponse response = shoppingService.searchProducts(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("쇼핑 검색 중 오류 발생", e);
            ShoppingMessageResponse errorResponse = ShoppingMessageResponse.builder()
                    .response("상품 검색 중 오류가 발생했습니다.")
                    .messageType("text")
                    .timestamp(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
