package com.gridline.dtoutage.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReportOutageRequest(
        @NotNull UUID dtId,
        @NotNull @PastOrPresent LocalDate outageDate,
        @NotNull LocalTime outageTime,
        @NotNull Integer faultCategoryId,
        @NotNull @Size(min = 10, max = 2000) String faultDescription,
        Integer challengeId,
        String additionalComment
) {}
