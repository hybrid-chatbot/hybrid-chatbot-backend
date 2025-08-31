package com.example.demo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private String userId;
    private List<OrderItemDto> orderItems;
    private BigDecimal totalAmount;
    private String shippingAddressId;
    private String paymentMethod;
}