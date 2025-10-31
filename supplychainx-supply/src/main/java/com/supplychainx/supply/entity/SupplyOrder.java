package com.supplychainx.supply.entity;

import com.supplychainx.common.entity.BaseEntity;
import com.supplychainx.supply.enums.SupplyOrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "supply_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SupplyOrder extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Le numéro de commande est obligatoire")
    @Size(max = 50, message = "Le numéro de commande ne peut pas dépasser 50 caractères")
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    @NotNull(message = "Le fournisseur est obligatoire")
    private Supplier supplier;

    @Column(name = "order_date", nullable = false)
    @NotNull(message = "La date de commande est obligatoire")
    private LocalDate orderDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Le statut est obligatoire")
    @Builder.Default
    private SupplyOrderStatus status = SupplyOrderStatus.EN_ATTENTE;

    @Column(name = "total_amount")
    private Double totalAmount;

    @OneToMany(mappedBy = "supplyOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SupplyOrderLine> orderLines = new ArrayList<>();

    
    public boolean canBeDeleted() {
        return status == SupplyOrderStatus.EN_ATTENTE;
    }

    
    public boolean canBeModified() {
        return status != SupplyOrderStatus.RECUE;
    }

    public Double calculateTotalAmount() {
        return orderLines != null ? orderLines.stream()
            .mapToDouble(SupplyOrderLine::getTotalPrice)
            .sum() : 0.0;
    }

    
    public void markAsReceived() {
        this.status = SupplyOrderStatus.RECUE;
        this.actualDeliveryDate = LocalDate.now();
    }
}
