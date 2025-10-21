package com.example.demo.service;

import com.example.demo.dto.ShoppingRequest;
import com.example.demo.dto.ShoppingMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingService {

    private final SimpleShoppingService simpleShoppingService;

    public ShoppingMessageResponse searchProducts(ShoppingRequest request) {
        log.info("쇼핑 상품 검색 시작 - 쿼리: {}", request.getQuery());
        
        // SimpleShoppingService를 통해 상품 검색
        return simpleShoppingService.searchProducts(request.getQuery());
    }
}
