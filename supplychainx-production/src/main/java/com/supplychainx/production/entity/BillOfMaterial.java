package com.supplychainx.production.entity;

import com.supplychainx.common.entity.BaseEntity;
import com.supplychainx.supply.entity.RawMaterial;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "bills_of_material")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class BillOfMaterial extends BaseEntity {

    // Relation ManyToOne avec Product (un produit peut avoir plusieurs lignes de nomenclature)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Le produit est obligatoire")
    private Product product;

    // Relation ManyToOne avec RawMaterial (une matière première peut être utilisée dans plusieurs produits)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_material_id", nullable = false)
    @NotNull(message = "La matière première est obligatoire")
    private RawMaterial rawMaterial;

    @Column(name = "quantity", nullable = false)
    @NotNull(message = "La quantité est obligatoire")
    @DecimalMin(value = "0.01", message = "La quantité doit être supérieure à 0")
    private Double quantity;

    @Column(name = "unit", nullable = false, length = 20)
    @NotBlank(message = "L'unité de mesure est obligatoire")
    @Size(max = 20, message = "L'unité ne peut pas dépasser 20 caractères")
    private String unit;

    // Méthode utilitaire pour calculer le coût de cette ligne de nomenclature
    public Double calculateLineCost() {
        if (rawMaterial != null && rawMaterial.getUnitPrice() != null && quantity != null) {
            return rawMaterial.getUnitPrice() * quantity;
        }
        return 0.0;
    }
}
