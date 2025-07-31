package com.niyiment.approvalflow.models;

import com.niyiment.approvalflow.enums.FileState;
import com.niyiment.approvalflow.enums.FileType;
import com.niyiment.approvalflow.enums.Priority;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
    private List<String> attachments;
    private double amount; // for financial approvals

    public FileTrackingItem(String id, String title, String description,
                            FileType type, User creator) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.creator = creator;
        this.currentState = FileState.DRAFT;
        this.priority = Priority.MEDIUM;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.metadata = new HashMap<>();
        this.transitionHistory = new ArrayList<>();
        this.attachments = new ArrayList<>();
        this.amount = 0.0;
    }

}
