package com.niyiment.approvalflow.statemachine;

import com.niyiment.approvalflow.enums.Action;
import com.niyiment.approvalflow.enums.FileState;
import com.niyiment.approvalflow.enums.FileType;
import com.niyiment.approvalflow.enums.Priority;
import com.niyiment.approvalflow.models.FileTrackingItem;
import com.niyiment.approvalflow.models.User;

import java.io.File;

import static com.niyiment.approvalflow.enums.Action.SUBMIT;

public class StateTransitionRule {

    public boolean canTransition(FileTrackingItem item , Action action, User user) {
        FileState currentState = item.getCurrentState();

        return switch (currentState) {
            case DRAFT -> action == SUBMIT && user.equals(item.getCreator());
            case SUBMITTED -> action == Action.APPROVE_L1 || action == Action.REJECT || action == Action.CANCEL;
            case PENDING_L1_APPROVAL -> (action == Action.APPROVE_L1 && user.getApprovalLevel() >= 1) ||
                    (action == Action.REJECT && user.getApprovalLevel() >= 1) ||
                    (action == Action.CANCEL && user.equals(item.getCreator()));
            case PENDING_L2_APPROVAL -> (action == Action.APPROVE_L2 && user.getApprovalLevel() >= 2) ||
                    (action == Action.REJECT && user.getApprovalLevel() >= 2);
            case PENDING_L3_APPROVAL -> (action == Action.APPROVE_L3 && user.getApprovalLevel() >= 3) ||
                    (action == Action.REJECT && user.getApprovalLevel() >= 3);
            case APPROVED -> action == Action.COMPLETE;
            case COMPLETED -> action == Action.ARCHIVE;
            case REJECTED -> action == Action.REVISE && user.equals(item.getCreator());
            default -> false;
        };
    }

    public FileState getNextState(FileTrackingItem item, Action action) {
        FileState currentState = item.getCurrentState();

        return switch(action) {
            case SUBMIT -> needsMultiLevelApproval(item) ? FileState.PENDING_L1_APPROVAL : FileState.APPROVED;
            case APPROVE_L1 -> needsL2Approval(item) ? FileState.PENDING_L2_APPROVAL :
                needsL3Approval(item) ? FileState.PENDING_L3_APPROVAL : FileState.APPROVED;
            case APPROVE_L2 -> needsL3Approval(item) ? FileState.PENDING_L3_APPROVAL : FileState.APPROVED;
            case APPROVE_L3 -> FileState.APPROVED;
            case REJECT -> FileState.REJECTED;
            case CANCEL -> FileState.CANCELLED;
            case COMPLETE -> FileState.COMPLETED;
            case ARCHIVE -> FileState.ARCHIVED;
            case REVISE -> FileState.DRAFT;
            default -> currentState;
        };
    }

    public User getNextAssignee(FileTrackingItem item, Action action) {
        return null;
    }

    private boolean needsMultiLevelApproval(FileTrackingItem item) {
        return item.getType() == FileType.VENDOR_PAYMENT && item.getAmount() > 1000 ||
                item.getType() == FileType.PURCHASE_ORDER && item.getAmount() > 5000 ||
                item.getPriority() == Priority.URGENT;
    }

    private boolean needsL2Approval(FileTrackingItem item) {
        return item.getAmount() > 5000 || item.getPriority() == Priority.HIGH;
    }

    private boolean needsL3Approval(FileTrackingItem item) {
        return  item.getAmount() > 25000 || item.getPriority() == Priority.URGENT;
    }
}
