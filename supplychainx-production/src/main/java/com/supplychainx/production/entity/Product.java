package com.supplychainx.production.entity;

import com.supplychainx.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Le code produit est obligatoire")
    @Size(max = 50, message = "Le code ne peut pas dépasser 50 caractères")
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 50)
    @Size(max = 50, message = "La catégorie ne peut pas dépasser 50 caractères")
    private String category;

    @Column(name = "stock", nullable = false)
    @NotNull(message = "Le stock est obligatoire")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stock;

    @Column(name = "stock_min", nullable = false)
    @NotNull(message = "Le stock minimum est obligatoire")
    @Min(value = 0, message = "Le stock minimum ne peut pas être négatif")
    private Integer stockMin;

    @Column(name = "unit", nullable = false, length = 20)
    @NotBlank(message = "L'unité de mesure est obligatoire")
    @Size(max = 20, message = "L'unité ne peut pas dépasser 20 caractères")
    private String unit;

    @Column(name = "unit_price")
    @DecimalMin(value = "0.0", message = "Le prix unitaire doit être positif")
    private Double unitPrice;

    @Column(name = "production_cost")
    @DecimalMin(value = "0.0", message = "Le coût de production doit être positif")
    private Double productionCost;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BillOfMaterial> billsOfMaterial = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductionOrder> productionOrders = new ArrayList<>();

    // Méthode utilitaire pour vérifier si le stock est faible
    public boolean isLowStock() {
        return stock < stockMin;
    }
}
