package com.gridline.dtoutage.web;

import com.gridline.dtoutage.domain.AccessRequest;
import com.gridline.dtoutage.domain.AccessRequestStatus;
import com.gridline.dtoutage.service.AccessRequestService;
import com.gridline.dtoutage.web.dto.AccessRequestResponse;
import com.gridline.dtoutage.web.dto.DecideAccessRequestRequest;
import com.gridline.dtoutage.web.dto.SubmitAccessRequestRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/access-requests")
@RequiredArgsConstructor
public class AccessRequestController {

    private final AccessRequestService accessRequestService;

    /** Anyone who can authenticate (including a brand-new, still-inactive user) can submit a UAR. */
    @PostMapping
    public ResponseEntity<AccessRequestResponse> submit(@Valid @RequestBody SubmitAccessRequestRequest request) {
        AccessRequest created = accessRequestService.submit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AccessRequestResponse.from(created));
    }

    /** Queue view — Admins see it to profile USER-tier requests, SuperAdmins see everything. */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AccessRequestResponse> pending() {
        return accessRequestService.findPending().stream().map(AccessRequestResponse::from).toList();
    }

    @PatchMapping("/{requestId}")
    @PreAuthorize("hasRole('ADMIN')") // fine-grained USER-vs-ADMIN-vs-SUPERADMIN tier check happens in the service
    public AccessRequestResponse decide(
            @PathVariable UUID requestId,
            @Valid @RequestBody DecideAccessRequestRequest request) {
        AccessRequest decided = request.status() == AccessRequestStatus.APPROVED
                ? accessRequestService.approve(requestId, request.notes())
                : accessRequestService.reject(requestId, request.notes());
        return AccessRequestResponse.from(decided);
    }
}
