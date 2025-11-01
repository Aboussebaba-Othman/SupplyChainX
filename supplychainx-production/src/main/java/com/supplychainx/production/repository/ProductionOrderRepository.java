package com.supplychainx.production.repository;

import com.supplychainx.production.entity.ProductionOrder;
import com.supplychainx.production.enums.ProductionOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long>, JpaSpecificationExecutor<ProductionOrder> {

    Optional<ProductionOrder> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    Page<ProductionOrder> findByStatus(ProductionOrderStatus status, Pageable pageable);

    @Query("SELECT po FROM ProductionOrder po WHERE po.plannedDate BETWEEN :start AND :end")
    Page<ProductionOrder> findByStartDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end, Pageable pageable);

    // Find orders in a given status whose end date is before `now` (delayed)
    List<ProductionOrder> findByStatusAndEndDateBefore(ProductionOrderStatus status, LocalDate now);

    Page<ProductionOrder> findByProductId(Long productId, Pageable pageable);
}
