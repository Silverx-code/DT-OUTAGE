package com.gridline.dtoutage.web;

import com.gridline.dtoutage.domain.Role;
import com.gridline.dtoutage.service.CurrentUserService;
import com.gridline.dtoutage.service.DashboardService;
import com.gridline.dtoutage.web.dto.DashboardSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    /**
     * USERs are always scoped to their assigned business unit. PAT users
     * may optionally request a business-unit filter for reporting.
     */
    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasAnyRole('USER', 'PAT')")
    public DashboardSummaryResponse summary(@RequestParam(required = false) String businessUnit) {
        var user = currentUserService.getOrCreateCurrentUser();
        String effectiveBusinessUnit = user.getRole() == Role.USER
                ? (user.getBusinessUnit() == null ? "" : user.getBusinessUnit())
                : businessUnit;
        return dashboardService.buildSummary(effectiveBusinessUnit);
    }
}
