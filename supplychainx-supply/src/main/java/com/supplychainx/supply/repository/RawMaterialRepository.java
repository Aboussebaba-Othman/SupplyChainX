package com.supplychainx.supply.repository;

import com.supplychainx.supply.entity.RawMaterial;
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
public interface RawMaterialRepository extends JpaRepository<RawMaterial, Long>, JpaSpecificationExecutor<RawMaterial> {

    // Recherche une matière première par son code
    Optional<RawMaterial> findByCode(String code);

    // Recherche des matières premières par nom (contient)
    Page<RawMaterial> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Recherche des matières premières par catégorie
    Page<RawMaterial> findByCategory(String category, Pageable pageable);

    // Recherche des matières premières dont le stock est inférieur au stock minimum (stock critique)
    @Query("SELECT rm FROM RawMaterial rm WHERE rm.stock < rm.stockMin")
    List<RawMaterial> findLowStockMaterials();

    // Recherche des matières premières dont le stock est inférieur au stock minimum avec pagination
    @Query("SELECT rm FROM RawMaterial rm WHERE rm.stock < rm.stockMin")
    Page<RawMaterial> findLowStockMaterials(Pageable pageable);

    // Recherche des matières premières avec un stock inférieur à une valeur donnée
    List<RawMaterial> findByStockLessThan(Integer stock);

    // Vérifie si une matière première avec ce code existe déjà
    boolean existsByCode(String code);

    // Vérifie si une matière première est utilisée dans des lignes de commande
    @Query("SELECT CASE WHEN COUNT(sol) > 0 THEN true ELSE false END " +
           "FROM SupplyOrderLine sol " +
           "WHERE sol.material.id = :materialId")
    boolean isUsedInOrders(@Param("materialId") Long materialId);

    // Recherche des matières premières par fournisseur
    @Query("SELECT rm FROM RawMaterial rm JOIN rm.suppliers s WHERE s.id = :supplierId")
    List<RawMaterial> findBySupplier(@Param("supplierId") Long supplierId);

    // Compte le nombre de matières en stock critique
    @Query("SELECT COUNT(rm) FROM RawMaterial rm WHERE rm.stock < rm.stockMin")
    Long countLowStockMaterials();
}
