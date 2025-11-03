package com.supplychainx.delivery.entity;

import com.supplychainx.common.entity.BaseEntity;
import com.supplychainx.production.entity.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "delivery_order_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class DeliveryOrderLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_order_id", nullable = false)
    @NotNull(message = "La commande de livraison est obligatoire")
    private DeliveryOrder deliveryOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Le produit est obligatoire")
    private Product product;

    @Column(name = "quantity", nullable = false)
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au minimum 1")
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", message = "Le prix unitaire ne peut pas être négatif")
    private Double unitPrice;

    public Double getLineTotal() {
        if (quantity != null && unitPrice != null) {
            return quantity * unitPrice;
        }
        return 0.0;
    }
}
