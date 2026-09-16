package com.gridline.dtoutage.web;

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

    /**
     * businessUnit is optional — Admin/SuperAdmin can omit it for an
     * org-wide view, while a plain USER's frontend should always pass
     * their own BU. Enforcing that a USER can't request another BU's
     * summary belongs in a follow-up pass once BU-scoping middleware
     * exists; for now this endpoint trusts the query param.
     */
    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasRole('USER')")
    public DashboardSummaryResponse summary(@RequestParam(required = false) String businessUnit) {
        return dashboardService.buildSummary(businessUnit);
    }
}
