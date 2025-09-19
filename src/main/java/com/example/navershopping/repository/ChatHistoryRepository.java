package com.example.navershopping.repository;

import com.example.navershopping.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findByUserIdOrderByTimestampDesc(String userId);
    List<ChatHistory> findBySessionIdOrderByTimestampDesc(String sessionId);
    void deleteByUserId(String userId);
} 