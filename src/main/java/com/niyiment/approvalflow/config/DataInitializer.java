package com.niyiment.approvalflow.config;

import com.niyiment.approvalflow.entity.ApprovalRequest;
import com.niyiment.approvalflow.enums.ApprovalState;
import com.niyiment.approvalflow.enums.Priority;
import com.niyiment.approvalflow.enums.RequestType;
import com.niyiment.approvalflow.repository.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final ApprovalRequestRepository requestRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (requestRepository.count() == 0) {
            initializeSampleData();
        }
    }
    
    private void initializeSampleData() {
        log.info("Initializing sample data...");
        
        // Sample request 1 - Leave Request
        ApprovalRequest leaveRequest = ApprovalRequest.builder()
                .requestNumber("REQ-LEAVE-001")
                .requestType(RequestType.LEAVE_REQUEST)
                .title("Annual Leave - 5 Days")
                .description("Annual leave request for vacation from Dec 20-24, 2024")
                .amount(BigDecimal.ZERO)
                .submittedBy("john.doe")
                .submitterEmail("john.doe@company.com")
                .department("IT")
                .priority(Priority.NORMAL)
                .currentState(ApprovalState.L1_PENDING)
                .level1Approver("john.smith")
                .level2Approver("jane.doe")
                .currentApprover("john.smith")
                .submittedAt(LocalDateTime.now().minusDays(1))
                .lastActionAt(LocalDateTime.now().minusDays(1))
                .dueDate(LocalDateTime.now().plusDays(4))
                .build();
        
        // Sample request 2 - Vendor Payment
        ApprovalRequest vendorPayment = ApprovalRequest.builder()
                .requestNumber("REQ-VENDOR-002")
                .requestType(RequestType.VENDOR_PAYMENT)
                .title("Software License Payment - Microsoft")
                .description("Annual Microsoft Office 365 license renewal payment")
                .amount(new BigDecimal("15000.00"))
                .submittedBy("sarah.wilson")
                .submitterEmail("sarah.wilson@company.com")
                .department("IT")
                .priority(Priority.HIGH)
                .currentState(ApprovalState.L2_PENDING)
                .level1Approver("john.smith")
                .level2Approver("jane.doe")
                .level3Approver("mike.wilson")
                .currentApprover("jane.doe")
                .submittedAt(LocalDateTime.now().minusDays(2))
                .lastActionAt(LocalDateTime.now().minusDays(1))
                .dueDate(LocalDateTime.now().plusDays(1))
                .build();
        
        // Sample request 3 - Petty Cash
        ApprovalRequest pettyCash = ApprovalRequest.builder()
                .requestNumber("REQ-CASH-003")
                .requestType(RequestType.PETTY_CASH)
                .title("Office Supplies Purchase")
                .description("Stationery and office supplies for Q1 2025")
                .amount(new BigDecimal("250.00"))
                .submittedBy("mary.johnson")
                .submitterEmail("mary.johnson@company.com")
                .department("HR")
                .priority(Priority.LOW)
                .currentState(ApprovalState.FINAL_APPROVED)
                .level1Approver("sarah.jones")
                .level2Approver("jane.doe")
                .currentApprover(null)
                .submittedAt(LocalDateTime.now().minusDays(5))
                .lastActionAt(LocalDateTime.now().minusDays(1))
                .dueDate(LocalDateTime.now().minusDays(2))
                .build();
        
        // Sample request 4 - Overdue Request
        ApprovalRequest overdueRequest = ApprovalRequest.builder()
                .requestNumber("REQ-EXP-004")
                .requestType(RequestType.EXPENSE_CLAIM)
                .title("Travel Expenses - Client Meeting")
                .description("Business travel expenses for client meeting in Chicago")
                .amount(new BigDecimal("1200.00"))
                .submittedBy("tom.brown")
                .submitterEmail("tom.brown@company.com")
                .department("OPERATIONS")
                .priority(Priority.URGENT)
                .currentState(ApprovalState.L1_PENDING)
                .level1Approver("john.smith")
                .level2Approver("david.brown")
                .currentApprover("john.smith")
                .submittedAt(LocalDateTime.now().minusDays(7))
                .lastActionAt(LocalDateTime.now().minusDays(7))
                .dueDate(LocalDateTime.now().minusDays(3))
                .build();
        
        requestRepository.save(leaveRequest);
        requestRepository.save(vendorPayment);
        requestRepository.save(pettyCash);
        requestRepository.save(overdueRequest);
        
        log.info("Sample data initialized with {} requests", requestRepository.count());
    }
}
