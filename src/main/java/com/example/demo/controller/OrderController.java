package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;

    @PostMapping
    public Order placeOrder(@RequestBody Order order) {
        order.setOrderedAt(LocalDateTime.now());
        order.setStatus("paid");
        return orderRepository.save(order);
    }

    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(@PathVariable String userId) {
        return orderRepository.findByUserId(userId);
    }
}