package com.example.navershopping.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "naver_shopping_searches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NaverShoppingSearch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "query", length = 500, nullable = false)
    private String query;
    
    @Column(name = "total_results")
    private Integer totalResults;
    
    @Column(name = "display_count")
    private Integer displayCount;
    
    @Column(name = "start_index")
    private Integer startIndex;
    
    @Column(name = "last_build_date", length = 100)
    private String lastBuildDate;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}