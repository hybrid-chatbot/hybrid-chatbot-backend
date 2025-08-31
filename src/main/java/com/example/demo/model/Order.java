package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "orders")
@Setter
public class Order {
    @Id
    private String id;

    private String userId;
    private List<OrderItem> orderItems;
    private BigDecimal totalAmount;
    private String status;
    private String shippingAddressId;
    private LocalDateTime orderedAt;
    private String paymentMethod;
}