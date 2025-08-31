package com.example.demo.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
}