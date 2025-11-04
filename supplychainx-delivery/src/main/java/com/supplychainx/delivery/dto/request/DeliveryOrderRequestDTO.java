package com.supplychainx.delivery.dto.request;

import com.supplychainx.delivery.enums.OrderStatus;
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
public class DeliveryOrderRequestDTO {

    @NotBlank(message = "Le numéro de commande est obligatoire")
    @Size(max = 50, message = "Le numéro de commande ne peut pas dépasser 50 caractères")
    private String orderNumber;

    @NotNull(message = "Le client est obligatoire")
    private Long customerId;

    @NotNull(message = "La date de commande est obligatoire")
    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    @Size(max = 255, message = "L'adresse de livraison ne peut pas dépasser 255 caractères")
    private String deliveryAddress;

    @Size(max = 100, message = "La ville de livraison ne peut pas dépasser 100 caractères")
    private String deliveryCity;

    @Size(max = 20, message = "Le code postal ne peut pas dépasser 20 caractères")
    private String deliveryPostalCode;

    private OrderStatus status;

    @NotNull(message = "Les lignes de commande sont obligatoires")
    @Size(min = 1, message = "Il doit y avoir au moins une ligne de commande")
    private List<DeliveryOrderLineRequestDTO> orderLines;
}
