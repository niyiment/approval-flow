package com.niyiment.approvalflow.service;


import com.niyiment.approvalflow.entity.ApprovalRequest;
import com.niyiment.approvalflow.entity.NotificationLog;
import com.niyiment.approvalflow.enums.ApprovalState;
import com.niyiment.approvalflow.enums.NotificationType;
import com.niyiment.approvalflow.exception.ResourceNotFoundException;
import com.niyiment.approvalflow.repository.ApprovalRequestRepository;
import com.niyiment.approvalflow.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final JavaMailSender mailSender;
    private final ApprovalRequestRepository requestRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final ApproverService approverService;

    private static final String FROM_EMAIL = "approvals@company.com";
    private static final String APPROVAL_URL = "http://localhost:8080/approvals";

    @Async
    @Transactional
    public void sendSubmissionConfirmation(ApprovalRequest request) {
        String subject = String.format("Request submitted - %s", request.getRequestNumber());
        String message = buildSubmissionMessage(request);

        sendEmail(request.getSubmitterEmail(), subject, message, NotificationType.SUBMISSION_CONFIRMATION, request);
        log.info("Submission confirmation sent for request: {}", request.getRequestNumber());
    }

    @Async
    @Transactional
    public void sendApprovalRequest(ApprovalRequest request) {
        if (request.getCurrentApprover() == null) {
            log.warn("No current approver set for request: {}", request.getRequestNumber());
            return;
        }

        String approverEmail = approverService.getApproverEmail(request.getCurrentApprover());
        String subject = String.format("Approval Required - %s", request.getRequestNumber());
        String message = buildApprovalRequestMessage(request);

        sendEmail(approverEmail, subject, message,
                NotificationType.APPROVAL_REQUEST, request);

        log.info("Approval request sent to {} for request: {}",
                request.getCurrentApprover(), request.getRequestNumber());
    }

    @Async
    @Transactional
    public void sendFinalApproval(ApprovalRequest request) {
        String subject = String.format("Request Approved - %s", request.getRequestNumber());
        String message = buildFinalApprovalMessage(request);

        sendEmail(request.getSubmitterEmail(), subject, message,
                NotificationType.FINAL_APPROVAL, request);

        log.info("Final approval notification sent for request: {}", request.getRequestNumber());
    }

    @Async
    @Transactional
    public void sendRejectionNotice(ApprovalRequest request) {
        String subject = String.format("Request Rejected - %s", request.getRequestNumber());
        String message = buildRejectionMessage(request);

        sendEmail(request.getSubmitterEmail(), subject, message,
                NotificationType.REJECTION_NOTICE, request);

        log.info("Rejection notice sent for request: {}", request.getRequestNumber());
    }

    @Async
    @Transactional
    public void sendProcessingComplete(ApprovalRequest request) {
        String subject = String.format("Request Processed - %s", request.getRequestNumber());
        String message = buildProcessingCompleteMessage(request);

        sendEmail(request.getSubmitterEmail(), subject, message,
                NotificationType.PROCESSING_COMPLETE, request);

        log.info("Processing complete notification sent for request: {}", request.getRequestNumber());
    }

    // Scheduled reminder notifications
    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void sendReminderNotifications() {
        log.info("Starting reminder notification job");

        sendNormalReminders();
        sendWarningReminders();
        sendEscalationNotices();
        handleExpiredRequests();
    }

    private void sendNormalReminders() {
        // Send normal reminders after 2 days
        List<ApprovalRequest> requests = requestRepository.findRequestsForReminder(
                LocalDateTime.now().minusDays(2));

        for (ApprovalRequest request : requests) {
            if (shouldSendReminder(request, NotificationType.REMINDER_NORMAL, 2)) {
                sendReminderNotification(request, NotificationType.REMINDER_NORMAL);
            }
        }
    }

    private void sendWarningReminders() {
        List<ApprovalRequest> requests = requestRepository.findRequestsForReminder(
                LocalDateTime.now().minusDays(4));

        for (ApprovalRequest request : requests) {
            if (shouldSendReminder(request, NotificationType.REMINDER_WARNING, 4)) {
                sendReminderNotification(request, NotificationType.REMINDER_WARNING);
                sendSubmitterWarning(request);
            }
        }
    }

    private void sendEscalationNotices() {
        List<ApprovalRequest> requests = requestRepository.findRequestsForReminder(
                LocalDateTime.now().minusDays(6));

        for (ApprovalRequest request : requests) {
            if (shouldSendReminder(request, NotificationType.ESCALATION_NOTICE, 6)) {
                sendEscalationNotification(request);
            }
        }
    }

    private void handleExpiredRequests() {
        List<ApprovalRequest> overdueRequests = requestRepository.findOverdueRequests(
                LocalDateTime.now().minusDays(2));

        for (ApprovalRequest request : overdueRequests) {
            if (request.getCurrentState().name().contains("PENDING")) {
                request.setCurrentState(ApprovalState.EXPIRED);
                request.setLastActionAt(LocalDateTime.now());
                request.setCurrentApprover(null);

                sendExpirationNotice(request);

                log.info("Request expired: {}", request.getRequestNumber());
            }
        }

        requestRepository.saveAll(overdueRequests);
    }

    private void sendReminderNotification(ApprovalRequest request, NotificationType type) {
        String approverEmail = approverService.getApproverEmail(request.getCurrentApprover());
        String subject = String.format("%s - Approval Reminder - %s",
                getUrgencyPrefix(type), request.getRequestNumber());
        String message = buildReminderMessage(request, type);

        sendEmail(approverEmail, subject, message, type, request);

        log.info("Reminder ({}) sent to {} for request: {}",
                type, request.getCurrentApprover(), request.getRequestNumber());
    }

    private void sendSubmitterWarning(ApprovalRequest request) {
        String subject = String.format("Approval Delayed - %s", request.getRequestNumber());
        String message = buildSubmitterWarningMessage(request);

        sendEmail(request.getSubmitterEmail(), subject, message,
                NotificationType.REMINDER_WARNING, request);
    }

    private void sendEscalationNotification(ApprovalRequest request) {
        String managerEmail = approverService.getManagerEmail(request.getCurrentApprover());
        String subject = String.format("ESCALATION - Approval Overdue - %s", request.getRequestNumber());
        String message = buildEscalationMessage(request);

        sendEmail(managerEmail, subject, message, NotificationType.ESCALATION_NOTICE, request);

        String approverEmail = approverService.getApproverEmail(request.getCurrentApprover());
        sendEmail(approverEmail, subject, message, NotificationType.ESCALATION_NOTICE, request);

        log.info("Escalation notice sent for request: {}", request.getRequestNumber());
    }

    private void sendExpirationNotice(ApprovalRequest request) {
        String subject = String.format("Request Expired - %s", request.getRequestNumber());
        String message = buildExpirationMessage(request);

        sendEmail(request.getSubmitterEmail(), subject, message,
                NotificationType.ESCALATION_NOTICE, request);
    }

    private boolean shouldSendReminder(ApprovalRequest request, NotificationType type, int days) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        return request.getNotifications().stream()
                .noneMatch(n -> n.getNotificationType() == type &&
                        n.getCreatedAt().isAfter(oneDayAgo)) &&
                request.getDaysInCurrentState() >= days;
    }

    private void sendEmail(String to, String subject, String message,
                           NotificationType type, ApprovalRequest request) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(FROM_EMAIL);
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);

            mailSender.send(mailMessage);

            logNotification(request, type, to, subject, message, true, null);

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            logNotification(request, type, to, subject, message, false, e.getMessage());
        }
    }

    @Transactional
    public void addNotificationToRequest(Long requestId, NotificationLog notification) {
        NotificationLog savedLog = notificationLogRepository.save(notification);

        ApprovalRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        request.addNotification(savedLog);
    }

    private void logNotification(ApprovalRequest request, NotificationType type,
                                 String recipient, String subject, String message,
                                 boolean sent, String errorMessage) {
        NotificationLog notification = NotificationLog.builder()
                .request(request)
                .notificationType(type)
                .recipient(recipient)
                .subject(subject)
                .message(message)
                .sent(sent)
                .errorMessage(errorMessage)
                .sentAt(sent ? LocalDateTime.now() : null)
                .build();
        addNotificationToRequest(request.getId(), notification);
    }

    private String buildSubmissionMessage(ApprovalRequest request) {
        return String.format("""
            Dear %s,
            
            Your request has been successfully submitted and is now in the approval process.
            
            Request Details:
            - Request Number: %s
            - Type: %s
            - Title: %s
            - Amount: $%,.2f
            - Priority: %s
            - Current Status: Pending Level 1 Approval
            - Current Approver: %s
            - Due Date: %s
            
            You can track the progress of your request at: %s%d
            
            You will be notified of any updates to your request.
            
            Best regards,
            Approval System
            """,
                request.getSubmittedBy(),
                request.getRequestNumber(),
                request.getRequestType().toString().replace("_", " "),
                request.getTitle(),
                request.getAmount(),
                request.getPriority(),
                request.getCurrentApprover(),
                request.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                APPROVAL_URL,
                request.getId()
        );
    }

    private String buildApprovalRequestMessage(ApprovalRequest request) {
        return String.format("""
            Dear %s,
            
            A new request requires your approval.
            
            Request Details:
            - Request Number: %s
            - Type: %s
            - Title: %s
            - Amount: $%,.2f
            - Priority: %s
            - Submitted By: %s
            - Submitted Date: %s
            - Due Date: %s
            
            Description:
            %s
            
            Please review and approve/reject this request at: %s%d
            
            Best regards,
            Approval System
            """,
                request.getCurrentApprover(),
                request.getRequestNumber(),
                request.getRequestType().toString().replace("_", " "),
                request.getTitle(),
                request.getAmount(),
                request.getPriority(),
                request.getSubmittedBy(),
                request.getSubmittedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                request.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                request.getDescription(),
                APPROVAL_URL,
                request.getId()
        );
    }

    private String buildReminderMessage(ApprovalRequest request, NotificationType type) {
        String urgency = switch (type) {
            case REMINDER_NORMAL -> "This is a friendly reminder";
            case REMINDER_WARNING -> "⚠️ WARNING: This request is approaching its due date";
            case ESCALATION_NOTICE -> "🚨 URGENT: This request is overdue and has been escalated";
            default -> "Reminder";
        };

        return String.format("""
            Dear %s,
            
            %s that the following request is pending your approval:
            
            Request Details:
            - Request Number: %s
            - Type: %s
            - Title: %s
            - Amount: $%,.2f
            - Priority: %s
            - Submitted By: %s
            - Days Pending: %d
            - Due Date: %s
            
            Please review and take action at: %s%d
            
            %s
            
            Best regards,
            Approval System
            """,
                request.getCurrentApprover(),
                urgency,
                request.getRequestNumber(),
                request.getRequestType().toString().replace("_", " "),
                request.getTitle(),
                request.getAmount(),
                request.getPriority(),
                request.getSubmittedBy(),
                request.getDaysInCurrentState(),
                request.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                APPROVAL_URL,
                request.getId(),
                type == NotificationType.ESCALATION_NOTICE ?
                        "This matter requires immediate attention." : ""
        );
    }

    private String buildFinalApprovalMessage(ApprovalRequest request) {
        return String.format("""
            Dear %s,
            
            Great news! Your request has been fully approved and is ready for processing.
            
            Request Details:
            - Request Number: %s
            - Type: %s
            - Title: %s
            - Amount: $%,.2f
            - Final Approval Date: %s
            
            Your request will now be processed by the appropriate department.
            You will receive a confirmation once processing is complete.
            
            Best regards,
            Approval System
            """,
                request.getSubmittedBy(),
                request.getRequestNumber(),
                request.getRequestType().toString().replace("_", " "),
                request.getTitle(),
                request.getAmount(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
        );
    }

    private String buildRejectionMessage(ApprovalRequest request) {
        return String.format("""
            Dear %s,
            
            Unfortunately, your request has been rejected.
            
            Request Details:
            - Request Number: %s
            - Type: %s
            - Title: %s
            - Amount: $%,.2f
            - Rejection Date: %s
            - Rejected At: %s
            
            Reason for Rejection:
            %s
            
            You may revise and resubmit your request if appropriate.
            
            Best regards,
            Approval System
            """,
                request.getSubmittedBy(),
                request.getRequestNumber(),
                request.getRequestType().toString().replace("_", " "),
                request.getTitle(),
                request.getAmount(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                request.getCurrentState().toString().replace("_", " "),
                request.getRejectionReason()
        );
    }

    private String buildProcessingCompleteMessage(ApprovalRequest request) {
        return String.format("""
            Dear %s,
            
            Your approved request has been successfully processed.
            
            Request Details:
            - Request Number: %s
            - Type: %s
            - Title: %s
            - Amount: $%,.2f
            - Processing Complete Date: %s
            
            All actions related to this request have been completed.
            
            Best regards,
            Approval System
            """,
                request.getSubmittedBy(),
                request.getRequestNumber(),
                request.getRequestType().toString().replace("_", " "),
                request.getTitle(),
                request.getAmount(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
        );
    }

    private String buildSubmitterWarningMessage(ApprovalRequest request) {
        return String.format("""
            Dear %s,
            
            This is to inform you that your request is experiencing delays in the approval process.
            
            Request Details:
            - Request Number: %s
            - Current Status: Pending approval from %s
            - Days Pending: %d days
            - Due Date: %s
            
            We have sent reminder notifications to the approver.
            If this is urgent, you may want to contact the approver directly.
            
            Best regards,
            Approval System
            """,
                request.getSubmittedBy(),
                request.getRequestNumber(),
                request.getCurrentApprover(),
                request.getDaysInCurrentState(),
                request.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
        );
    }

    private String buildEscalationMessage(ApprovalRequest request) {
        return String.format("""
            Dear Manager,
            
            The following approval request has been escalated due to prolonged inactivity:
            
            Request Details:
            - Request Number: %s
            - Type: %s
            - Title: %s
            - Amount: $%,.2f
            - Assigned Approver: %s
            - Days Overdue: %d days
            - Original Due Date: %s
            
            This matter requires immediate attention to prevent further delays.
            
            Please review at: %s%d
            
            Best regards,
            Approval System
            """,
                request.getRequestNumber(),
                request.getRequestType().toString().replace("_", " "),
                request.getTitle(),
                request.getAmount(),
                request.getCurrentApprover(),
                request.getDaysInCurrentState(),
                request.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                APPROVAL_URL,
                request.getId()
        );
    }

    private String buildExpirationMessage(ApprovalRequest request) {
        return String.format("""
            Dear %s,
            
            Your request has expired due to prolonged inactivity in the approval process.
            
            Request Details:
            - Request Number: %s
            - Type: %s
            - Title: %s
            - Amount: $%,.2f
            - Expiration Date: %s
            
            If you still need this request processed, please resubmit it.
            
            Best regards,
            Approval System
            """,
                request.getSubmittedBy(),
                request.getRequestNumber(),
                request.getRequestType().toString().replace("_", " "),
                request.getTitle(),
                request.getAmount(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
        );
    }

    private String getUrgencyPrefix(NotificationType type) {
        return switch (type) {
            case REMINDER_NORMAL -> "REMINDER";
            case REMINDER_WARNING -> "WARNING";
            case ESCALATION_NOTICE -> "ESCALATION";
            default -> "NOTIFICATION";
        };
    }

}
