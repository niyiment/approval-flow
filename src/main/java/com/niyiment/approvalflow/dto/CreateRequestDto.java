package com.niyiment.approvalflow.dto;

import com.niyiment.approvalflow.enums.ApprovalState;
import com.niyiment.approvalflow.enums.Priority;
import com.niyiment.approvalflow.enums.RequestType;
import lombok.Builder;

import java.math.BigDecimal;


@Builder
public record CreateRequestDto(
        String requestNumber,
        RequestType requestType,
        String title,
        String description,
        BigDecimal amount,
        String submittedBy,
        String submitterEmail,
        String department,
        Priority priority,
        ApprovalState approvalState
) {
}
