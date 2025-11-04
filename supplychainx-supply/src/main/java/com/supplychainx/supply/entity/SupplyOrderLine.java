package com.supplychainx.supply.entity;

import com.supplychainx.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;


@Entity
@Table(name = "supply_order_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SupplyOrderLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_order_id", nullable = false)
    @NotNull(message = "La commande est obligatoire")
    private SupplyOrder supplyOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    @NotNull(message = "La matière première est obligatoire")
    private RawMaterial material;

    @Column(name = "quantity", nullable = false)
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au minimum 1")
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix unitaire doit être supérieur à 0")
    private Double unitPrice;


    public Double getTotalPrice() {
        return quantity * unitPrice;
    }
}
