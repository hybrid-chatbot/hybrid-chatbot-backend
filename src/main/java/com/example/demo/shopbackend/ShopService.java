package com.example.demo.shopbackend;

import com.example.demo.dto.*;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShopService {
    @Autowired private UserRepository userRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private OrderRepository orderRepo;

    public User register(UserDto dto) {
        if (userRepo.findByEmail(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        return userRepo.save(User.builder()
                .email(dto.getUsername())
                .password(dto.getPassword()) // 실제 서비스에서는 암호화 필요
                .build());
    }

    public User login(UserDto dto) {
        return userRepo.findByEmail(dto.getUsername())
                .filter(u -> u.getPassword().equals(dto.getPassword()))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
    }

    public Product addProduct(ProductDto dto) {
        return productRepo.save(Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .build());
    }

    public Product updateProduct(String id, ProductDto dto) {
        Product p = productRepo.findById(id).orElseThrow(() -> new RuntimeException("Not Found"));
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        return productRepo.save(p);
    }

    public Order placeOrder(OrderDto dto) {
        List<OrderItem> orderItems = dto.getOrderItems().stream()
                .map(itemDto -> OrderItem.builder()
                        .productId(itemDto.getProductId())
                        .productName(itemDto.getProductName())
                        .quantity(itemDto.getQuantity())
                        .unitPrice(itemDto.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        return orderRepo.save(Order.builder()
                .userId(dto.getUserId())
                .orderItems(orderItems)
                .build());
    }

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }
} 