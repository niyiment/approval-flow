package com.niyiment.approvalflow.controller;

import com.niyiment.approvalflow.dto.*;
import com.niyiment.approvalflow.entity.ApprovalRequest;
import com.niyiment.approvalflow.enums.ApprovalState;
import com.niyiment.approvalflow.service.ApprovalService;
import com.niyiment.approvalflow.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;



@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Slf4j
public class ApprovalController {

    private final ApprovalService approvalService;
    private final DashboardService dashboardService;

    @PostMapping
    public ResponseEntity<ApprovalRequestDto> createRequest(@Valid @RequestBody CreateRequestDto dto) {
        log.info("Creating approval request: {}", dto.title());

        ApprovalRequest request = approvalService.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApprovalRequestDto.fromEntity(request));
    }

    @PostMapping("/{requestId}/submit")
    public ResponseEntity<ApprovalRequestDto> submitRequest(@PathVariable Long requestId) {
        log.info("Submitting approval request: {}", requestId);

        ApprovalRequest request = approvalService.submitRequest(requestId);
        return ResponseEntity.ok(ApprovalRequestDto.fromEntity(request));
    }

    @PostMapping("/{requestId}/approve")
    public ResponseEntity<ApprovalRequestDto> approveRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody ApprovalActionDto dto) {
        log.info("Approving request {} by {}", requestId, dto.actionBy());

        ApprovalRequest request = approvalService.approveRequest(
                requestId, dto.actionBy(), dto.comments());
        return ResponseEntity.ok(ApprovalRequestDto.fromEntity(request));
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ApprovalRequestDto> rejectRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody RejectionDto dto) {
        log.info("Rejecting request {} by {}", requestId, dto.actionBy());

        ApprovalRequest request = approvalService.rejectRequest(
                requestId, dto.actionBy(), dto.reason());
        return ResponseEntity.ok(ApprovalRequestDto.fromEntity(request));
    }

    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<ApprovalRequestDto> cancelRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody CancellationDto dto) {
        log.info("Cancelling request {} by {}", requestId, dto.cancelledBy());

        ApprovalRequest request = approvalService.cancelRequest(
                requestId, dto.cancelledBy(), dto.reason());
        return ResponseEntity.ok(ApprovalRequestDto.fromEntity(request));
    }

    @PostMapping("/{requestId}/process")
    public ResponseEntity<ApprovalRequestDto> processRequest(@PathVariable Long requestId) {
        log.info("Processing approved request: {}", requestId);

        ApprovalRequest request = approvalService.processRequest(requestId);
        return ResponseEntity.ok(ApprovalRequestDto.fromEntity(request));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ApprovalRequestDto> getRequest(@PathVariable Long requestId) {
        ApprovalRequest request = approvalService.getRequestById(requestId);
        return ResponseEntity.ok(ApprovalRequestDto.fromEntity(request));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ApprovalRequestDto>> getPendingRequests(
            @RequestParam String approver) {
        List<ApprovalRequest> requests = approvalService.getPendingRequestsForApprover(approver);
        List<ApprovalRequestDto> dtos = requests.stream()
                .map(ApprovalRequestDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<ApprovalRequestDto>> getMyRequests(
            @RequestParam String submitter) {
        List<ApprovalRequest> requests = approvalService.getRequestsBySubmitter(submitter);
        List<ApprovalRequestDto> dtos = requests.stream()
                .map(ApprovalRequestDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<ApprovalRequestDto>> getOverdueRequests() {
        List<ApprovalRequest> requests = approvalService.getOverdueRequests();
        List<ApprovalRequestDto> dtos = requests.stream()
                .map(ApprovalRequestDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/status/{state}")
    public ResponseEntity<List<ApprovalRequestDto>> getRequestsByStatus(
            @PathVariable ApprovalState state) {
        List<ApprovalRequest> requests = approvalService.getRequestsByState(state);
        List<ApprovalRequestDto> dtos = requests.stream()
                .map(ApprovalRequestDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{requestId}/history")
    public ResponseEntity<List<ApprovalActionDto>> getRequestHistory(@PathVariable Long requestId) {
        List<ApprovalActionDto> history = approvalService.getRequestHistory(requestId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboard(@RequestParam String user) {
        DashboardDto dashboard = dashboardService.getDashboardData(user);
        return ResponseEntity.ok(dashboard);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.error("Invalid argument: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_ARGUMENT", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        log.error("Invalid state: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_STATE", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}

