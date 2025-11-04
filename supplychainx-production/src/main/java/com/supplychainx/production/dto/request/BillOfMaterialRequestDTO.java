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
public class BillOfMaterialRequestDTO {

    @NotNull(message = "Le produit est obligatoire")
    private Long productId;

    @NotNull(message = "La matière première est obligatoire")
    private Long rawMaterialId;

    @NotNull(message = "La quantité est obligatoire")
    @DecimalMin(value = "0.01", message = "La quantité doit être supérieure à 0")
    private Double quantity;

    @NotBlank(message = "L'unité de mesure est obligatoire")
    @Size(max = 20, message = "L'unité ne peut pas dépasser 20 caractères")
    private String unit;
}
