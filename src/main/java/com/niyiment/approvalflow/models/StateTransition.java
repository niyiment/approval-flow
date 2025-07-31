package com.niyiment.approvalflow.models;

import com.niyiment.approvalflow.enums.Action;
import com.niyiment.approvalflow.enums.FileState;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StateTransition {
    private FileState fromState;
    private FileState toState;
    private Action action;
    private User performedBy;
    private LocalDateTime timestamp;
    private String comment;

    public StateTransition(FileState fromState, FileState toState, Action action, User performedBy, String comment) {
            this.fromState = fromState;
            this.toState = toState;
            this.action = action;
            this.performedBy = performedBy;
            this.comment = comment;
            this.timestamp = LocalDateTime.now();
    }
}
