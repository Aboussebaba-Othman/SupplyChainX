package com.supplychainx.supply.repository;

import com.supplychainx.supply.entity.Supplier;
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
public interface SupplierRepository extends JpaRepository<Supplier, Long>, JpaSpecificationExecutor<Supplier> {

    // Recherche un fournisseur par son code
    Optional<Supplier> findByCode(String code);

    // Recherche des fournisseurs par nom (contient)
    Page<Supplier> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Recherche des fournisseurs par email
    Optional<Supplier> findByEmail(String email);

    // Recherche des fournisseurs avec une note supérieure ou égale
    List<Supplier> findByRatingGreaterThanEqual(Double rating);

    // Recherche des fournisseurs avec un délai inférieur ou égal
    List<Supplier> findByLeadTimeLessThanEqual(Integer leadTime);

    // Vérifie si un fournisseur avec ce code existe déjà
    boolean existsByCode(String code);

    // Vérifie si un fournisseur avec cet email existe déjà
    boolean existsByEmail(String email);

    // Vérifie si un fournisseur a des commandes actives (EN_ATTENTE ou EN_COURS)
    @Query("SELECT CASE WHEN COUNT(so) > 0 THEN true ELSE false END " +
           "FROM SupplyOrder so " +
           "WHERE so.supplier.id = :supplierId " +
           "AND so.status IN ('EN_ATTENTE', 'EN_COURS')")
    boolean hasActiveOrders(@Param("supplierId") Long supplierId);

    // Recherche des fournisseurs avec pagination et tri
    @Query("SELECT s FROM Supplier s ORDER BY s.rating DESC, s.name ASC")
    Page<Supplier> findAllOrderByRatingDesc(Pageable pageable);
}
