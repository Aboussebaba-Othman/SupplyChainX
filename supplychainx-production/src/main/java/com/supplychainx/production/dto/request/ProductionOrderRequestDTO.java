package com.supplychainx.production.dto.request;

import com.supplychainx.production.enums.ProductionOrderStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionOrderRequestDTO {

    @NotBlank(message = "Le numéro d'ordre est obligatoire")
    @Size(max = 50, message = "Le numéro d'ordre ne peut pas dépasser 50 caractères")
    private String orderNumber;

    @NotNull(message = "Le produit est obligatoire")
    private Long productId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au minimum 1")
    private Integer quantity;

    @NotNull(message = "La date de planification est obligatoire")
    private LocalDate plannedDate;

    @NotNull(message = "Le statut est obligatoire")
    private ProductionOrderStatus status;

    private String notes;
}
