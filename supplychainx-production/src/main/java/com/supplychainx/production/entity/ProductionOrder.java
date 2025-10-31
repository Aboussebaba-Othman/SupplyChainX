package com.supplychainx.production.entity;

import com.supplychainx.common.entity.BaseEntity;
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

    // Relation ManyToOne avec Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Le produit est obligatoire")
    private Product product;

    @Column(name = "quantity", nullable = false)
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au minimum 1")
    private Integer quantity;

    @Column(name = "planned_date", nullable = false)
    @NotNull(message = "La date de planification est obligatoire")
    private LocalDate plannedDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le statut est obligatoire")
    private ProductionOrderStatus status;

    @Column(name = "total_cost")
    @DecimalMin(value = "0.0", message = "Le coût total doit être positif")
    private Double totalCost;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Méthode utilitaire pour calculer le coût total de production
    public Double calculateTotalCost() {
        if (product != null && product.getProductionCost() != null && quantity != null) {
            return product.getProductionCost() * quantity;
        }
        return 0.0;
    }

    // Méthode pour vérifier si l'ordre est en retard
    public boolean isDelayed() {
        if (status == ProductionOrderStatus.EN_COURS && plannedDate != null) {
            return LocalDate.now().isAfter(plannedDate);
        }
        return false;
    }
}
