package com.supplychainx.delivery.dto.response;

import com.supplychainx.delivery.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponseDTO {

    private Long id;
    private String deliveryNumber;
    private DeliveryOrderResponseDTO deliveryOrder;
    private String vehicle;
    private String driver;
    private String driverPhone;
    private DeliveryStatus status;
    private LocalDate deliveryDate;
    private LocalDate actualDeliveryDate;
    private Double cost;
    private String trackingNumber;
}
