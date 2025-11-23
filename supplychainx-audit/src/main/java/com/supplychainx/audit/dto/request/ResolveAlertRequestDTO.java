package com.supplychainx.audit.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveAlertRequestDTO {

    @NotBlank(message = "Resolved by is required")
    private String resolvedBy;

    private String resolutionComment;
}
