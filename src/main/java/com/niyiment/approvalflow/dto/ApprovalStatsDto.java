package com.niyiment.approvalflow.dto;

import lombok.Builder;

import java.math.BigDecimal;


@Builder
public record ApprovalStatsDto(
        long totalRequests,
        long approvedRequests,
        long rejectedRequests,
        long cancelledRequests,
        long expiredRequests,
        double averageProcessingDays,
        BigDecimal totalApprovedAmount
) {
}
