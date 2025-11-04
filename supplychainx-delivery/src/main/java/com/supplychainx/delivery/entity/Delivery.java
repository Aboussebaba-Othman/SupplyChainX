package com.supplychainx.delivery.entity;

import com.supplychainx.common.entity.BaseEntity;
import com.supplychainx.delivery.enums.DeliveryStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "deliveries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Delivery extends BaseEntity {

    @Column(name = "delivery_number", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Le numéro de livraison est obligatoire")
    @Size(max = 50, message = "Le numéro de livraison ne peut pas dépasser 50 caractères")
    private String deliveryNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_order_id", nullable = false, unique = true)
    @NotNull(message = "La commande de livraison est obligatoire")
    private DeliveryOrder deliveryOrder;

    @Column(name = "vehicle", length = 100)
    @Size(max = 100, message = "Le véhicule ne peut pas dépasser 100 caractères")
    private String vehicle;

    @Column(name = "driver", length = 100)
    @Size(max = 100, message = "Le chauffeur ne peut pas dépasser 100 caractères")
    private String driver;

    @Column(name = "driver_phone", length = 20)
    @Size(max = 20, message = "Le téléphone du chauffeur ne peut pas dépasser 20 caractères")
    private String driverPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Le statut est obligatoire")
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PLANIFIEE;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Column(name = "cost")
    @DecimalMin(value = "0.0", message = "Le coût ne peut pas être négatif")
    private Double cost;

    @Column(name = "tracking_number", length = 100)
    @Size(max = 100, message = "Le numéro de suivi ne peut pas dépasser 100 caractères")
    private String trackingNumber;

    // Méthodes métier
    public Double calculateCost() {
        // Logique de calcul du coût (peut être basé sur la distance, poids, etc.)
        return this.cost != null ? this.cost : 0.0;
    }

    public boolean canBeModified() {
        return status != DeliveryStatus.LIVREE && status != DeliveryStatus.ANNULEE;
    }

    public void markAsDelivered() {
        this.status = DeliveryStatus.LIVREE;
        this.actualDeliveryDate = LocalDate.now();
    }
}
