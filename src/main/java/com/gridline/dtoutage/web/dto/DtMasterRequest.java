package com.gridline.dtoutage.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DtMasterRequest(
        @NotBlank String dtCode,
        @NotBlank String dtName,
        @NotBlank String businessUnit,
        @NotBlank String undertaking,
        @NotBlank String feeder,
        @NotNull @DecimalMin("0.0") BigDecimal capacityKva,
        @NotBlank String supplyBand,
        Boolean active
) {}
