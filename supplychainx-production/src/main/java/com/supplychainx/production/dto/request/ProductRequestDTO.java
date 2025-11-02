package com.supplychainx.production.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    @NotBlank(message = "Le code du produit est obligatoire")
    @Size(max = 50, message = "Le code ne peut pas dépasser 50 caractères")
    private String code;

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String name;

    private String description;

    @Size(max = 50, message = "La catégorie ne peut pas dépasser 50 caractères")
    private String category;

    @NotNull(message = "Le stock est obligatoire")
    @DecimalMin(value = "0.0", message = "Le stock ne peut pas être négatif")
    private Double stock;

    @NotNull(message = "Le stock minimum est obligatoire")
    @DecimalMin(value = "0.0", message = "Le stock minimum ne peut pas être négatif")
    private Double stockMin;

    @Min(value = 0, message = "Le temps de production doit être positif")
    private Integer productionTime;

    @DecimalMin(value = "0.0", message = "Le coût de production doit être positif")
    private Double cost;
}

