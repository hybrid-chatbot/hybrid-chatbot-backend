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

import java.util.List;

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

    /**
     * 쇼핑 채팅 메시지 처리
     * 
     * 사용자가 채팅으로 입력한 메시지를 받아서 상품을 검색하고 응답을 반환합니다.
     * 
     * 처리 과정:
     * 1. 사용자 메시지를 데이터베이스에 저장
     * 2. 메시지를 키워드로 사용하여 상품 검색
     * 3. 검색 결과를 봇 응답으로 저장
     * 4. 상품 정보가 포함된 응답 반환
     * 
     * @param request 사용자 메시지 요청 (세션ID, 사용자ID, 메시지, 언어코드 포함)
     * @return 상품 검색 결과가 포함된 응답
     */
    @PostMapping("/message")
    public ResponseEntity<ShoppingMessageResponse> processShoppingMessage(@Valid @RequestBody MessageRequest request) {
        log.info("쇼핑 채팅 메시지 처리 요청: {}", request);

        try {
            // 1단계: 사용자 메시지를 데이터베이스에 저장
            chatService.saveMessage(
                    request.getSessionId(), // 세션 ID
                    request.getUserId(), // 사용자 ID
                    "user", // 발신자 (사용자)
                    request.getMessage(), // 메시지 내용
                    request.getLanguageCode(), // 언어 코드
                    null // 분석 정보 (사용자 메시지는 null)
            );

            // 2단계: 사용자 메시지를 키워드로 사용하여 상품 검색
            ShoppingMessageResponse response = simpleShoppingService.searchProducts(request.getMessage());

            // 3단계: 봇의 응답을 데이터베이스에 저장
            chatService.saveMessage(
                    request.getSessionId(), // 세션 ID
                    request.getUserId(), // 사용자 ID
                    "bot", // 발신자 (봇)
                    response.getResponse(), // 봇 응답 메시지
                    request.getLanguageCode(), // 언어 코드
                    createAnalysisInfo(response) // 분석 정보 생성
            );

            // 4단계: 검색 결과 반환
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("쇼핑 채팅 메시지 처리 실패", e);
            return ResponseEntity.internalServerError().build(); // 500 에러 반환
        }
    }

    /**
     * 상품 검색 API (GET 방식)
     * 
     * 키워드로 상품을 검색합니다.
     * 
     * @param query 검색할 키워드 (예: "나이키 운동화", "청바지")
     * @return 검색된 상품 목록이 포함된 응답
     */
    @GetMapping("/search")
    public ResponseEntity<ShoppingMessageResponse> searchProducts(@RequestParam String query) {
        log.info("상품 검색 API 요청 (GET) - 쿼리: {}", query);

        try {
            // 쇼핑 서비스를 통해 상품 검색
            ShoppingMessageResponse response = simpleShoppingService.searchProducts(query);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("상품 검색 API 실패 (GET) - 쿼리: {}", query, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 상품 검색 API (POST 방식) - 프론트엔드 호환용
     * 
     * 프론트엔드에서 POST 방식으로 상품을 검색합니다.
     * 기존 상품검색 흐름을 그대로 유지하면서 POST 방식으로 처리합니다.
     * 
     * @param request 상품 검색 요청 (세션ID, 사용자ID, 메시지, 언어코드 포함)
     * @return 검색된 상품 목록이 포함된 응답
     */
    @PostMapping("/search")
    public ResponseEntity<ShoppingMessageResponse> searchProductsPost(@Valid @RequestBody MessageRequest request) {
        log.info("상품 검색 API 요청 (POST) - 메시지: {}, 세션ID: {}", request.getMessage(), request.getSessionId());

        try {
            // 1단계: 사용자 메시지를 데이터베이스에 저장
            chatService.saveMessage(
                    request.getSessionId(), // 세션 ID
                    request.getUserId(), // 사용자 ID
                    "user", // 발신자 (사용자)
                    request.getMessage(), // 메시지 내용
                    request.getLanguageCode(), // 언어 코드
                    null // 분석 정보 (사용자 메시지는 null)
            );

            // 2단계: 의도분석 기반 상품검색 사용
            ShoppingMessageResponse response = simpleShoppingService.searchProductsWithIntent(
                    request.getMessage(), 
                    request.getSessionId(), 
                    request.getLanguageCode()
            );

            // 3단계: 봇의 응답을 데이터베이스에 저장
            chatService.saveMessage(
                    request.getSessionId(), // 세션 ID
                    request.getUserId(), // 사용자 ID
                    "bot", // 발신자 (봇)
                    response.getResponse(), // 봇 응답 메시지
                    request.getLanguageCode(), // 언어 코드
                    createAnalysisInfo(response) // 분석 정보 생성
            );

            // 4단계: 검색 결과 반환
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("상품 검색 API 실패 (POST) - 메시지: {}", request.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 브랜드별 상품 검색 API
     * 
     * 특정 브랜드의 상품들을 검색합니다.
     * 
     * @param brand 검색할 브랜드명 (예: "나이키", "아디다스")
     * @return 해당 브랜드의 상품 목록이 포함된 응답
     */
    @GetMapping("/brand")
    public ResponseEntity<ShoppingMessageResponse> searchByBrand(@RequestParam String brand) {
        log.info("브랜드별 상품 검색 API 요청 - 브랜드: {}", brand);

        try {
            // 브랜드별 상품 검색
            ShoppingMessageResponse response = simpleShoppingService.searchProductsByBrand(brand);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("브랜드별 상품 검색 API 실패 - 브랜드: {}", brand, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 카테고리별 상품 검색 API
     * 
     * 특정 카테고리의 상품들을 검색합니다.
     * 
     * @param category 검색할 카테고리명 (예: "청바지", "운동화", "가방")
     * @return 해당 카테고리의 상품 목록이 포함된 응답
     */
    @GetMapping("/category")
    public ResponseEntity<ShoppingMessageResponse> searchByCategory(@RequestParam String category) {
        log.info("카테고리별 상품 검색 API 요청 - 카테고리: {}", category);

        try {
            // 카테고리별 상품 검색
            ShoppingMessageResponse response = simpleShoppingService.searchProductsByCategory(category);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("카테고리별 상품 검색 API 실패 - 카테고리: {}", category, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 가격 범위별 상품 검색 API
     * 
     * 지정된 가격 범위 내의 상품들을 검색합니다.
     * 
     * @param minPrice 최소 가격 (원)
     * @param maxPrice 최대 가격 (원)
     * @return 해당 가격 범위의 상품 목록이 포함된 응답
     */
    @GetMapping("/price-range")
    public ResponseEntity<ShoppingMessageResponse> searchByPriceRange(
            @RequestParam Integer minPrice,
            @RequestParam Integer maxPrice) {

        log.info("가격 범위별 상품 검색 API 요청 - 최소: {}원, 최대: {}원", minPrice, maxPrice);

        try {
            // 가격 범위별 상품 검색
            ShoppingMessageResponse response = simpleShoppingService.searchProductsByPriceRange(minPrice, maxPrice);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("가격 범위별 상품 검색 API 실패 - 최소: {}원, 최대: {}원", minPrice, maxPrice, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 인기 상품 조회 API
     * 
     * 검색 횟수가 많은 상위 상품들을 조회합니다.
     * 
     * @return 인기 상품 목록이 포함된 응답
     */
    @GetMapping("/popular")
    public ResponseEntity<ShoppingMessageResponse> getPopularProducts() {
        log.info("인기 상품 조회 API 요청");

        try {
            // 인기 상품 조회
            ShoppingMessageResponse response = simpleShoppingService.getPopularProducts();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("인기 상품 조회 API 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 최신 상품 조회 API
     * 
     * 최근에 검색된 상품들을 조회합니다.
     * 
     * @return 최신 상품 목록이 포함된 응답
     */
    @GetMapping("/recent")
    public ResponseEntity<ShoppingMessageResponse> getRecentProducts() {
        log.info("최신 상품 조회 API 요청");

        try {
            // 최신 상품 조회
            ShoppingMessageResponse response = simpleShoppingService.getRecentProducts();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("최신 상품 조회 API 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 상품 상세 정보 조회 API
     * 
     * 특정 상품의 상세 정보를 조회합니다.
     * 
     * @param id 조회할 상품의 ID
     * @return 상품 상세 정보 (없으면 404 에러)
     */
    @GetMapping("/product/{id}")
    public ResponseEntity<NaverShoppingItem> getProductDetail(@PathVariable Long id) {
        log.info("상품 상세 정보 조회 API 요청 - ID: {}", id);

        try {
            // 상품 상세 정보 조회
            NaverShoppingItem product = simpleShoppingService.getProductDetail(id);
            
            if (product != null) {
                return ResponseEntity.ok(product); // 상품 정보 반환
            } else {
                return ResponseEntity.notFound().build(); // 404 에러 (상품 없음)
            }

        } catch (Exception e) {
            log.error("상품 상세 정보 조회 API 실패 - ID: {}", id, e);
            return ResponseEntity.internalServerError().build(); // 500 에러
        }
    }


    /**
     * 더미 데이터 생성 API
     * 
     * 테스트를 위해 나이키 운동화 더미 데이터를 생성합니다.
     * 
     * @return 생성된 더미 데이터 개수
     */
    @PostMapping("/create-dummy-data")
    public ResponseEntity<String> createDummyData() {
        log.info("더미 데이터 생성 API 요청");
        
        try {
            int count = simpleShoppingService.createDummyNikeShoes();
            return ResponseEntity.ok("더미 데이터 " + count + "개가 생성되었습니다.");
            
        } catch (Exception e) {
            log.error("더미 데이터 생성 실패", e);
            return ResponseEntity.internalServerError().body("더미 데이터 생성 실패: " + e.getMessage());
        }
    }

    /**
     * 데이터베이스 상품 조회 API
     * 
     * 데이터베이스에 저장된 상품들을 조회합니다.
     * 
     * @return 저장된 상품 목록
     */
    @GetMapping("/debug/products")
    public ResponseEntity<List<NaverShoppingItem>> getProducts() {
        log.info("데이터베이스 상품 조회 API 요청");
        
        try {
            List<NaverShoppingItem> products = simpleShoppingService.getAllProducts();
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            log.error("상품 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 분석 정보 생성
     * 
     * 채팅 메시지에 저장할 분석 정보를 생성합니다.
     * 
     * @param response 쇼핑 응답 객체
     * @return 분석 정보 객체
     */
    private ChatMessage.AnalysisInfo createAnalysisInfo(ShoppingMessageResponse response) {
        return ChatMessage.AnalysisInfo.builder()
                .engine("shopping-chatbot") // 분석 엔진명
                .intentName(response.getAnalysisInfo() != null ? response.getAnalysisInfo().getIntentType() : "unknown") // 의도명
                .originalIntentName(response.getAnalysisInfo() != null ? response.getAnalysisInfo().getIntentType() : "unknown") // 원본 의도명
                .originalIntentScore(1.0f) // 의도 신뢰도 (1.0 = 100%)
                .build();
    }
}
