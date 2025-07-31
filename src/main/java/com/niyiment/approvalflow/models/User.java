package com.niyiment.approvalflow.models;

import lombok.Data;

@Data
public class User {
    private String id;
    private String name;
    private String email;
    private String department;
    // 0 = regular user, 1-3 = approval levels
    private int approvalLevel;
}
