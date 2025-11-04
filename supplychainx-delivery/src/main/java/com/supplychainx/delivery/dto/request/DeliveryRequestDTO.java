package com.supplychainx.delivery.dto.request;

import com.supplychainx.delivery.enums.DeliveryStatus;
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
public class DeliveryRequestDTO {

    @NotBlank(message = "Le numéro de livraison est obligatoire")
    @Size(max = 50, message = "Le numéro de livraison ne peut pas dépasser 50 caractères")
    private String deliveryNumber;

    @NotNull(message = "La commande est obligatoire")
    private Long deliveryOrderId;

    @Size(max = 100, message = "Le véhicule ne peut pas dépasser 100 caractères")
    private String vehicle;

    @Size(max = 100, message = "Le chauffeur ne peut pas dépasser 100 caractères")
    private String driver;

    @Size(max = 20, message = "Le téléphone du chauffeur ne peut pas dépasser 20 caractères")
    private String driverPhone;

    private DeliveryStatus status;

    private LocalDate deliveryDate;

    @DecimalMin(value = "0.0", message = "Le coût ne peut pas être négatif")
    private Double cost;

    @Size(max = 100, message = "Le numéro de suivi ne peut pas dépasser 100 caractères")
    private String trackingNumber;
}
