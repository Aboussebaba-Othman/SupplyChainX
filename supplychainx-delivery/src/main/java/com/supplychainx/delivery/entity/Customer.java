package com.supplychainx.delivery.entity;

import com.supplychainx.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Customer extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Le code client est obligatoire")
    @Size(max = 50, message = "Le code ne peut pas dépasser 50 caractères")
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Le nom du client est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String name;

    @Column(name = "contact", length = 255)
    @Size(max = 255, message = "Le contact ne peut pas dépasser 255 caractères")
    private String contact;

    @Column(name = "phone", length = 20)
    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String phone;

    @Column(name = "email", length = 100)
    @Email(message = "L'email doit être valide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;

    @Column(name = "address", length = 255)
    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    private String address;

    @Column(name = "city", length = 100)
    @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
    private String city;

    @Column(name = "postal_code", length = 20)
    @Size(max = 20, message = "Le code postal ne peut pas dépasser 20 caractères")
    private String postalCode;

    @Column(name = "country", length = 100)
    @Size(max = 100, message = "Le pays ne peut pas dépasser 100 caractères")
    private String country;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DeliveryOrder> deliveryOrders = new ArrayList<>();
}
