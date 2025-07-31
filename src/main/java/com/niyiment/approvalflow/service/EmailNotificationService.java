package com.niyiment.approvalflow.service;

import com.niyiment.approvalflow.models.FileTrackingItem;
import com.niyiment.approvalflow.models.StateTransition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class EmailNotificationService implements NotificationService {

    @Override
    public void sendNotification(FileTrackingItem item, StateTransition transition) {

        String subject = String.format("File %s - %s", item.getId(), transition.getAction());
        String body = buildNotificationBody(item, transition);

        sendEmail(item.getCreator().getEmail(), subject, body);

        if (item.getCurrentAssignee() != null) {
            sendEmail(item.getCurrentAssignee().getEmail(), subject, body);
        }

        log.info("Email notification sent for item: {}", item.getId());
    }

    private String buildNotificationBody(FileTrackingItem item, StateTransition transition) {
        return String.format("File %s\nTitle: %s\nPerformed by: %s\nNew State: %s\nComment: %s\n",
                item.getId(), item.getTitle(), transition.getPerformedBy(), transition.getToState(),
                transition.getComment()
        );
    }
    private void sendEmail(String to, String subject, String body) {
        // Implement your actual send email here
        log.info("Sending email to: {}, Subject: {}", to, subject);
    }
}
