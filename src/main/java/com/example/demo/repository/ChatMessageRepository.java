
package com.example.demo.repository;

import com.example.demo.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    List<ChatMessage> findTop10BySessionIdOrderByTimestampDesc(String sessionId);
    List<ChatMessage> findTop10ByUserIdOrderByTimestampDesc(String userId);
    List<ChatMessage> findByUserId(String userId);
}
