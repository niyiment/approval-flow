package com.niyiment.approvalflow.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


/**
 * Entity for request attachements
 */

@Entity
@Table(name = "request_attachments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private ApprovalRequest request;

    @NotBlank
    private String fileName;

    @NotBlank
    private String originalFileName;

    private String contentType;

    private Long fileSize;

    private String filePath;

    @CreationTimestamp
    private LocalDateTime uploadedAt;

    private String uploadedBy;
}
