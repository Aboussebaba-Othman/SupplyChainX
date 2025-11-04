package com.supplychainx.production.repository;

import com.supplychainx.production.entity.BillOfMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillOfMaterialRepository extends JpaRepository<BillOfMaterial, Long>, JpaSpecificationExecutor<BillOfMaterial> {

    List<BillOfMaterial> findByProductId(Long productId);

    @Query("SELECT b FROM BillOfMaterial b WHERE b.rawMaterial.id = :rawMaterialId")
    List<BillOfMaterial> findByRawMaterialId(@Param("rawMaterialId") Long rawMaterialId);

    @Query("SELECT SUM(b.quantity) FROM BillOfMaterial b WHERE b.product.id = :productId")
    Double sumQuantityByProductId(@Param("productId") Long productId);
}
