package com.niyiment.approvalflow.entity;


import com.niyiment.approvalflow.enums.ApprovalState;
import com.niyiment.approvalflow.enums.Priority;
import com.niyiment.approvalflow.enums.RequestType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * Entity for approval request
 */

@Entity
@Table(name="approval_request")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String requestNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    @NotBlank
    @Size(max = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal amount;

    @NotBlank
    private String submittedBy;

    @Email
    private String submitterEmail;

    @NotBlank
    private String department;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    private ApprovalState currentState;

    private String currentApprover;

    @Column(name = "level1_approver")
    private String level1Approver;

    @Column(name = "level2_approver")
    private String level2Approver;

    @Column(name = "level3_approver")
    private String level3Approver;

    private LocalDateTime submittedAt;
    private LocalDateTime lastActionAt;
    private LocalDateTime dueDate;
    private LocalDateTime processedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ApprovalAction>  actions = new ArrayList<>();

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RequestAttachment>  attachments = new ArrayList<>();

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<NotificationLog> notifications = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String rejectionReason;
    private String cancellationReason;

    public void addNotification(NotificationLog log) {
        notifications.add(log);
        log.setRequest(this);
    }

    public boolean requiresLevel3Approval() {
        return amount.compareTo(new BigDecimal("10000000")) > 0 ||
                requestType == RequestType.VENDOR_PAYMENT &&
                        amount.compareTo(new BigDecimal("5000000")) > 0;

    }

    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate);
    }

    public long getDaysInCurrentState() {
        if (lastActionAt != null) {
            return Duration.between(lastActionAt, LocalDateTime.now()).toDays();
        }
        return 0L;
    }

}
