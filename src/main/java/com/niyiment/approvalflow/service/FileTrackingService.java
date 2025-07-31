package com.niyiment.approvalflow.service;


import com.niyiment.approvalflow.enums.Action;
import com.niyiment.approvalflow.enums.FileState;
import com.niyiment.approvalflow.enums.FileType;
import com.niyiment.approvalflow.models.FileTrackingItem;
import com.niyiment.approvalflow.models.StateTransition;
import com.niyiment.approvalflow.models.User;
import com.niyiment.approvalflow.statemachine.FileTrackingStateMachine;
import com.niyiment.approvalflow.statemachine.StateTransitionRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class FileTrackingService {
    private Map<String, FileTrackingItem> items;
    private final FileTrackingStateMachine stateMachine;
    private final UserService userService;

    public FileTrackingService() {
        this.items = new HashMap<>();
        this.stateMachine = new FileTrackingStateMachine(
                new StateTransitionRule(),
                new EmailNotificationService()
        );
        this.userService = new UserService();
    }

    public FileTrackingItem createItem(String title, String description, FileType fileType, String creatorId, double amount) {
        User creator = userService.getUser(creatorId);
        String id = "FT-" + System.currentTimeMillis();

        FileTrackingItem item = new FileTrackingItem(id, title, description, fileType, creator);
        item.setAmount(amount);
        items.put(id, item);

        return item;
    }

    public boolean performAction(String itemId, Action action, String userId, String comment) {
        FileTrackingItem item = items.get(itemId);
        User user = userService.getUser(userId);

        if (item == null || user == null) {
            return false;
        }

        try {
            return stateMachine.executeAction(item, action, user, comment);
        } catch (IllegalStateException exception) {
           log.error("Action failed: {}", exception.getMessage());
            return false;
        }
    }

    public List<FileTrackingItem> getItemsByUser(String userId) {
        return items.values().stream()
                .filter(item -> item.getCreator().getId().equals(userId) ||
                        (item.getCurrentAssignee() != null &&
                                item.getCurrentAssignee().getId().equals(userId)))
                .toList();
    }

    public List<FileTrackingItem> getItemsByState(FileState state) {
        return items.values().stream()
                .filter(item -> item.getCurrentState() == state)
                .toList();
    }

    public FileTrackingItem getItem(String itemId) {
        return items.get(itemId);
    }

    public List<Action> getAvailableActions(String itemId, String userId) {
        FileTrackingItem item = items.get(itemId);
        User user = userService.getUser(userId);

        if (item == null || user == null) {
            return new ArrayList<>();
        }

        return stateMachine.getAvailableActions(item, user);
    }

    public void vendorPaymentApprovalDemo() {

        log.info("\n== Vendor Payment Approval Flow Demo ==\n");

        FileTrackingItem item = createItem(
                "Office supplies payment",
                "Payment for monthly office supplies from ABC Supplies",
                FileType.VENDOR_PAYMENT, "emp001", 15000.0
        );

        log.info("Created item: {} - {}",item.getId(), item.getTitle());
        log.info("Initial state: {}", item.getCurrentState());

        performAction(item.getId(), Action.SUBMIT, "emp001", "Initial submission");
        log.info("After vendor payment request submission: {}", item.getCurrentState());

        performAction(item.getId(), Action.APPROVE_L1, "mgr001", "Approved by manager");
        log.info("After L1 approval: {}", item.getCurrentState());

        performAction(item.getId(), Action.APPROVE_L2, "dir001", "Approved by director");
        log.info("After L2 approval: {}", item.getCurrentState());

        performAction(item.getId(), Action.APPROVE_L3, "ceo001", "Final approval by CEO");
        log.info("After L3 approval:{}", item.getCurrentState());

        performAction(item.getId(), Action.COMPLETE, "emp001", "Payment processed");
        log.info("After completion: {}",item.getCurrentState());

        performAction(item.getId(), Action.ARCHIVE, "emp001", "Archived for record keeping");
        log.info("Final state: {}", item.getCurrentState());

        log.info("\n== Transition History ==");
        for (StateTransition transition : item.getTransitionHistory()) {
            log.info("{} -> {} ({}) by {} at {} ", transition.getFromState(),
                    transition.getToState(), transition.getAction(),
                    transition.getPerformedBy().getName(), transition.getTimestamp()
            );
        }
    }

    public void leaveApprovalDemo() {
        log.info("\n== Leave Request Approval Flow Demo ==");
        FileTrackingItem leaveRequest = createItem(
                "Annual Leave Request",
                "Two weeks vacation in December",
                FileType.LEAVE_REQUEST,
                "emp001",
                0.0
        );

        log.info("Created leave request with the following details: {} - {}",leaveRequest.getId(), leaveRequest.getTitle());
        log.info("Initial state approval process: {}", leaveRequest.getCurrentState());

        performAction(leaveRequest.getId(), Action.SUBMIT, "emp001", "Vacation request");
        log.info("After submission: {}", leaveRequest.getCurrentState());

        performAction(leaveRequest.getId(), Action.APPROVE_L1, "mgr001", "Approved for vacation");

        log.info("Leave request final state: {}", leaveRequest.getCurrentState());
    }
}
