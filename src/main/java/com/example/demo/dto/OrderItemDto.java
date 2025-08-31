package com.example.demo.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}