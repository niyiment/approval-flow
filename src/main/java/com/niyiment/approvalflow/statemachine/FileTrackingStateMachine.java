package com.niyiment.approvalflow.statemachine;

import com.niyiment.approvalflow.enums.Action;
import com.niyiment.approvalflow.enums.FileState;
import com.niyiment.approvalflow.models.FileTrackingItem;
import com.niyiment.approvalflow.models.StateTransition;
import com.niyiment.approvalflow.models.User;
import com.niyiment.approvalflow.service.NotificationService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FileTrackingStateMachine {
    private final StateTransitionRule transitionRule;
    private final NotificationService notificationService;

    public FileTrackingStateMachine(StateTransitionRule transitionRule,
                                    NotificationService notificationService) {
        this.transitionRule = transitionRule;
        this.notificationService = notificationService;
    }

    public boolean executeAction(FileTrackingItem item, Action action, User user, String comment) {
        log.info("Item details => ID: {}, \nState: {}, \nAction: {}, \nUser: {}, \nApproval level: {}",
                item.getId(), item.getCurrentState(), action, user.getName(), user.getApprovalLevel()
                );
        if (!transitionRule.canTransition(item, action, user)) {
            throw new IllegalStateException(
                    String.format("Cannot perform action %s on item %s in state %s by user %s",
                            action, item.getId(), item.getCurrentState(), user.getName())
            );
        }

        FileState previousState = item.getCurrentState();
        FileState nextState = transitionRule.getNextState(item, action);
        User nextAssignee = transitionRule.getNextAssignee(item, action);

        StateTransition stateTransition = new StateTransition(previousState, nextState, action, user, comment);
        item.getTransitionHistory().add(stateTransition);
        item.setCurrentState(nextState);
        item.setCurrentAssignee(nextAssignee);

        notificationService.sendNotification(item, stateTransition);

        return true;
    }

    public List<Action> getAvailableActions(FileTrackingItem item, User user) {
        List<Action> availableActions = new ArrayList<>();

        for (Action action : Action.values()) {
            if (transitionRule.canTransition(item, action, user)) {
                availableActions.add(action);
            }
        }

        return availableActions;
    }
}
