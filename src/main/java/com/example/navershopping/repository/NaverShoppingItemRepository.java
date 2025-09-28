package com.example.navershopping.repository;

import com.example.navershopping.entity.NaverShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NaverShoppingItemRepository extends JpaRepository<NaverShoppingItem, Long> {
    
    Optional<NaverShoppingItem> findByProductId(String productId);
    
    List<NaverShoppingItem> findBySearchQuery(String searchQuery);
    
    List<NaverShoppingItem> findByMallName(String mallName);
    
    List<NaverShoppingItem> findByBrand(String brand);
    
    @Query("SELECT n FROM NaverShoppingItem n WHERE n.title LIKE %:keyword%")
    List<NaverShoppingItem> findByTitleContaining(@Param("keyword") String keyword);
    
    @Query("SELECT n FROM NaverShoppingItem n WHERE n.lprice BETWEEN :minPrice AND :maxPrice")
    List<NaverShoppingItem> findByPriceRange(@Param("minPrice") Integer minPrice, @Param("maxPrice") Integer maxPrice);
    
    @Query("SELECT n FROM NaverShoppingItem n WHERE n.category1 = :category1")
    List<NaverShoppingItem> findByCategory1(@Param("category1") String category1);
    
    @Query("SELECT n FROM NaverShoppingItem n WHERE n.category1 = :category1 AND n.category2 = :category2")
    List<NaverShoppingItem> findByCategory1AndCategory2(@Param("category1") String category1, @Param("category2") String category2);
    
    @Query("SELECT DISTINCT n.mallName FROM NaverShoppingItem n")
    List<String> findDistinctMallNames();
    
    @Query("SELECT DISTINCT n.brand FROM NaverShoppingItem n WHERE n.brand IS NOT NULL")
    List<String> findDistinctBrands();
    
    @Query("SELECT DISTINCT n.category1 FROM NaverShoppingItem n WHERE n.category1 IS NOT NULL")
    List<String> findDistinctCategory1();
    
    // 추가 쿼리 메서드들
    List<NaverShoppingItem> findByTitleContainingIgnoreCase(String title);
    
    List<NaverShoppingItem> findBySearchQueryOrderByLastSearchedAtDesc(String searchQuery);
    
    @Query("SELECT n FROM NaverShoppingItem n ORDER BY n.searchCount DESC LIMIT 10")
    List<NaverShoppingItem> findTop10ByOrderBySearchCountDesc();
    
    @Query("SELECT n FROM NaverShoppingItem n ORDER BY n.lastSearchedAt DESC LIMIT 10")
    List<NaverShoppingItem> findTop10ByOrderByLastSearchedAtDesc();
    
    @Query("SELECT n FROM NaverShoppingItem n WHERE n.category1 = :category1 AND n.id != :id")
    List<NaverShoppingItem> findByCategory1AndIdNot(@Param("category1") String category1, @Param("id") Long id);
    
    @Query("SELECT n FROM NaverShoppingItem n WHERE n.brand = :brand AND n.id != :id")
    List<NaverShoppingItem> findByBrandAndIdNot(@Param("brand") String brand, @Param("id") Long id);
}