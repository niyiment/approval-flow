package com.niyiment.approvalflow.entity;


import com.niyiment.approvalflow.enums.ApprovalState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for approval actions
 */

@Entity
@Table(name = "approval_actions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private ApprovalRequest request;

    @Enumerated(EnumType.STRING)
    private ApprovalState fromState;

    @Enumerated(EnumType.STRING)
    private ApprovalState toState;

    @NotBlank
    private String actionBy;

    @NotBlank
    private String action;

    private String comments;

    @CreationTimestamp
    private LocalDateTime actionDate;
}
