package com.niyiment.approvalflow.enums;


/**
 * Events that trigger state transitions
 */
public enum ApprovalEvent {
    SUBMIT,
    L1_APPROVE,
    L1_REJECT,
    L2_APPROVE,
    L2_REJECT,
    L3_APPROVE,
    L3_REJECT,
    FINAL_APPROVE,
    CANCEL,
    PROCESS,
    EXPIRE,
    RESET

}
