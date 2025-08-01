package com.niyiment.approvalflow.service;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApproverService {
    private final Map<String, ApproverInfo> approvers = new HashMap<>();
    private final Map<String, ApproverConfig> departmentConfigs = new HashMap<>();
    {
        approvers.put("john.smith", new ApproverInfo("john.smith", "john.smith@company.com", "jane.doe"));
        approvers.put("jane.doe", new ApproverInfo("jane.doe", "jane.doe@company.com", "mike.wilson"));
        approvers.put("mike.wilson", new ApproverInfo("mike.wilson", "mike.wilson@company.com", "ceo@company.com"));
        approvers.put("sarah.jones", new ApproverInfo("sarah.jones", "sarah.jones@company.com", "jane.doe"));
        approvers.put("david.brown", new ApproverInfo("david.brown", "david.brown@company.com", "mike.wilson"));

        departmentConfigs.put("IT", new ApproverConfig("john.smith", "jane.doe", "mike.wilson"));
        departmentConfigs.put("HR", new ApproverConfig("sarah.jones", "jane.doe", "mike.wilson"));
        departmentConfigs.put("FINANCE", new ApproverConfig("david.brown", "jane.doe", "mike.wilson"));
        departmentConfigs.put("OPERATIONS", new ApproverConfig("john.smith", "david.brown", "mike.wilson"));
    }

    public ApproverConfig getApproverConfig(String department, BigDecimal amount) {
        ApproverConfig config = departmentConfigs.get(department.toUpperCase());
        if (config == null) {
            return new ApproverConfig("john.smith", "jane.doe", "mike.wilson");
        }
        return config;
    }

    public String getApproverEmail(String username) {
        ApproverInfo info = approvers.get(username);
        return info != null ? info.email() : username + "@company.com";
    }

    public String getManagerEmail(String username) {
        ApproverInfo info = approvers.get(username);
        if (info != null && info.manager() != null) {
            return getApproverEmail(info.manager());
        }
        return "manager@company.com";
    }

    public boolean isAdmin(String username) {
        return "admin".equals(username) || "mike.wilson".equals(username);
    }

    public boolean isValidApprover(String username) {
        return approvers.containsKey(username);
    }

    public record ApproverInfo(String username, String email, String manager) {
    }

    public record ApproverConfig(String level1Approver, String level2Approver, String level3Approver) {
    }
}
