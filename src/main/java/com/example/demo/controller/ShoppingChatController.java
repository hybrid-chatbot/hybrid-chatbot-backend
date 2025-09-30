package com.example.demo.controller;

import com.example.demo.dto.ShoppingRequest;
import com.example.demo.dto.ShoppingMessageResponse;
import com.example.demo.service.ShoppingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shopping")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:8501", "https://hybrid-chatbot-frontend.vercel.app/"})
public class ShoppingChatController {

    private final ShoppingService shoppingService;

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
