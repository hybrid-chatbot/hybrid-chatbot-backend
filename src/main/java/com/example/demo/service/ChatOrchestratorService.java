// src/main/java/com/example/demo/service/ChatOrchestratorService.java

package com.example.demo.service;

import com.example.demo.dto.AiServerResponse;
import com.example.demo.dto.AnalysisTrace; 
import com.example.demo.dto.MessageRequest;
import com.example.demo.dto.ShoppingMessageResponse;
import com.example.demo.model.ChatMessage;
import com.example.demo.utils.CosineSimilarityCalculator;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatOrchestratorService {

    // 임계치 상수
    private static final double DIALOGFLOW_SCORE_THRESHOLD = 0.6;
    private static final double SIMILARITY_SCORE_THRESHOLD = 0.0; // 의미 유사도 기준 점수

    // 서비스 의존성 주입
    private final DialogflowService dialogflowService;
    private final AiServerService aiServerService;
    private final IntentResponseService intentResponseService;
    private final ChatService chatService;
    private final EmbeddingService embeddingService; // 
    private final IntentRepresentativeService intentRepresentativeService; //  문장 관리자 전문가
    private final SimpleShoppingService simpleShoppingService; // ✨ 쇼핑 서비스 추가

    public void processMessage(MessageRequest request) {
        // ✨ 1. '생각의 흔적'을 기록할 노트를 새로 만듭니다.
        AnalysisTrace.Builder traceBuilder = AnalysisTrace.builder();

        try {
            DetectIntentResponse dialogflowResponse = dialogflowService.detectIntent(
                    request.getSessionId(),
                    request.getMessage(),
                    request.getLanguageCode()
            );

            String originalIntentName = dialogflowResponse.getQueryResult().getIntent().getDisplayName();
            float intentScore = dialogflowResponse.getQueryResult().getIntentDetectionConfidence();
            String dialogflowReply = dialogflowResponse.getQueryResult().getFulfillmentText();

            // ✨ 2. Dialogflow의 1차 분석 결과를 노트에 기록합니다.
            traceBuilder.dialogflowIntent(originalIntentName).dialogflowScore(intentScore);

<<<<<<< HEAD
            // ✨ 3. 쇼핑 의도 감지 시 상품 검색 처리
            if (isShoppingIntent(originalIntentName, request.getMessage())) {
                log.info("쇼핑 의도 감지됨: {} - 상품 검색 시작", originalIntentName);
                handleShoppingIntent(request, originalIntentName, intentScore, traceBuilder);
                return; // 쇼핑 처리 완료 후 종료
            }

            if (intentScore >= DIALOGFLOW_SCORE_THRESHOLD) {
                // ✨ 4. 2차 검증을 수행하고, 그 과정을 노트에 기록합니다.
=======
            if (intentScore >= DIALOGFLOW_SCORE_THRESHOLD) {
                // ✨ 3. 2차 검증을 수행하고, 그 과정을 노트에 기록합니다.
>>>>>>> f5217b1b3c80a64be7ec5f31fe30a5203797c244
                if (isSemanticallySimilar(request.getMessage(), originalIntentName, traceBuilder)) {
                    log.info("Dialogflow 점수가 높고(score: {}), 의미 유사도 검증 통과. Dialogflow 응답 사용.", intentScore);
                    handleHighConfidenceIntent(request, originalIntentName, intentScore, dialogflowReply, traceBuilder.build());
                } else {
                    log.warn("Dialogflow 점수는 높았지만(score: {}), 의미 유사도가 낮아 RAG를 호출합니다.", intentScore);
                    handleLowConfidenceIntent(request, originalIntentName, intentScore, traceBuilder);
                }
            } else {
                handleLowConfidenceIntent(request, originalIntentName, intentScore, traceBuilder);
            }

        } catch (Exception e) {
            log.error("메시지 처리 중 오류가 발생했습니다: {}", request, e);
        }
    }

    /**
     * 사용자의 질문과 Dialogflow가 예측한 의도의 대표 문장 간의 의미 유사도를 검증합니다.
     * @return 의미적으로 유사하면 true, 그렇지 않으면 false
     */
    private boolean isSemanticallySimilar(String userQuestion, String intentName, AnalysisTrace.Builder traceBuilder) {
        Optional<String> representativeTextOpt = intentRepresentativeService.getRepresentativeText(intentName);
        if (representativeTextOpt.isEmpty()) {
            return true;
        }
        String representativeText = representativeTextOpt.get();

        List<List<Double>> embeddings = embeddingService.getEmbeddings(List.of(userQuestion, representativeText));
        if (embeddings == null || embeddings.size() < 2) {
            return false;
        }

        double similarity = CosineSimilarityCalculator.calculate(embeddings.get(0), embeddings.get(1));
        log.info("의미 유사도 검증 결과: '{}' vs '{}' = {}", userQuestion, representativeText, similarity);

        // ✨ 4. 2차 검증 결과(유사도 점수, 판단)를 노트에 기록합니다.
        traceBuilder.similarityScore(similarity);
        boolean isSimilar = similarity >= SIMILARITY_SCORE_THRESHOLD;
        traceBuilder.safetyNetJudgement(isSimilar ? "유사도 검증 통과" : "RAG 호출 결정");

        return isSimilar;
    }

    private void handleLowConfidenceIntent(MessageRequest request, String originalIntentName, float originalIntentScore, AnalysisTrace.Builder traceBuilder) {
        log.info("Dialogflow 점수가 낮거나(score: {}), 의미 유사도 검증 실패. Python AI 서버 호출 시작.", originalIntentScore);
        List<String> allIntents = List.of(
            // 그룹 A: RAG가 해결할 복잡한 의도
            "환불절차문의_VIP혜택",
            "환불금액문의_쿠폰사용",
            "환불혜택문의_등급변경",
            "콜라보상품_중복할인_문의",
            "이벤트_중복할인_문의",
            "콜라보상품_환불_문의",
            // 그룹 B: Dialogflow가 기본적으로 학습할 단순 의도
            "배송_조회",
            "일반_환불_문의",
            "일반_교환_문의",
            "주문_수정",
            "주문_취소"
        );
        AiServerResponse aiResponse = aiServerService.classifyIntent(request.getMessage(), allIntents);
        if (aiResponse != null) {
            // ✨ 5. RAG의 최종 분석 결과를 노트에 기록합니다.
            traceBuilder.ragFinalIntent(aiResponse.getFinal_intent());
            traceBuilder.finalEngine(aiResponse.getEngine());
            
            String finalIntent = aiResponse.getFinal_intent();
            String fixedResponse = intentResponseService.getResponseForIntent(finalIntent);
            ChatMessage.AnalysisInfo analysisInfo = ChatMessage.AnalysisInfo.builder()
                    .engine(aiResponse.getEngine()).intentName(finalIntent)
                    .originalIntentName(originalIntentName).originalIntentScore(originalIntentScore)
                    .build();
            // ✨ 6. 완성된 '생각 노트'와 함께 최종 결과를 저장합니다.
            chatService.saveMessage(
                    request.getSessionId(), request.getUserId(), "bot",
                    fixedResponse, request.getLanguageCode(), analysisInfo, traceBuilder.build() // trace 추가
            );
        } else {
            log.warn("Python AI 서버 호출에 실패했습니다. Dialogflow의 응답으로 대체합니다.");
            handleHighConfidenceIntent(request, originalIntentName, originalIntentScore, "죄송해요, 지금은 AI 서버에 문제가 있어 답변을 드릴 수 없어요.", traceBuilder.build());
        }
    }
    private void handleHighConfidenceIntent(MessageRequest request, String intentName, float intentScore, String reply, AnalysisTrace trace) {
        ChatMessage.AnalysisInfo analysisInfo = ChatMessage.AnalysisInfo.builder()
                .engine("dialogflow").intentName(intentName)
                .originalIntentName(intentName).originalIntentScore(intentScore)
                .build();
        chatService.saveMessage(
                request.getSessionId(), request.getUserId(), "bot",
                reply, request.getLanguageCode(), analysisInfo, trace // trace 추가
        );
    }
<<<<<<< HEAD

    /**
     * 쇼핑 의도인지 판단합니다.
     * 
     * @param intentName Dialogflow에서 감지된 의도명
     * @param userMessage 사용자 메시지
     * @return 쇼핑 의도 여부
     */
    private boolean isShoppingIntent(String intentName, String userMessage) {
        // 의도명 기반 판단
        String lowerIntent = intentName.toLowerCase();
        boolean intentBased = lowerIntent.contains("shopping") || lowerIntent.contains("product") || lowerIntent.contains("search") ||lowerIntent.contains("buy") ||lowerIntent.contains("purchase");
        
        // 메시지 내용 기반 판단 (상품명, 브랜드명 등이 포함된 경우)
        String lowerMessage = userMessage.toLowerCase();
        boolean messageBased = lowerMessage.matches(".*(나이키|아디다스|나이키|아디다스|신발|운동화|청바지|가방|옷|상품|제품|브랜드).*") || lowerMessage.matches(".*(찾아|검색|보여|추천|어떤|좋은).*");
        
        log.info("쇼핑 의도 판단 - 의도: {}, 메시지: {}, 의도기반: {}, 메시지기반: {}", 
                intentName, userMessage, intentBased, messageBased);
        
        return intentBased || messageBased;
    }

    /**
     * 쇼핑 의도 처리 - 상품 검색 및 결과 저장
     * 
     * @param request 사용자 요청
     * @param intentName 의도명
     * @param intentScore 의도 점수
     * @param traceBuilder 분석 추적 빌더
     */
    private void handleShoppingIntent(MessageRequest request, String intentName, float intentScore, AnalysisTrace.Builder traceBuilder) {
        try {
            log.info("쇼핑 의도 처리 시작 - 메시지: {}", request.getMessage());
            
            // ✨ 1. SimpleShoppingService를 통해 상품 검색
            ShoppingMessageResponse shoppingResponse = simpleShoppingService.searchProducts(request.getMessage());
            
            // ✨ 2. 쇼핑 분석 정보 생성
            ChatMessage.AnalysisInfo analysisInfo = ChatMessage.AnalysisInfo.builder()
                    .engine("shopping-service")
                    .intentName(intentName)
                    .originalIntentName(intentName)
                    .originalIntentScore(intentScore)
                    .build();
            
            // ✨ 3. 쇼핑 데이터 생성
            ChatMessage.ShoppingData shoppingData = ChatMessage.ShoppingData.builder()
                    .intentType("product_search")
                    .originalQuery(request.getMessage())
                    .totalResults(shoppingResponse.getProducts() != null ? shoppingResponse.getProducts().size() : 0)
                    .searchTime(java.time.LocalDateTime.now().toString())
                    .confidence("high")
                    .products(convertToProductInfo(shoppingResponse))
                    .build();
            
            // ✨ 4. 분석 추적 정보 업데이트
            traceBuilder.shoppingIntent(intentName)
                       .shoppingResults(shoppingResponse.getProducts() != null ? shoppingResponse.getProducts().size() : 0)
                       .finalEngine("shopping-service");
            
            // ✨ 5. 결과를 DB에 저장
            chatService.saveMessage(
                    request.getSessionId(), 
                    request.getUserId(), 
                    "bot", 
                    shoppingResponse.getResponse(), 
                    request.getLanguageCode(), 
                    analysisInfo, 
                    traceBuilder.build(),
                    shoppingData // 쇼핑 데이터 추가
            );
            
            log.info("쇼핑 의도 처리 완료 - 검색된 상품 수: {}", 
                    shoppingResponse.getProducts() != null ? shoppingResponse.getProducts().size() : 0);
            
        } catch (Exception e) {
            log.error("쇼핑 의도 처리 중 오류 발생: {}", request.getMessage(), e);
            
            // 오류 발생 시 기본 응답 저장
            ChatMessage.AnalysisInfo errorAnalysisInfo = ChatMessage.AnalysisInfo.builder()
                    .engine("shopping-service-error")
                    .intentName(intentName)
                    .originalIntentName(intentName)
                    .originalIntentScore(intentScore)
                    .build();
            
            chatService.saveMessage(
                    request.getSessionId(), 
                    request.getUserId(), 
                    "bot", 
                    "죄송합니다. 상품 검색 중 오류가 발생했습니다. 다시 시도해주세요.", 
                    request.getLanguageCode(), 
                    errorAnalysisInfo, 
                    traceBuilder.build()
            );
        }
    }

    /**
     * ShoppingMessageResponse를 ChatMessage.ProductInfo 리스트로 변환
     * 
     * @param shoppingResponse 쇼핑 응답
     * @return ProductInfo 리스트
     */
    private List<ChatMessage.ProductInfo> convertToProductInfo(ShoppingMessageResponse shoppingResponse) {
        if (shoppingResponse.getProducts() == null) {
            return List.of();
        }
        
        return shoppingResponse.getProducts().stream()
                .map(product -> ChatMessage.ProductInfo.builder()
                        .id(product.getId())
                        .title(product.getTitle())
                        .image(product.getImage())
                        .link(product.getLink())
                        .lprice(product.getLprice())
                        .hprice(product.getHprice())
                        .mallName(product.getMallName())
                        .brand(product.getBrand())
                        .category1(product.getCategory1())
                        .category2(product.getCategory2())
                        .productType(product.getProductType())
                        .maker(product.getMaker())
                        .searchCount(product.getSearchCount())
                        .lastSearchedAt(product.getLastSearchedAt())
                        .priceFormatted(product.getPriceFormatted())
                        .discountRate(product.getDiscountRate())
                        .isRecommended(product.isRecommended())
                        .recommendationReason(product.getRecommendationReason())
                        .build())
                .toList();
    }
=======
>>>>>>> f5217b1b3c80a64be7ec5f31fe30a5203797c244
}