package com.gridline.dtoutage.web.dto;

import com.gridline.dtoutage.domain.AccessRequestStatus;
import jakarta.validation.constraints.NotNull;

public record DecideAccessRequestRequest(
        @NotNull AccessRequestStatus status, // APPROVED or REJECTED
        String notes
) {}
