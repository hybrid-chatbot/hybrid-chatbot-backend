package com.example.demo.integration;

import com.example.demo.controller.MessageController;
import com.example.demo.dto.MessageRequest;
import com.example.demo.kafka.MessageConsumer;
import com.example.demo.model.ChatMessage;
import com.example.demo.service.ChatService;
import com.example.demo.service.DialogflowService;
import com.example.navershopping.entity.NaverShoppingItem;
import com.example.navershopping.service.NaverShoppingService;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.Intent;
import com.google.cloud.dialogflow.v2.QueryResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class FullFlowChatUiIntegrationTest {

    @Autowired
    private MessageConsumer messageConsumer;

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessageController messageController;

    @MockBean
    private DialogflowService dialogflowService;

    @MockBean
    private NaverShoppingService naverShoppingService;

/**
 * E2E 시뮬레이션 테스트
 * 사용자 메시지 -> Dialogflow 의도 -> DB 검색 -> 저장 -> /result 응답까지 검증
 */
@Test
void endToEnd_shoppingIntent_productsSaved_andResultEndpointReturnsForUi() {
        // Given
        String sessionId = "it-session-" + System.currentTimeMillis();
        String userId = "user-1";
        String userMessage = "아이폰 케이스 추천해줘";

        // Mock Dialogflow response
        QueryResult queryResult = QueryResult.newBuilder()
                .setFulfillmentText("아이폰 케이스 추천 결과입니다.")
                .setIntent(Intent.newBuilder().setDisplayName("product_recommendation").build())
                .setIntentDetectionConfidence(0.92f)
                .build();
        DetectIntentResponse dfResponse = DetectIntentResponse.newBuilder()
                .setQueryResult(queryResult)
                .build();
        when(dialogflowService.detectIntent(eq(sessionId), eq(userMessage), eq("ko")))
                .thenReturn(dfResponse);

        // Mock saved products in DB
        List<NaverShoppingItem> items = List.of(
                NaverShoppingItem.builder()
                        .id(1L)
                        .productId("iphone-case-1")
                        .title("아이폰 15 Pro 투명 케이스")
                        .link("https://test.com/iphone15pro-clear-case")
                        .image("https://test.com/iphone15pro-clear-case.jpg")
                        .lprice(25000)
                        .hprice(35000)
                        .mallName("애플스토어")
                        .brand("Apple")
                        .category1("디지털/가전")
                        .category2("휴대폰")
                        .category3("케이스")
                        .searchQuery("아이폰 케이스 추천해줘")
                        .lastSearchedAt(LocalDateTime.now())
                        .searchCount(10)
                        .build(),
                NaverShoppingItem.builder()
                        .id(2L)
                        .productId("iphone-case-2")
                        .title("아이폰 14 실리콘 케이스")
                        .link("https://test.com/iphone14-silicone-case")
                        .image("https://test.com/iphone14-silicone-case.jpg")
                        .lprice(20000)
                        .hprice(30000)
                        .mallName("삼성스토어")
                        .brand("Samsung")
                        .category1("디지털/가전")
                        .category2("휴대폰")
                        .category3("케이스")
                        .searchQuery("아이폰 케이스 추천해줘")
                        .lastSearchedAt(LocalDateTime.now())
                        .searchCount(7)
                        .build()
        );
        when(naverShoppingService.getSavedProductsByQuery(userMessage)).thenReturn(items);

        // When: simulate message flow via consumer
        MessageRequest request = MessageRequest.builder()
                .sessionId(sessionId)
                .userId(userId)
                .message(userMessage)
                .languageCode("ko")
                .build();
        messageConsumer.consume(request);

        // Then: DB has bot message with shopping data
        Optional<ChatMessage> latest = chatService.findLatestMessageBySessionId(sessionId);
        assertTrue(latest.isPresent());
        ChatMessage saved = latest.get();
        assertEquals("bot", saved.getSender());
        assertNotNull(saved.getShoppingData());
        assertNotNull(saved.getShoppingData().getProducts());
        assertTrue(saved.getShoppingData().getProducts().size() >= 1);

        // And: /api/messages/result returns 200 with data (UI가 폴링하는 엔드포인트)
        ResponseEntity<ChatMessage> resultResp = messageController.getMessageResult(sessionId);
        assertEquals(200, resultResp.getStatusCode().value());
        ChatMessage body = resultResp.getBody();
        assertNotNull(body);
        assertNotNull(body.getShoppingData());
        assertFalse(body.getShoppingData().getProducts().isEmpty());
        // UI에 필요한 필드 몇 개 샘플 체크
        ChatMessage.ProductInfo p = body.getShoppingData().getProducts().get(0);
        assertNotNull(p.getTitle());
        assertNotNull(p.getLink());
        assertNotNull(p.getImage());
    }
}
