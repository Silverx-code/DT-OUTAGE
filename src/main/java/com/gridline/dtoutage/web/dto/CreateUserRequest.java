package com.gridline.dtoutage.web.dto;

import com.gridline.dtoutage.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotNull Role role,
        @NotBlank String businessUnit
) {}
