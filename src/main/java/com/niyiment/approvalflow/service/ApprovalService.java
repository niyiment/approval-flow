package com.niyiment.approvalflow.service;


import com.niyiment.approvalflow.dto.ApprovalActionDto;
import com.niyiment.approvalflow.dto.ApprovalRequestDto;
import com.niyiment.approvalflow.dto.CreateRequestDto;
import com.niyiment.approvalflow.dto.DashboardDto;
import com.niyiment.approvalflow.entity.ApprovalAction;
import com.niyiment.approvalflow.entity.ApprovalRequest;
import com.niyiment.approvalflow.enums.ApprovalEvent;
import com.niyiment.approvalflow.enums.ApprovalState;
import com.niyiment.approvalflow.exception.ResourceNotFoundException;
import com.niyiment.approvalflow.repository.ApprovalActionRepository;
import com.niyiment.approvalflow.repository.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApprovalService {

    private final ApprovalRequestRepository requestRepository;
    private final ApprovalActionRepository actionRepository;
    private final StateMachineFactory<ApprovalState, ApprovalEvent> stateMachineFactory;
    private final StateMachinePersister<ApprovalState, ApprovalEvent, String> persister;
    private final NotificationService notificationService;
    private final ApproverService approverService;

    public ApprovalRequest createRequest(CreateRequestDto dto) {
        ApprovalRequest request = ApprovalRequest.builder()
                .requestNumber(generateRequestNumber())
                .requestType(dto.requestType())
                .title(dto.title())
                .description(dto.description())
                .amount(dto.amount())
                .submittedBy(dto.submittedBy())
                .submitterEmail(dto.submitterEmail())
                .department(dto.department())
                .priority(dto.priority())
                .currentState(ApprovalState.DRAFT)
                .build();

        setApprovers(request);

        return requestRepository.save(request);
    }

    public ApprovalRequest submitRequest(Long requestId) {
        ApprovalRequest request = getRequestById(requestId);

        if (request.getCurrentState() != ApprovalState.DRAFT) {
            throw new IllegalStateException("Only draft requests can be submitted");
        }

        request.setSubmittedAt(LocalDateTime.now());
        request.setLastActionAt(LocalDateTime.now());
        request.setDueDate(calculateDueDate(request));
        request.setCurrentState(ApprovalState.SUBMITTED);

        StateMachine<ApprovalState, ApprovalEvent> sm = createStateMachine(request);

        sendEvent(sm, ApprovalEvent.SUBMIT, request);
        routeToNextApprover(request);

        logAction(request, ApprovalState.DRAFT, ApprovalState.SUBMITTED,
                request.getSubmittedBy(), "Request submitted");
        notificationService.sendSubmissionConfirmation(request);
        notificationService.sendApprovalRequest(request);

        return requestRepository.save(request);
    }

    public ApprovalRequest approveRequest(Long requestId, String approver, String comments) {
        ApprovalRequest request = getRequestById(requestId);

        validateApprover(request, approver);

        ApprovalState currentState = request.getCurrentState();
        ApprovalEvent event = getApprovalEvent(currentState);
        ApprovalState newState = getNextApprovalState(currentState);

        StateMachine<ApprovalState, ApprovalEvent> sm = createStateMachine(request);

        sendEvent(sm, event, request);
        request.setCurrentState(newState);
        request.setLastActionAt(LocalDateTime.now());
        logAction(request, currentState, newState, approver,
                "Approved" + (comments != null ? ": " + comments : ""));

        if (isNextApprovalRequired(request)) {
            routeToNextApprover(request);
            notificationService.sendApprovalRequest(request);
        } else {
            sendEvent(sm, ApprovalEvent.FINAL_APPROVE, request);
            request.setCurrentState(ApprovalState.FINAL_APPROVED);
            notificationService.sendFinalApproval(request);
        }

        return requestRepository.save(request);
    }

    public ApprovalRequest rejectRequest(Long requestId, String approver, String reason) {
        ApprovalRequest request = getRequestById(requestId);

        validateApprover(request, approver);

        ApprovalState currentState = request.getCurrentState();
        ApprovalEvent event = getRejectionEvent(currentState);

        StateMachine<ApprovalState, ApprovalEvent> sm = createStateMachine(request);

        sendEvent(sm, event, request);

        request.setCurrentState(ApprovalState.REJECTED);
        request.setLastActionAt(LocalDateTime.now());
        request.setRejectionReason(reason);
        request.setCurrentApprover(null);

        logAction(request, currentState, ApprovalState.REJECTED, approver,
                "Rejected: " + reason);
        notificationService.sendRejectionNotice(request);

        return requestRepository.save(request);
    }

    public ApprovalRequest cancelRequest(Long requestId, String canceller, String reason) {
        ApprovalRequest request = getRequestById(requestId);

        if (!canCancel(request, canceller)) {
            throw new IllegalStateException("Request cannot be cancelled by this user");
        }

        ApprovalState currentState = request.getCurrentState();
        StateMachine<ApprovalState, ApprovalEvent> sm = createStateMachine(request);

        sendEvent(sm, ApprovalEvent.CANCEL, request);

        request.setCurrentState(ApprovalState.CANCELLED);
        request.setLastActionAt(LocalDateTime.now());
        request.setCancellationReason(reason);
        request.setCurrentApprover(null);
        logAction(request, currentState, ApprovalState.CANCELLED, canceller,
                "Cancelled: " + reason);

        return requestRepository.save(request);
    }

    public ApprovalRequest processRequest(Long requestId) {
        ApprovalRequest request = getRequestById(requestId);

        if (request.getCurrentState() != ApprovalState.FINAL_APPROVED) {
            throw new IllegalStateException("Only finally approved requests can be processed");
        }
        StateMachine<ApprovalState, ApprovalEvent> sm = createStateMachine(request);

        sendEvent(sm, ApprovalEvent.PROCESS, request);
        request.setCurrentState(ApprovalState.PROCESSED);
        request.setProcessedAt(LocalDateTime.now());
        request.setLastActionAt(LocalDateTime.now());
        request.setCurrentApprover(null);

        logAction(request, ApprovalState.FINAL_APPROVED, ApprovalState.PROCESSED,
                "SYSTEM", "Request processed");
        notificationService.sendProcessingComplete(request);

        return requestRepository.save(request);
    }

    public List<ApprovalRequest> getPendingRequestsForApprover(String approver) {
        return requestRepository.findByCurrentApproverAndCurrentStateIn(
                approver,
                List.of(ApprovalState.L1_PENDING, ApprovalState.L2_PENDING, ApprovalState.L3_PENDING)
        );
    }

    public List<ApprovalRequest> getRequestsBySubmitter(String submitter) {
        return requestRepository.findBySubmittedByOrderByCreatedAtDesc(submitter);
    }

    public List<ApprovalRequest> getOverdueRequests() {
        return requestRepository.findOverdueRequests(LocalDateTime.now());
    }

    public List<ApprovalRequest> getRequestsForReminder(int daysThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysThreshold);
        return requestRepository.findRequestsForReminder(threshold);
    }

    public ApprovalRequest getRequestById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));
    }

    private String generateRequestNumber() {
        return "REQ-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void setApprovers(ApprovalRequest request) {
        ApproverService.ApproverConfig config = approverService.getApproverConfig(
                request.getDepartment(), request.getAmount());

        request.setLevel1Approver(config.level1Approver());
        request.setLevel2Approver(config.level2Approver());

        if (request.requiresLevel3Approval()) {
            request.setLevel3Approver(config.level3Approver());
        }
    }

    private LocalDateTime calculateDueDate(ApprovalRequest request) {
        int daysToAdd = switch (request.getPriority()) {
            case URGENT -> 1;
            case HIGH -> 2;
            case NORMAL -> 5;
            case LOW -> 7;
        };
        return LocalDateTime.now().plusDays(daysToAdd);
    }

    private StateMachine<ApprovalState, ApprovalEvent> createStateMachine(ApprovalRequest request) {
        StateMachine<ApprovalState, ApprovalEvent> sm = stateMachineFactory.getStateMachine(request.getRequestNumber());
        sm.getExtendedState().getVariables().put("requestId", request.getId());
        sm.getExtendedState().getVariables().put("requiresL3", request.requiresLevel3Approval());
        try {
            persister.restore(sm, request.getRequestNumber());
        } catch (Exception e) {
            log.debug("No previous state found for request {}, starting fresh", request.getRequestNumber());
            sm.start();
        }

        return sm;
    }

    private void saveStateMachine(StateMachine<ApprovalState, ApprovalEvent> sm, String requestNumber) {
        try {
            persister.persist(sm, requestNumber);
        } catch (Exception e) {
            log.error("Failed to persist state machine for request {}: {}", requestNumber, e.getMessage());
        }
    }

    private void sendEvent(StateMachine<ApprovalState, ApprovalEvent> sm,
                           ApprovalEvent event, ApprovalRequest request) {
        Message<ApprovalEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("requestId", request.getId())
                .build();

        sm.sendEvent(message);
        request.setCurrentState(sm.getState().getId());
        saveStateMachine(sm, request.getRequestNumber());
    }

    public List<ApprovalRequest> getRequestsByState(ApprovalState state) {
        return requestRepository.findByCurrentStateOrderByCreatedAtDesc(state);
    }

    public List<ApprovalActionDto> getRequestHistory(Long requestId) {
        ApprovalRequest request = getRequestById(requestId);
        return request.getActions().stream()
                .map(ApprovalActionDto::fromEntity)
                .sorted((a, b) -> b.actionDate().compareTo(a.actionDate()))
                .toList();
    }

    public DashboardDto getDashboardData(String username) {
        return DashboardDto.builder()
                .totalPendingApprovals(getPendingRequestsForApprover(username).size())
                .myPendingRequests(getRequestsBySubmitter(username).stream()
                        .mapToInt(r -> r.getCurrentState().name().contains("PENDING") ? 1 : 0)
                        .sum())
                .overdueRequests(getOverdueRequests().size())
                .recentRequests(getRequestsBySubmitter(username).stream()
                        .limit(10)
                        .map(ApprovalRequestDto::fromEntity)
                        .toList())
                .build();
    }

    private void routeToNextApprover(ApprovalRequest request) {
        String nextApprover = switch (request.getCurrentState()) {
            case SUBMITTED, L1_PENDING -> {
                request.setCurrentState(ApprovalState.L1_PENDING);
                yield request.getLevel1Approver();
            }
            case L1_APPROVED, L2_PENDING -> {
                request.setCurrentState(ApprovalState.L2_PENDING);
                yield request.getLevel2Approver();
            }
            case L2_APPROVED, L3_PENDING -> {
                if (request.requiresLevel3Approval()) {
                    request.setCurrentState(ApprovalState.L3_PENDING);
                    yield request.getLevel3Approver();
                } else {
                    yield null;
                }
            }
            default -> null;
        };

        request.setCurrentApprover(nextApprover);
    }

    private boolean isNextApprovalRequired(ApprovalRequest request) {
        return switch (request.getCurrentState()) {
            case L1_APPROVED -> true;
            case L2_APPROVED -> request.requiresLevel3Approval();
            case L3_APPROVED -> false;
            default -> false;
        };
    }

    private ApprovalEvent getApprovalEvent(ApprovalState currentState) {
        return switch (currentState) {
            case L1_PENDING -> ApprovalEvent.L1_APPROVE;
            case L2_PENDING -> ApprovalEvent.L2_APPROVE;
            case L3_PENDING -> ApprovalEvent.L3_APPROVE;
            default -> throw new IllegalStateException("Invalid state for approval: " + currentState);
        };
    }

    private ApprovalEvent getRejectionEvent(ApprovalState currentState) {
        return switch (currentState) {
            case L1_PENDING -> ApprovalEvent.L1_REJECT;
            case L2_PENDING -> ApprovalEvent.L2_REJECT;
            case L3_PENDING -> ApprovalEvent.L3_REJECT;
            default -> throw new IllegalStateException("Invalid state for rejection: " + currentState);
        };
    }

    private ApprovalState getNextApprovalState(ApprovalState currentState) {
        return switch (currentState) {
            case L1_PENDING -> ApprovalState.L1_APPROVED;
            case L2_PENDING -> ApprovalState.L2_APPROVED;
            case L3_PENDING -> ApprovalState.L3_APPROVED;
            default -> throw new IllegalStateException("Invalid state for approval: " + currentState);
        };
    }

    private void validateApprover(ApprovalRequest request, String approver) {
        if (!approver.equals(request.getCurrentApprover())) {
            throw new IllegalArgumentException("User not authorized to approve this request");
        }
    }

    private boolean canCancel(ApprovalRequest request, String canceller) {
        return canceller.equals(request.getSubmittedBy()) ||
                approverService.isAdmin(canceller);
    }

    private void logAction(ApprovalRequest request, ApprovalState fromState,
                           ApprovalState toState, String actionBy, String action) {
        ApprovalAction approvalAction = ApprovalAction.builder()
                .request(request)
                .fromState(fromState)
                .toState(toState)
                .actionBy(actionBy)
                .action(action)
                .build();

        request.getActions().add(approvalAction);
    }
}


