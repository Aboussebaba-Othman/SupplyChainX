package com.supplychainx.supply.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialRequestDTO {

    @NotBlank(message = "Le code de la matière première est obligatoire")
    @Size(max = 50, message = "Le code ne peut pas dépasser 50 caractères")
    private String code;

    @NotBlank(message = "Le nom de la matière première est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String name;

    @NotNull(message = "Le stock est obligatoire")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stock;

    @NotNull(message = "Le stock minimum est obligatoire")
    @Min(value = 0, message = "Le stock minimum ne peut pas être négatif")
    private Integer stockMin;

    @NotBlank(message = "L'unité de mesure est obligatoire")
    @Size(max = 20, message = "L'unité ne peut pas dépasser 20 caractères")
    private String unit;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix unitaire doit être supérieur à 0")
    private Double unitPrice;

    @Size(max = 50, message = "La catégorie ne peut pas dépasser 50 caractères")
    private String category;

    private List<Long> supplierIds;
}
