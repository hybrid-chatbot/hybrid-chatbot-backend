package com.example.navershopping.controller;

import com.example.navershopping.dto.NaverShoppingResponse;
import com.example.navershopping.service.NaverShoppingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/naver-shopping")
public class NaverShoppingController {

    private final NaverShoppingService naverShoppingService;

    @Autowired
    public NaverShoppingController(NaverShoppingService naverShoppingService) {
        this.naverShoppingService = naverShoppingService;
    }

    @GetMapping("/search")
    public ResponseEntity<NaverShoppingResponse> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int display,
            @RequestParam(defaultValue = "1") int start) {
        
        NaverShoppingResponse response = naverShoppingService.searchProducts(query, display, start);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search/simple")
    public ResponseEntity<NaverShoppingResponse> searchProductsSimple(
            @RequestParam String query) {
        
        NaverShoppingResponse response = naverShoppingService.searchProducts(query);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}