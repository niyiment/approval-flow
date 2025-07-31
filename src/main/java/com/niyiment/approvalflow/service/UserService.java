package com.niyiment.approvalflow.service;

import com.niyiment.approvalflow.models.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    private Map<String, User> users;

    public UserService() {
        this.users = new HashMap<>();
        initializeUsers();
    }

    private void initializeUsers() {
        users.put("emp001", new User("emp001", "John Doe", "john.doe@company.com", "IT", 0));
        users.put("mgr001", new User("mgr001", "Jane Smith", "jane.smith@company.com", "IT", 1));
        users.put("dir001", new User("dir001", "Bob Johnson", "bob.johnson@company.com", "Operations", 2));
        users.put("ceo001", new User("ceo001", "Alice Wilson", "alice.wilson@company.com", "Executive", 3));
    }

    public User getUser(String userId) {
        return users.get(userId);
    }

    public void addUser(User user) {
        users.put(user.getId(), user);
    }
}
