package com.supplychainx.supply.entity;

import com.supplychainx.common.entity.BaseEntity;
import com.supplychainx.supply.enums.SupplyOrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Supplier extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Le code fournisseur est obligatoire")
    @Size(max = 50, message = "Le code ne peut pas dépasser 50 caractères")
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Le nom du fournisseur est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String name;

    @Column(name = "contact", length = 255)
    @Size(max = 255, message = "Les informations de contact ne peuvent pas dépasser 255 caractères")
    private String contact;

    @Column(name = "email", length = 100)
    @Email(message = "L'email doit être valide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;

    @Column(name = "phone", length = 20)
    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String phone;

    @Column(name = "address", length = 255)
    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    private String address;

    @Column(name = "rating")
    @DecimalMin(value = "0.0", message = "La note doit être au minimum 0.0")
    @DecimalMax(value = "5.0", message = "La note doit être au maximum 5.0")
    private Double rating;

    @Column(name = "lead_time")
    @Min(value = 1, message = "Le délai doit être au minimum 1 jour")
    private Integer leadTime;  

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SupplyOrder> orders = new ArrayList<>();

    @ManyToMany(mappedBy = "suppliers", fetch = FetchType.LAZY)
    @Builder.Default
    private List<RawMaterial> materials = new ArrayList<>();

    
    public boolean hasActiveOrders() {
        return orders != null && orders.stream()
            .anyMatch(order -> order.getStatus() != SupplyOrderStatus.RECUE 
                            && order.getStatus() != SupplyOrderStatus.ANNULEE);
    }

    
    public Integer calculateAverageDeliveryTime() {
        return leadTime;
    }
}
