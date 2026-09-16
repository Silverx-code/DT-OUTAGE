package com.gridline.dtoutage.web.dto;

import com.gridline.dtoutage.domain.Role;
import com.gridline.dtoutage.domain.User;

import java.util.UUID;

public record UserSummaryResponse(
        UUID userId,
        String fullName,
        String email,
        Role role,
        String businessUnit,
        boolean isActive
) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
                user.getUserId(), user.getFullName(), user.getEmail(),
                user.getRole(), user.getBusinessUnit(), user.isActive());
    }
}
