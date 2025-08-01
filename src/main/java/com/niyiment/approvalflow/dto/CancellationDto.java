package com.niyiment.approvalflow.dto;

import jakarta.validation.constraints.NotBlank;

public record CancellationDto (
        @NotBlank
       String cancelledBy,
       @NotBlank
       String reason
){
}
