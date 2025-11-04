package com.supplychainx.delivery.repository;

import com.supplychainx.delivery.entity.DeliveryOrder;
import com.supplychainx.delivery.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, Long> {

    Optional<DeliveryOrder> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

        Page<DeliveryOrder> findByStatus(OrderStatus status, Pageable pageable);

    Page<DeliveryOrder> findByCustomerId(Long customerId, Pageable pageable);

    @Query("SELECT d FROM DeliveryOrder d WHERE d.orderDate BETWEEN :startDate AND :endDate")
    Page<DeliveryOrder> findByOrderDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT d FROM DeliveryOrder d WHERE d.status = :status AND d.expectedDeliveryDate < :date")
    List<DeliveryOrder> findDelayedOrders(
            @Param("status") OrderStatus status,
            @Param("date") LocalDate date
    );

        @Query("SELECT d FROM DeliveryOrder d WHERE d.expectedDeliveryDate = :date AND d.status IN ('EN_PREPARATION', 'EN_ROUTE')")
        List<DeliveryOrder> findOrdersForDeliveryDate(@Param("date") LocalDate date);
}
