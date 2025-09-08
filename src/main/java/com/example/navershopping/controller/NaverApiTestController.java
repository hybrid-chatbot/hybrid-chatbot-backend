package com.example.navershopping.controller;

import com.example.navershopping.service.NaverShoppingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test/naver")
public class NaverApiTestController {

    @Autowired
    private NaverShoppingService naverShoppingService;
    
    @Value("${naver.shopping.client.id}")
    private String clientId;
    
    @Value("${naver.shopping.client.secret}")
    private String clientSecret;

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("clientId", clientId != null ? clientId.substring(0, Math.min(clientId.length(), 10)) + "..." : "null");
        config.put("clientSecret", clientSecret != null ? clientSecret.substring(0, Math.min(clientSecret.length(), 5)) + "..." : null);
        config.put("status", "configured");
        return ResponseEntity.ok(config);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("service", "Naver Shopping API");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }

    @GetMapping("/search/test")
    public ResponseEntity<Map<String, Object>> testSearch(
            @RequestParam(defaultValue = "노트북") String query,
            @RequestParam(defaultValue = "3") int display) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            var response = naverShoppingService.searchProducts(query, display, 1);
            
            if (response != null) {
                result.put("success", true);
                result.put("query", query);
                result.put("totalResults", response.getTotal());
                result.put("displayCount", display);
                result.put("timestamp", java.time.LocalDateTime.now().toString());
                
                if (response.getItems() != null && !response.getItems().isEmpty()) {
                    result.put("firstItem", Map.of(
                        "title", response.getItems().get(0).getTitle(),
                        "price", response.getItems().get(0).getLprice(),
                        "mall", response.getItems().get(0).getMallName()
                    ));
                }
                
                result.put("message", "API 호출 성공");
            } else {
                result.put("success", false);
                result.put("message", "응답이 null입니다");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            result.put("message", "API 호출 중 오류 발생");
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/detailed")
    public ResponseEntity<Map<String, Object>> detailedSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int display) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            var response = naverShoppingService.searchProducts(query, display, 1);
            
            if (response != null && response.getItems() != null) {
                result.put("success", true);
                result.put("query", query);
                result.put("totalResults", response.getTotal());
                result.put("displayCount", display);
                result.put("timestamp", java.time.LocalDateTime.now().toString());
                
                var items = response.getItems();
                result.put("items", items.stream()
                    .map(item -> Map.of(
                        "title", item.getTitle(),
                        "price", item.getLprice(),
                        "highPrice", item.getHprice(),
                        "mall", item.getMallName(),
                        "link", item.getLink(),
                        "image", item.getImage()
                    ))
                    .toList());
                
                result.put("message", "상세 검색 결과");
            } else {
                result.put("success", false);
                result.put("message", "검색 결과가 없습니다");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            result.put("message", "상세 검색 중 오류 발생");
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/compare")
    public ResponseEntity<Map<String, Object>> compareSearch(
            @RequestParam String query1,
            @RequestParam String query2,
            @RequestParam(defaultValue = "3") int display) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            var response1 = naverShoppingService.searchProducts(query1, display, 1);
            var response2 = naverShoppingService.searchProducts(query2, display, 1);
            
            result.put("success", true);
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            // 첫 번째 검색 결과
            if (response1 != null) {
                result.put("query1", Map.of(
                    "query", query1,
                    "totalResults", response1.getTotal(),
                    "firstItem", response1.getItems() != null && !response1.getItems().isEmpty() ? 
                        Map.of("title", response1.getItems().get(0).getTitle(), "price", response1.getItems().get(0).getLprice()) : null
                ));
            }
            
            // 두 번째 검색 결과
            if (response2 != null) {
                result.put("query2", Map.of(
                    "query", query2,
                    "totalResults", response2.getTotal(),
                    "firstItem", response2.getItems() != null && !response2.getItems().isEmpty() ? 
                        Map.of("title", response2.getItems().get(0).getTitle(), "price", response2.getItems().get(0).getLprice()) : null
                ));
            }
            
            result.put("message", "비교 검색 완료");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "비교 검색 중 오류 발생");
        }
        
        return ResponseEntity.ok(result);
    }
}