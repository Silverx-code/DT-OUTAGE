package com.gridline.dtoutage.service;

import com.gridline.dtoutage.domain.User;
import com.gridline.dtoutage.exception.ResourceNotFoundException;
import com.gridline.dtoutage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Resolves the authenticated caller's local {@link User} row from the JWT
 * already sitting in the security context.
 *
 * <p>The row itself is guaranteed to exist by
 * {@link com.gridline.dtoutage.config.GridlineJwtAuthenticationConverter},
 * which runs during authentication (before any @PreAuthorize check or
 * controller method executes) and creates an inert User the first time a
 * given Entra ID identity is ever seen. That converter is also where the
 * granted ROLE_* authority comes from — this service just needs to load
 * the matching row for services that want the full entity (full name, BU,
 * etc.), not just the role.
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getOrCreateCurrentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String authId = firstPresent(jwt.getClaimAsString("oid"), jwt.getSubject());
        String tenantId = jwt.getClaimAsString("tid");
        return userRepository.findByTenantIdAndAuthId(tenantId, authId)
                .or(() -> userRepository.findByTenantIdAndAuthId(tenantId, jwt.getSubject()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No local user row for authenticated Entra object ID " + authId
                                + " — this should not happen if GridlineJwtAuthenticationConverter ran."));
    }

    private String firstPresent(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }
}
