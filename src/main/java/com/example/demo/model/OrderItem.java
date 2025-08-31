package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}