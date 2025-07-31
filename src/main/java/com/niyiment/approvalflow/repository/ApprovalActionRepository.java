package com.niyiment.approvalflow.repository;

import com.niyiment.approvalflow.entity.ApprovalAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApprovalActionRepository extends JpaRepository<ApprovalAction, Long> {
    
    List<ApprovalAction> findByRequestIdOrderByActionDateDesc(Long requestId);
    
    List<ApprovalAction> findByActionByOrderByActionDateDesc(String actionBy);
    
    @Query("SELECT a FROM ApprovalAction a WHERE a.actionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.actionDate DESC")
    List<ApprovalAction> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(a) FROM ApprovalAction a WHERE a.actionBy = :actionBy AND " +
           "a.actionDate BETWEEN :startDate AND :endDate")
    long countActionsByUserAndDateRange(
        @Param("actionBy") String actionBy,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
}