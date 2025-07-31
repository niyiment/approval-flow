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

    public User(String id, String name, String email, String department, int approvalLevel) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.approvalLevel = approvalLevel;
    }
}
