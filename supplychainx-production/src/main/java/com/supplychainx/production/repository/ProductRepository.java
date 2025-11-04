package com.supplychainx.production.repository;

import com.supplychainx.production.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByCode(String code);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Product> findByCategory(String category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stock < p.stockMin")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.stock < p.stockMin")
    Page<Product> findLowStockProducts(Pageable pageable);

    boolean existsByCode(String code);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stock < p.stockMin")
    Long countLowStockProducts();

    List<Product> findByStockLessThan(Double stock);

    @Query("SELECT SUM(p.cost * p.stock) FROM Product p")
    Double calculateTotalInventoryValue();

    @Query("SELECT CASE WHEN COUNT(po) > 0 THEN true ELSE false END " +
           "FROM ProductionOrder po " +
           "WHERE po.product.id = :productId")
    boolean isUsedInProductionOrders(@Param("productId") Long productId);
}
