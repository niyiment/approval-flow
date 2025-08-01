package com.niyiment.approvalflow.service;

import com.niyiment.approvalflow.dto.ApprovalRequestDto;
import com.niyiment.approvalflow.dto.ApprovalStatsDto;
import com.niyiment.approvalflow.dto.DashboardDto;
import com.niyiment.approvalflow.entity.ApprovalRequest;
import com.niyiment.approvalflow.enums.ApprovalState;
import com.niyiment.approvalflow.enums.Priority;
import com.niyiment.approvalflow.repository.ApprovalActionRepository;
import com.niyiment.approvalflow.repository.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {
    
    private final ApprovalRequestRepository requestRepository;
    private final ApprovalActionRepository actionRepository;
    
    public DashboardDto getDashboardData(String username) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        long totalPending = requestRepository.countPendingRequestsByApprover(username);
        long myPending = requestRepository.findBySubmittedByOrderByCreatedAtDesc(username)
                .stream()
                .mapToLong(r -> r.getCurrentState().name().contains("PENDING") ? 1 : 0)
                .sum();
        long overdue = requestRepository.findOverdueRequests(now).size();
        long thisMonth = requestRepository.findByStateAndDateRange(
                null, monthStart, now).size();

        List<ApprovalRequestDto> recentRequests = requestRepository
                .findBySubmittedByOrderByCreatedAtDesc(username)
                .stream()
                .limit(10)
                .map(ApprovalRequestDto::fromEntity)
                .toList();

        List<ApprovalRequestDto> urgentRequests = requestRepository
                .findByCurrentApproverAndCurrentStateIn(username, 
                    List.of(ApprovalState.L1_PENDING, ApprovalState.L2_PENDING, ApprovalState.L3_PENDING))
                .stream()
                .filter(r -> r.getPriority() == Priority.URGENT || r.isOverdue())
                .map(ApprovalRequestDto::fromEntity)
                .toList();

        ApprovalStatsDto stats = calculateStats();
        
        return DashboardDto.builder()
                .totalPendingApprovals(totalPending)
                .myPendingRequests(myPending)
                .overdueRequests(overdue)
                .requestsThisMonth(thisMonth)
                .recentRequests(recentRequests)
                .urgentRequests(urgentRequests)
                .stats(stats)
                .build();
    }
    
    private ApprovalStatsDto calculateStats() {
        List<ApprovalRequest> allRequests = requestRepository.findAll();
        
        long total = allRequests.size();
        long approved = allRequests.stream()
                .mapToLong(r -> r.getCurrentState() == ApprovalState.PROCESSED ? 1 : 0)
                .sum();
        long rejected = allRequests.stream()
                .mapToLong(r -> r.getCurrentState() == ApprovalState.REJECTED ? 1 : 0)
                .sum();
        long cancelled = allRequests.stream()
                .mapToLong(r -> r.getCurrentState() == ApprovalState.CANCELLED ? 1 : 0)
                .sum();
        long expired = allRequests.stream()
                .mapToLong(r -> r.getCurrentState() == ApprovalState.EXPIRED ? 1 : 0)
                .sum();
        
        double avgDays = allRequests.stream()
                .filter(r -> r.getProcessedAt() != null && r.getSubmittedAt() != null)
                .mapToLong(r -> java.time.Duration.between(r.getSubmittedAt(), r.getProcessedAt()).toDays())
                .average()
                .orElse(0.0);
        
        BigDecimal totalAmount = allRequests.stream()
                .filter(r -> r.getCurrentState() == ApprovalState.PROCESSED)
                .map(ApprovalRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return ApprovalStatsDto.builder()
                .totalRequests(total)
                .approvedRequests(approved)
                .rejectedRequests(rejected)
                .cancelledRequests(cancelled)
                .expiredRequests(expired)
                .averageProcessingDays(avgDays)
                .totalApprovedAmount(totalAmount)
                .build();
    }
}