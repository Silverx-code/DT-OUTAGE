package com.gridline.dtoutage.web.dto;

import com.gridline.dtoutage.domain.AccessRequest;
import com.gridline.dtoutage.domain.AccessRequestStatus;
import com.gridline.dtoutage.domain.Role;

import java.time.Instant;
import java.util.UUID;

public record AccessRequestResponse(
        UUID requestId,
        String requestedByName,
        Role requestedRole,
        String businessUnit,
        String referenceUserName,
        String justification,
        AccessRequestStatus status,
        String approvedByName,
        Instant approvedAt,
        Instant createdAt
) {
    public static AccessRequestResponse from(AccessRequest r) {
        return new AccessRequestResponse(
                r.getRequestId(),
                r.getRequesterName(),
                r.getRequestedRole(),
                r.getBusinessUnit(),
                r.getReferenceUser() != null ? r.getReferenceUser().getFullName() + " (" + r.getReferenceUser().getRole() + ")" : null,
                r.getJustification(),
                r.getStatus(),
                r.getApprovedBy() != null ? r.getApprovedBy().getFullName() : null,
                r.getApprovedAt(),
                r.getCreatedAt()
        );
    }
}
