package com.supplychainx.delivery.entity;

import com.supplychainx.common.entity.BaseEntity;
import com.supplychainx.delivery.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delivery_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class DeliveryOrder extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Le numéro de commande est obligatoire")
    @Size(max = 50, message = "Le numéro de commande ne peut pas dépasser 50 caractères")
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "Le client est obligatoire")
    private Customer customer;

    @Column(name = "order_date", nullable = false)
    @NotNull(message = "La date de commande est obligatoire")
    private LocalDate orderDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Column(name = "delivery_address", length = 255)
    @Size(max = 255, message = "L'adresse de livraison ne peut pas dépasser 255 caractères")
    private String deliveryAddress;

    @Column(name = "delivery_city", length = 100)
    @Size(max = 100, message = "La ville de livraison ne peut pas dépasser 100 caractères")
    private String deliveryCity;

    @Column(name = "delivery_postal_code", length = 20)
    @Size(max = 20, message = "Le code postal ne peut pas dépasser 20 caractères")
    private String deliveryPostalCode;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Le statut est obligatoire")
    private OrderStatus status;

    @OneToOne(mappedBy = "deliveryOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Delivery delivery;

    @OneToMany(mappedBy = "deliveryOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryOrderLine> orderLines = new ArrayList<>();

    public void addOrderLine(DeliveryOrderLine orderLine) {
        orderLines.add(orderLine);
        orderLine.setDeliveryOrder(this);
    }

    public void removeOrderLine(DeliveryOrderLine orderLine) {
        orderLines.remove(orderLine);
        orderLine.setDeliveryOrder(null);
    }

    public Double calculateTotalAmount() {
        return orderLines.stream()
                .mapToDouble(DeliveryOrderLine::getLineTotal)
                .sum();
    }
}
