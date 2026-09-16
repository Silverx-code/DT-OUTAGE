package com.gridline.dtoutage.service;

import com.gridline.dtoutage.domain.*;
import com.gridline.dtoutage.exception.ResourceNotFoundException;
import com.gridline.dtoutage.repository.AccessAuditLogRepository;
import com.gridline.dtoutage.repository.AccessRequestRepository;
import com.gridline.dtoutage.repository.UserRepository;
import com.gridline.dtoutage.web.dto.SubmitAccessRequestRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Mirrors the ASU UAR (User Access Request) profiling pattern:
 *
 *   1. SUBMIT   — requester picks a role/BU and, optionally, a reference
 *                 profile to be matched against (like matching a UAR form
 *                 to an existing CIS/CRM user).
 *   2. APPROVE  — an Admin (for USER-tier requests) or SuperAdmin (for
 *                 ADMIN/SUPERADMIN-tier requests) reviews and approves,
 *                 which is the moment the linked User row actually
 *                 activates with the granted role + BU.
 *   3. AUDIT    — every submit/approve/reject writes an AccessAuditLog row,
 *                 mirroring the OWIS "requester -> line manager -> HRBP"
 *                 approval-chain paper trail.
 *
 * Authorization for *who* can approve *what* tier lives here rather than
 * only in @PreAuthorize, because "an ADMIN can approve USER but not ADMIN
 * requests" depends on the specific request's requested role, not just the
 * caller's own role — that's a data-dependent check @PreAuthorize alone
 * can't express cleanly.
 */
@Service
@RequiredArgsConstructor
public class AccessRequestService {

    private final AccessRequestRepository accessRequestRepository;
    private final AccessAuditLogRepository accessAuditLogRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public AccessRequest submit(SubmitAccessRequestRequest request) {
        User requester = currentUserService.getOrCreateCurrentUser();

        User referenceUser = request.referenceUserId() == null ? null :
                userRepository.findById(request.referenceUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("Reference user not found"));

        AccessRequest accessRequest = AccessRequest.builder()
                .requestedBy(requester)
                .requesterName(requester.getFullName())
                .requesterEmail(requester.getEmail())
                .requestedRole(request.requestedRole())
                .businessUnit(request.businessUnit())
                .referenceUser(referenceUser)
                .justification(request.justification())
                .status(AccessRequestStatus.PENDING)
                .build();

        accessRequest = accessRequestRepository.save(accessRequest);

        accessAuditLogRepository.save(AccessAuditLog.builder()
                .requestId(accessRequest.getRequestId())
                .action("SUBMITTED")
                .performedBy(requester.getUserId())
                .build());

        return accessRequest;
    }

    public List<AccessRequest> findPending() {
        return accessRequestRepository.findByStatusOrderByCreatedAtAsc(AccessRequestStatus.PENDING);
    }

    @Transactional
    public AccessRequest approve(UUID requestId, String notes) {
        AccessRequest accessRequest = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Access request not found: " + requestId));
        User approver = currentUserService.getOrCreateCurrentUser();

        assertCanDecide(approver, accessRequest.getRequestedRole());

        // Provision: activate (or create) the User row with the granted
        // role + BU. If they already had an inert row from first login,
        // reuse it rather than creating a duplicate.
        String requesterEmail = accessRequest.getRequesterEmail();
        User grantee = accessRequest.getRequestedBy() != null
                ? accessRequest.getRequestedBy()
                : userRepository.findByEmail(requesterEmail)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No user record found for " + requesterEmail));

        grantee.setRole(accessRequest.getRequestedRole());
        grantee.setBusinessUnit(accessRequest.getBusinessUnit());
        grantee.setActive(true);
        userRepository.save(grantee);

        accessRequest.setStatus(AccessRequestStatus.APPROVED);
        accessRequest.setApprovedBy(approver);
        accessRequest.setApprovedAt(Instant.now());
        accessRequest = accessRequestRepository.save(accessRequest);

        accessAuditLogRepository.save(AccessAuditLog.builder()
                .requestId(accessRequest.getRequestId())
                .action("APPROVED")
                .performedBy(approver.getUserId())
                .notes(notes)
                .build());

        return accessRequest;
    }

    @Transactional
    public AccessRequest reject(UUID requestId, String notes) {
        AccessRequest accessRequest = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Access request not found: " + requestId));
        User approver = currentUserService.getOrCreateCurrentUser();

        assertCanDecide(approver, accessRequest.getRequestedRole());

        accessRequest.setStatus(AccessRequestStatus.REJECTED);
        accessRequest.setApprovedBy(approver);
        accessRequest.setApprovedAt(Instant.now());
        accessRequest = accessRequestRepository.save(accessRequest);

        accessAuditLogRepository.save(AccessAuditLog.builder()
                .requestId(accessRequest.getRequestId())
                .action("REJECTED")
                .performedBy(approver.getUserId())
                .notes(notes)
                .build());

        return accessRequest;
    }

    /**
     * Escalation rule mirroring "change requests need HRBP concurrence":
     * an ADMIN may approve USER-tier requests within their own authority,
     * but ADMIN- or SUPERADMIN-tier requests require a SUPERADMIN. This is
     * enforced here, not just via @PreAuthorize, because it depends on the
     * specific request's requested_role.
     */
    private void assertCanDecide(User approver, Role requestedRole) {
        boolean allowed = switch (requestedRole) {
            case USER -> approver.getRole() == Role.ADMIN || approver.getRole() == Role.SUPERADMIN;
            case ADMIN, SUPERADMIN -> approver.getRole() == Role.SUPERADMIN;
        };
        if (!allowed) {
            throw new AccessDeniedException(
                    "Only a SuperAdmin can approve " + requestedRole + "-tier requests.");
        }
    }
}
