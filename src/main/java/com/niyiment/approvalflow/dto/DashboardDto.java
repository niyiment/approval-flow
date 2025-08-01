package com.niyiment.approvalflow.dto;

import lombok.Builder;

import java.util.List;


@Builder
public record DashboardDto(
        long totalPendingApprovals,
        long myPendingRequests,
        long overdueRequests,
        long requestsThisMonth,
        List<ApprovalRequestDto> recentRequests,
        List<ApprovalRequestDto> urgentRequests,
        ApprovalStatsDto stats
) {
}
