package com.niyiment.approvalflow.service;

import com.niyiment.approvalflow.models.FileTrackingItem;
import com.niyiment.approvalflow.models.StateTransition;
import com.niyiment.approvalflow.statemachine.StateTransitionRule;

public interface NotificationService {
    void sendNotification(FileTrackingItem item, StateTransition transition);
}
