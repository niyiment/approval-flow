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
}
