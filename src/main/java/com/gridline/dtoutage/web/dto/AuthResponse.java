package com.gridline.dtoutage.web.dto;
import com.gridline.dtoutage.domain.Role;
public record AuthResponse(String token, String email, String fullName, Role role, boolean forcePasswordChange) {}
