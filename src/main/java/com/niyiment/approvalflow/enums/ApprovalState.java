package com.niyiment.approvalflow.enums;

/**
 * States in the approval workflow
 */
public enum ApprovalState {
    DRAFT,
    SUBMITTED,
    L1_PENDING,
    L1_APPROVED,
    L2_PENDING,
    L2_APPROVED,
    L3_PENDING,
    L3_APPROVED,
    FINAL_APPROVED,
    REJECTED,
    CANCELLED,
    PROCESSED,
    EXPIRED
}
