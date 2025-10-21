package com.example.navershopping.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "chat_history")
public class ChatHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false, length = 1000)
    private String userMessage;
    
    @Column(nullable = false, length = 1000)
    private String botResponse;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column
    private String sessionId;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
} 