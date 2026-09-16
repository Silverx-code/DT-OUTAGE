package com.gridline.dtoutage.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "access_audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "request_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID requestId;

    /** SUBMITTED | APPROVED | REJECTED | ESCALATED */
    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "performed_by")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID performedBy;

    @Column(name = "performed_at", nullable = false, updatable = false)
    private Instant performedAt;

    @Column(name = "notes")
    private String notes;

    @PrePersist
    void onCreate() {
        if (performedAt == null) {
            performedAt = Instant.now();
        }
    }
}
