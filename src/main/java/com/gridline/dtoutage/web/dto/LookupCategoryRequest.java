package com.gridline.dtoutage.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record LookupCategoryRequest(
        @NotBlank String name,
        @Min(0) Integer sortOrder,
        Boolean active
) {}
