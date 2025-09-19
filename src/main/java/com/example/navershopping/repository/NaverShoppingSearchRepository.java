package com.example.navershopping.repository;

import com.example.navershopping.entity.NaverShoppingSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NaverShoppingSearchRepository extends JpaRepository<NaverShoppingSearch, Long> {
    
    List<NaverShoppingSearch> findByQuery(String query);
    
    List<NaverShoppingSearch> findByQueryContaining(String query);
    
    @Query("SELECT n FROM NaverShoppingSearch n WHERE n.createdAt >= :startDate AND n.createdAt <= :endDate")
    List<NaverShoppingSearch> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT n FROM NaverShoppingSearch n ORDER BY n.createdAt DESC")
    List<NaverShoppingSearch> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT DISTINCT n.query FROM NaverShoppingSearch n")
    List<String> findDistinctQueries();
    
    @Query("SELECT COUNT(n) FROM NaverShoppingSearch n WHERE n.query = :query")
    Long countByQuery(@Param("query") String query);
}