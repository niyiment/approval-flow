package com.niyiment.approvalflow.dto;

import com.niyiment.approvalflow.entity.ApprovalRequest;
import com.niyiment.approvalflow.enums.ApprovalState;
import com.niyiment.approvalflow.enums.Priority;
import com.niyiment.approvalflow.enums.RequestType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ApprovalRequestDto(
         Long id,
         String requestNumber,
         RequestType type,
         String title,
         String description,
         BigDecimal amount,
         String submittedBy,
         String submitterEmail,
         String department,
         Priority priority,
         ApprovalState currentState,
         String currentApprover,
         String level1Approver,
         String level2Approver,
         String level3Approver,
         LocalDateTime submittedAt,
         LocalDateTime lastActionAt,
         LocalDateTime dueDate,
         LocalDateTime processedAt,
         LocalDateTime createdAt,
         LocalDateTime updatedAt,
         String rejectionReason,
         String cancellationReason,
         long daysInCurrentState,
         boolean overdue,
         List<ApprovalActionDto> actions

) {
    public static ApprovalRequestDto fromEntity(ApprovalRequest entity) {
        return ApprovalRequestDto.builder()
                .id(entity.getId())
                .requestNumber(entity.getRequestNumber())
                .type(entity.getRequestType())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .amount(entity.getAmount())
                .submittedBy(entity.getSubmittedBy())
                .submitterEmail(entity.getSubmitterEmail())
                .department(entity.getDepartment())
                .priority(entity.getPriority())
                .currentState(entity.getCurrentState())
                .currentApprover(entity.getCurrentApprover())
                .level1Approver(entity.getLevel1Approver())
                .level2Approver(entity.getLevel2Approver())
                .level3Approver(entity.getLevel3Approver())
                .submittedAt(entity.getSubmittedAt())
                .lastActionAt(entity.getLastActionAt())
                .dueDate(entity.getDueDate())
                .processedAt(entity.getProcessedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .rejectionReason(entity.getRejectionReason())
                .cancellationReason(entity.getCancellationReason())
                .daysInCurrentState(entity.getDaysInCurrentState())
                .overdue(entity.isOverdue())
                .actions(entity.getActions().stream()
                        .map(ApprovalActionDto::fromEntity)
                        .toList())
                .build();
    }
}
