package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    private String id;

    private String userId;
    private String name;
    private String address1;
    private String address2;
    private String zipCode;
    private Boolean isDefault;
    private LocalDateTime createdAt;
}