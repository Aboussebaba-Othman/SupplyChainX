package com.supplychainx.delivery.repository;

import com.supplychainx.delivery.entity.Delivery;
import com.supplychainx.delivery.enums.DeliveryStatus;
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
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByDeliveryNumber(String deliveryNumber);

    boolean existsByDeliveryNumber(String deliveryNumber);

    Optional<Delivery> findByDeliveryOrderId(Long deliveryOrderId);

    Page<Delivery> findByStatus(DeliveryStatus status, Pageable pageable);

    @Query("SELECT d FROM Delivery d WHERE d.deliveryDate = :date")
    List<Delivery> findByDeliveryDate(@Param("date") LocalDate date);

    @Query("SELECT d FROM Delivery d WHERE d.status = :status AND d.deliveryDate < :date")
    List<Delivery> findDelayedDeliveries(
            @Param("status") DeliveryStatus status,
            @Param("date") LocalDate date
    );

    @Query("SELECT d FROM Delivery d WHERE d.driver = :driver AND d.status IN ('PLANIFIEE', 'EN_COURS')")
    List<Delivery> findActiveDeliveriesByDriver(@Param("driver") String driver);

    @Query("SELECT d FROM Delivery d WHERE d.trackingNumber = :trackingNumber")
    Optional<Delivery> findByTrackingNumber(@Param("trackingNumber") String trackingNumber);
}
