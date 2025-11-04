package com.supplychainx.delivery.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequestDTO {

    @NotBlank(message = "Le code client est obligatoire")
    @Size(max = 50, message = "Le code ne peut pas dépasser 50 caractères")
    private String code;

    @NotBlank(message = "Le nom du client est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String name;

    @Size(max = 255, message = "Le contact ne peut pas dépasser 255 caractères")
    private String contact;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String phone;

    @Email(message = "L'email doit être valide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;

    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    private String address;

    @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
    private String city;

    @Size(max = 20, message = "Le code postal ne peut pas dépasser 20 caractères")
    private String postalCode;

    @Size(max = 100, message = "Le pays ne peut pas dépasser 100 caractères")
    private String country;
}
