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
import java.util.UUID;

@Entity
@Table(name = "dt_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DtMaster {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "dt_id", updatable = false, nullable = false)
    private UUID dtId;

    @Column(name = "dt_code", nullable = false, unique = true)
    private String dtCode;

    @Column(name = "dt_name", nullable = false)
    private String dtName;

    @Column(name = "business_unit", nullable = false)
    private String businessUnit;

    @Column(name = "undertaking", nullable = false)
    private String undertaking;

    @Column(name = "feeder", nullable = false)
    private String feeder;

    @Column(name = "capacity_kva", nullable = false)
    private BigDecimal capacityKva;

    @Column(name = "supply_band", nullable = false)
    private String supplyBand;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
