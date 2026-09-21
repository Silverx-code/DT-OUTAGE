package com.gridline.dtoutage.web.dto;
import jakarta.validation.constraints.NotBlank;
public record ResetPasswordRequest(@NotBlank String token, @NotBlank String password) {}
