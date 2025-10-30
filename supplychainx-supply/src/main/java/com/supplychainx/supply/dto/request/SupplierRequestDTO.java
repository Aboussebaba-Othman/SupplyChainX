package com.supplychainx.supply.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequestDTO {

    @NotBlank(message = "Le code du fournisseur est obligatoire")
    @Size(max = 50, message = "Le code ne peut pas dépasser 50 caractères")
    private String code;

    @NotBlank(message = "Le nom du fournisseur est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String name;

    @Size(max = 255, message = "Les informations de contact ne peuvent pas dépasser 255 caractères")
    private String contact;

    @Email(message = "L'email doit être valide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Le numéro de téléphone doit être valide")
    private String phone;

    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    private String address;

    @DecimalMin(value = "0.0", message = "La note doit être au minimum 0.0")
    @DecimalMax(value = "5.0", message = "La note doit être au maximum 5.0")
    private Double rating;

    @Min(value = 1, message = "Le délai doit être au minimum 1 jour")
    private Integer leadTime;
}
