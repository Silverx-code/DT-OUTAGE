package com.gridline.dtoutage.web.dto;

import com.gridline.dtoutage.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubmitAccessRequestRequest(
        @NotNull Role requestedRole,
        @NotBlank String businessUnit,
        UUID referenceUserId,
        String justification
) {}
