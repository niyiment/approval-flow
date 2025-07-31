package com.niyiment.approvalflow.repository;

import com.niyiment.approvalflow.entity.ApprovalRequest;
import com.niyiment.approvalflow.enums.ApprovalState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {
    
    Optional<ApprovalRequest> findByRequestNumber(String requestNumber);
    
    List<ApprovalRequest> findBySubmittedByOrderByCreatedAtDesc(String submittedBy);
    
    List<ApprovalRequest> findByCurrentApproverAndCurrentStateIn(
        String currentApprover, List<ApprovalState> states);
    
    List<ApprovalRequest> findByCurrentStateOrderByCreatedAtDesc(ApprovalState state);
    
    List<ApprovalRequest> findByDepartmentOrderByCreatedAtDesc(String department);
    
    @Query("SELECT r FROM ApprovalRequest r WHERE r.dueDate < :currentTime AND " +
           "r.currentState IN ('L1_PENDING', 'L2_PENDING', 'L3_PENDING')")
    List<ApprovalRequest> findOverdueRequests(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT r FROM ApprovalRequest r WHERE r.lastActionAt < :threshold AND " +
           "r.currentState IN ('L1_PENDING', 'L2_PENDING', 'L3_PENDING')")
    List<ApprovalRequest> findRequestsForReminder(@Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT r FROM ApprovalRequest r WHERE r.currentState = :state AND " +
           "r.createdAt BETWEEN :startDate AND :endDate")
    List<ApprovalRequest> findByStateAndDateRange(
        @Param("state") ApprovalState state,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(r) FROM ApprovalRequest r WHERE r.currentApprover = :approver AND " +
           "r.currentState IN ('L1_PENDING', 'L2_PENDING', 'L3_PENDING')")
    long countPendingRequestsByApprover(@Param("approver") String approver);
    
    @Query("SELECT r FROM ApprovalRequest r WHERE r.amount >= :minAmount AND " +
           "r.currentState = :state ORDER BY r.amount DESC")
    List<ApprovalRequest> findHighValueRequests(
        @Param("minAmount") java.math.BigDecimal minAmount,
        @Param("state") ApprovalState state);
}
