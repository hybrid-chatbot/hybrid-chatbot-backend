package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    private String id;

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}