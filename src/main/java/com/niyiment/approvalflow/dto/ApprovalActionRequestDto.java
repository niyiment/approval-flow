package com.niyiment.approvalflow.dto;

import jakarta.validation.constraints.NotBlank;

public record ApprovalActionRequestDto(
        @NotBlank
        String actionBy,
        String comments
) {
}
