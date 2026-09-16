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
@Table(name = "outage_audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutageAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "outage_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID outageId;

    /** REPORTED | RESTORED | EDITED | REOPENED */
    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "performed_by", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID performedBy;

    @Column(name = "performed_at", nullable = false, updatable = false)
    private Instant performedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_values", columnDefinition = "jsonb")
    private String oldValues;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_values", columnDefinition = "jsonb")
    private String newValues;

    @PrePersist
    void onCreate() {
        if (performedAt == null) {
            performedAt = Instant.now();
        }
    }
}
