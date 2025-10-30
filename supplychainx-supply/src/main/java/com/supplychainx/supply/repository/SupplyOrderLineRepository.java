package com.supplychainx.supply.repository;

import com.supplychainx.supply.entity.SupplyOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplyOrderLineRepository extends JpaRepository<SupplyOrderLine, Long> {

    // Recherche toutes les lignes d'une commande
    List<SupplyOrderLine> findBySupplyOrderId(Long supplyOrderId);

    // Recherche toutes les lignes contenant une matière première
    List<SupplyOrderLine> findByMaterialId(Long materialId);

    // Recherche des lignes par commande et matière
    List<SupplyOrderLine> findBySupplyOrderIdAndMaterialId(Long supplyOrderId, Long materialId);

    // Calcule la quantité totale commandée pour une matière première
    @Query("SELECT SUM(sol.quantity) FROM SupplyOrderLine sol " +
           "WHERE sol.material.id = :materialId " +
           "AND sol.supplyOrder.status IN ('EN_ATTENTE', 'EN_COURS')")
    Integer sumQuantityByMaterialInActiveOrders(@Param("materialId") Long materialId);

    // Calcule le montant total des lignes d'une commande
    @Query("SELECT SUM(sol.quantity * sol.unitPrice) FROM SupplyOrderLine sol " +
           "WHERE sol.supplyOrder.id = :orderId")
    Double sumTotalAmountByOrder(@Param("orderId") Long orderId);

    // Vérifie si une matière est utilisée dans des lignes de commande actives
    @Query("SELECT CASE WHEN COUNT(sol) > 0 THEN true ELSE false END " +
           "FROM SupplyOrderLine sol " +
           "WHERE sol.material.id = :materialId " +
           "AND sol.supplyOrder.status IN ('EN_ATTENTE', 'EN_COURS')")
    boolean materialHasActiveOrderLines(@Param("materialId") Long materialId);

    // Supprime toutes les lignes d'une commande
    void deleteBySupplyOrderId(Long supplyOrderId);
}
