package com.example.navershopping.controller;

import com.example.navershopping.service.NaverShoppingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private NaverShoppingService naverShoppingService;

    @GetMapping("/naver-shopping")
    public String testNaverShopping(@RequestParam(defaultValue = "노트북") String query) {
        try {
            var response = naverShoppingService.searchProducts(query, 3, 1);
            if (response != null && response.getItems() != null) {
                StringBuilder result = new StringBuilder();
                result.append("검색 결과: ").append(response.getTotal()).append("개\n\n");
                
                for (var item : response.getItems()) {
                    result.append("제품명: ").append(item.getTitle()).append("\n");
                    result.append("가격: ").append(item.getLprice()).append("원\n");
                    result.append("쇼핑몰: ").append(item.getMallName()).append("\n");
                    result.append("링크: ").append(item.getLink()).append("\n\n");
                }
                return result.toString();
            } else {
                return "검색 결과가 없습니다.";
            }
        } catch (Exception e) {
            return "오류 발생: " + e.getMessage();
        }
    }

    @GetMapping("/health")
    public String health() {
        return "애플리케이션이 정상적으로 실행 중입니다.";
    }
}