package com.niyiment.approvalflow.dto;

import com.niyiment.approvalflow.entity.ApprovalAction;
import com.niyiment.approvalflow.enums.ApprovalState;
import lombok.Builder;

import java.time.LocalDateTime;


@Builder
public record ApprovalActionDto(
        Long id,
        ApprovalState fromState,
        ApprovalState toState,
        String actionBy,
        String action,
        String comments,
        LocalDateTime actionDate
) {
    public static  ApprovalActionDto fromEntity(ApprovalAction entity) {
        return ApprovalActionDto.builder()
                .id(entity.getId())
                .fromState(entity.getFromState())
                .toState(entity.getToState())
                .actionBy(entity.getActionBy())
                .action(entity.getAction())
                .comments(entity.getComments())
                .actionDate(entity.getActionDate())
                .build();
    }
}
