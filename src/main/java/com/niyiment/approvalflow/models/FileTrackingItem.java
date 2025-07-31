package com.niyiment.approvalflow.models;

import com.niyiment.approvalflow.enums.FileState;
import com.niyiment.approvalflow.enums.FileType;
import com.niyiment.approvalflow.enums.Priority;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class FileTrackingItem {
    private String id;
    private String title;
    private String description;
    private FileType type;
    private FileState currentState;
    private Priority priority;
    private User creator;
    private User currentAssignee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;
    private List<StateTransition> transitionHistory;
    private List<String> attachements;
    private double amount; // for financial approvals
}
