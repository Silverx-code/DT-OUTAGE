package com.gridline.dtoutage.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "dt_outages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DtOutage {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "outage_id", updatable = false, nullable = false)
    private UUID outageId;

    @Column(name = "outage_ref", nullable = false, unique = true)
    private String outageRef;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dt_id", nullable = false)
    private DtMaster dt;

    // --- Snapshot columns: populated once at insert time from DtMaster and
    // never touched again, so later corrections to dt_master don't rewrite
    // history. See the comment in V1__init_schema.sql for the full reasoning. ---
    @Column(name = "dt_code_snapshot", nullable = false)
    private String dtCodeSnapshot;

    @Column(name = "business_unit_snapshot", nullable = false)
    private String businessUnitSnapshot;

    @Column(name = "undertaking_snapshot", nullable = false)
    private String undertakingSnapshot;

    @Column(name = "feeder_snapshot", nullable = false)
    private String feederSnapshot;

    @Column(name = "capacity_snapshot", nullable = false)
    private BigDecimal capacitySnapshot;

    @Column(name = "band_snapshot", nullable = false)
    private String bandSnapshot;

    @Column(name = "outage_date", nullable = false)
    private LocalDate outageDate;

    @Column(name = "outage_time", nullable = false)
    private LocalTime outageTime;

    /** Generated column (STORED) — Postgres computes this, Hibernate only reads it. */
    @Column(name = "outage_datetime", insertable = false, updatable = false)
    private Instant outageDatetime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fault_category_id", nullable = false)
    private FaultCategory faultCategory;

    @Column(name = "fault_description", nullable = false)
    private String faultDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id")
    private ChallengeCategory challenge;

    @Column(name = "additional_comment")
    private String additionalComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutageStatus status;

    @Column(name = "restoration_date")
    private LocalDate restorationDate;

    @Column(name = "restoration_time")
    private LocalTime restorationTime;

    /** Generated column (STORED) — null until restoration_date is set. */
    @Column(name = "restoration_datetime", insertable = false, updatable = false)
    private Instant restorationDatetime;

    @Column(name = "restoration_remarks")
    private String restorationRemarks;

    /** Generated column (STORED) — Postgres computes the duration; never set from Java. */
    @Column(name = "outage_duration_minutes", insertable = false, updatable = false)
    private Integer outageDurationMinutes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    @Column(name = "reported_at", nullable = false, updatable = false)
    private Instant reportedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restored_by")
    private User restoredBy;

    @Column(name = "restored_at")
    private Instant restoredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (reportedAt == null) {
            reportedAt = now;
        }
        if (status == null) {
            status = OutageStatus.OUT;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
