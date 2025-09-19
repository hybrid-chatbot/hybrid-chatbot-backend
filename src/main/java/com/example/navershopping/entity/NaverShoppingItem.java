package com.example.navershopping.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "naver_shopping_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NaverShoppingItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", unique = true, nullable = false)
    private String productId;
    
    @Column(name = "title", length = 1000)
    private String title;
    
    @Column(name = "link", length = 2000)
    private String link;
    
    @Column(name = "image", length = 2000)
    private String image;
    
    @Column(name = "lprice")
    private Integer lprice;
    
    @Column(name = "hprice")
    private Integer hprice;
    
    @Column(name = "mall_name", length = 500)
    private String mallName;
    
    @Column(name = "product_type", length = 100)
    private String productType;
    
    @Column(name = "brand", length = 200)
    private String brand;
    
    @Column(name = "maker", length = 200)
    private String maker;
    
    @Column(name = "category1", length = 100)
    private String category1;
    
    @Column(name = "category2", length = 100)
    private String category2;
    
    @Column(name = "category3", length = 100)
    private String category3;
    
    @Column(name = "category4", length = 100)
    private String category4;
    
    @Column(name = "search_query", length = 500)
    private String searchQuery;
    
    @Column(name = "last_searched_at")
    private LocalDateTime lastSearchedAt;
    
    @Column(name = "search_count")
    private Integer searchCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}