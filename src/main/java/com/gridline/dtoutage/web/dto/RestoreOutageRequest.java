package com.gridline.dtoutage.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record RestoreOutageRequest(
        @NotNull LocalDate restorationDate,
        @NotNull LocalTime restorationTime,
        @NotNull @Size(min = 1, max = 2000) String restorationRemarks,
        Integer challengeId
) {}
