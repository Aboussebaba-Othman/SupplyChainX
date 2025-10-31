package com.supplychainx.supply.dto.request;

import com.supplychainx.supply.enums.SupplyOrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplyOrderRequestDTO {

    @NotBlank(message = "Le numéro de commande est obligatoire")
    @Size(max = 50, message = "Le numéro de commande ne peut pas dépasser 50 caractères")
    private String orderNumber;

    @NotNull(message = "Le fournisseur est obligatoire")
    private Long supplierId;

    @NotNull(message = "La date de commande est obligatoire")
    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    @NotNull(message = "Le statut est obligatoire")
    private SupplyOrderStatus status;

    @NotEmpty(message = "La commande doit contenir au moins une ligne")
    @Valid
    private List<SupplyOrderLineRequestDTO> orderLines;
}
