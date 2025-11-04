package com.supplychainx.delivery.dto.response;

import com.supplychainx.delivery.enums.OrderStatus;
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
public class DeliveryOrderResponseDTO {

    private Long id;
    private String orderNumber;
    private CustomerResponseDTO customer;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryPostalCode;
    private Double totalAmount;
    private OrderStatus status;
    private String notes;
    private List<DeliveryOrderLineResponseDTO> orderLines;
}
