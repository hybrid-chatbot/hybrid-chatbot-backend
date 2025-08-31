package com.example.demo.shopbackend;

import com.example.demo.dto.*;
import com.example.demo.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/shop")
public class ShopController {
    @Autowired private ShopService service;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserDto dto) {
        return ResponseEntity.ok(service.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody UserDto dto) {
        return ResponseEntity.ok(service.login(dto));
    }

    @PostMapping("/products")
    public ResponseEntity<Product> addProduct(@RequestBody ProductDto dto) {
        return ResponseEntity.ok(service.addProduct(dto));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody ProductDto dto) {
        return ResponseEntity.ok(service.updateProduct(id, dto));
    }

    @PostMapping("/orders")
    public ResponseEntity<Order> placeOrder(@RequestBody OrderDto dto) {
        return ResponseEntity.ok(service.placeOrder(dto));
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(service.getAllProducts());
    }
}

