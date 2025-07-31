package com.niyiment.approvalflow.entity;


import com.niyiment.approvalflow.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


/**
 * Entity for notification logs
 */
@Entity
@Table(name = "notification_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private ApprovalRequest request;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    private String recipient;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    private Boolean sent;

    private String errorMessage;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;
}
