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

/**
 * Mirrors the ASU "UAR" (User Access Request) profiling pattern: a request
 * is submitted, optionally matched against a reference/template user, and
 * an Admin or SuperAdmin approves it before the linked {@link User} account
 * becomes active. See /areas/dt-outage-reporting.md design notes.
 */
@Entity
@Table(name = "access_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessRequest {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "request_id", updatable = false, nullable = false)
    private UUID requestId;

    /** Null if the requester doesn't have a users row yet (first-ever login). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by")
    private User requestedBy;

    @Column(name = "requester_name", nullable = false)
    private String requesterName;

    @Column(name = "requester_email", nullable = false)
    private String requesterEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_role", nullable = false)
    private Role requestedRole;

    @Column(name = "business_unit", nullable = false)
    private String businessUnit;

    /** The profile this request should be provisioned to match. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_user_id")
    private User referenceUser;

    @Column(name = "justification")
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccessRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = AccessRequestStatus.PENDING;
        }
    }
}
