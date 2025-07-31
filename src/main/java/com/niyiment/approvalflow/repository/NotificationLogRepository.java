package com.niyiment.approvalflow.repository;

import com.niyiment.approvalflow.entity.NotificationLog;
import com.niyiment.approvalflow.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByRequestIdOrderByCreatedAtDesc(Long requestId);

    List<NotificationLog> findByRecipientOrderByCreatedAtDesc(String recipient);

    List<NotificationLog> findBySentFalseOrderByCreatedAtDesc();

    @Query("SELECT n FROM NotificationLog n WHERE n.notificationType = :notificationType AND " +
     "n. createdAt BETWEEN :startDate AND :endDate")
    List<NotificationLog> findByTypeAndDateRange(
            @Param("notificationType")NotificationType notificationType,
            @Param("startDate")LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
            );

    @Query("SELECT COUNT(n) FROM NotificationLog n WHERE n.sent = true AND " +
    "n.createdAt BETWEEN :startDate AND :endDate")
    long countSentNotificationsByDateRange(
            @Param("startDate")LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
