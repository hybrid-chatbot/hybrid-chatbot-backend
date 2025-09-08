package com.example.navershopping.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/simple")
public class SimpleTestController {

    @GetMapping("/test")
    public Map<String, String> simpleTest() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "애플리케이션이 정상적으로 실행 중입니다!");
        result.put("timestamp", java.time.LocalDateTime.now().toString());
        return result;
    }

    @GetMapping("/naver-config")
    public Map<String, String> naverConfig() {
        Map<String, String> result = new HashMap<>();
        result.put("clientId", "MX1_wyfeo9eBuPfVTCSA");
        result.put("clientSecret", "MdiPTZAHE0");
        result.put("status", "configured");
        return result;
    }
}