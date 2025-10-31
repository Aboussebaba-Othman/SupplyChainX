package com.supplychainx.supply.repository;

import com.supplychainx.supply.entity.SupplyOrder;
import com.supplychainx.supply.enums.SupplyOrderStatus;
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

// Repository pour lagestion des commandes d'approvisionnement
@Repository
public interface SupplyOrderRepository extends JpaRepository<SupplyOrder, Long>, JpaSpecificationExecutor<SupplyOrder> {

    // Recherche une commande par son numéro
    Optional<SupplyOrder> findByOrderNumber(String orderNumber);

    // Recherche des commandes par statut
    Page<SupplyOrder> findByStatus(SupplyOrderStatus status, Pageable pageable);

    // Recherche des commandes par fournisseur
    Page<SupplyOrder> findBySupplierId(Long supplierId, Pageable pageable);

    // Recherche des commandes par fournisseur et statut
    Page<SupplyOrder> findBySupplierIdAndStatus(Long supplierId, SupplyOrderStatus status, Pageable pageable);

    // Recherche des commandes entre deux dates
    @Query("SELECT so FROM SupplyOrder so WHERE so.orderDate BETWEEN :startDate AND :endDate")
    Page<SupplyOrder> findByOrderDateBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );

    // Recherche des commandes en retard (date de livraison prévue dépassée et statut EN_COURS)
    @Query("SELECT so FROM SupplyOrder so " +
           "WHERE so.status = 'EN_COURS' " +
           "AND so.expectedDeliveryDate < :currentDate")
    List<SupplyOrder> findDelayedOrders(@Param("currentDate") LocalDate currentDate);

    // Vérifie si un numéro de commande existe déjà
    boolean existsByOrderNumber(String orderNumber);

    // Compte le nombre de commandes actives pour un fournisseur
    @Query("SELECT COUNT(so) FROM SupplyOrder so " +
           "WHERE so.supplier.id = :supplierId " +
           "AND so.status IN ('EN_ATTENTE', 'EN_COURS')")
    Long countActiveOrdersBySupplier(@Param("supplierId") Long supplierId);

    // Calcule le montant total des commandes par statut
    @Query("SELECT SUM(so.totalAmount) FROM SupplyOrder so WHERE so.status = :status")
    Double sumTotalAmountByStatus(@Param("status") SupplyOrderStatus status);

    // Recherche les dernières commandes
    @Query("SELECT so FROM SupplyOrder so ORDER BY so.orderDate DESC")
    Page<SupplyOrder> findRecentOrders(Pageable pageable);
}
