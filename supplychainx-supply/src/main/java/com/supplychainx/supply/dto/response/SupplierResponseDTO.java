package com.supplychainx.supply.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponseDTO {

    private Long id;
    private String code;
    private String name;
    private String contact;
    private String email;
    private String phone;
    private String address;
    private Double rating;
    private Integer leadTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
