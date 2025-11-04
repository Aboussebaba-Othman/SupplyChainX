package com.supplychainx.production.entity;

import com.supplychainx.common.entity.BaseEntity;
import com.supplychainx.production.enums.Priority;
import com.supplychainx.production.enums.ProductionOrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "production_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ProductionOrder extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Le numéro d'ordre est obligatoire")
    @Size(max = 50, message = "Le numéro d'ordre ne peut pas dépasser 50 caractères")
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Le produit est obligatoire")
    private Product product;

    @Column(name = "quantity", nullable = false)
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au minimum 1")
    private Integer quantity;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le statut est obligatoire")
    private ProductionOrderStatus status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "estimated_time")
    @Min(value = 0, message = "Le temps estimé ne peut pas être négatif")
    private Integer estimatedTime;

    @Column(name = "priority", length = 20)
    @Enumerated(EnumType.STRING)
    private Priority priority;

    public boolean canBeStarted() {
        return status == ProductionOrderStatus.EN_ATTENTE;
    }

    public boolean canBeCancelled() {
        return status != ProductionOrderStatus.TERMINE && status != ProductionOrderStatus.ANNULE;
    }

    public Integer calculateEstimatedTime() {
        if (product != null && product.getProductionTime() != null && quantity != null) {
            return product.getProductionTime() * quantity;
        }
        return 0;
    }

    public boolean checkMaterialsAvailability() {
        return true;
    }

    public void consumeMaterials() {
    }

    public boolean isDelayed() {
        if (status == null) return false;
        if (status != ProductionOrderStatus.EN_PRODUCTION) return false;
        if (endDate == null) return false;
        return endDate.isBefore(java.time.LocalDate.now());
    }
}
