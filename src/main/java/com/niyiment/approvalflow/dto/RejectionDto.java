package com.niyiment.approvalflow.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectionDto(
        @NotBlank
        String actionBy,
        @NotBlank
        String reason
) {
}
