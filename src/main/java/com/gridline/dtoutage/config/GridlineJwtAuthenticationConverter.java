package com.gridline.dtoutage.config;

import com.gridline.dtoutage.domain.Role;
import com.gridline.dtoutage.domain.User;
import com.gridline.dtoutage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Runs once per authenticated request, before any controller or
 * {@code @PreAuthorize} check. Two jobs:
 *
 * <ol>
 *   <li>Find-or-create the local {@link User} row for this Entra ID
 *       identity (first login creates an inert row — {@code active=false},
 *       {@code role=USER} — that stays powerless until an
 *       {@link com.gridline.dtoutage.domain.AccessRequest} is approved).</li>
 *   <li>Grant a single {@code ROLE_<role>} authority sourced from *our*
 *       database, not from Entra ID app roles or groups — because role
 *       assignment here is governed by the UAR-style profiling workflow
 *       (Admin/SuperAdmin approval), not by anything configured in Azure.</li>
 * </ol>
 *
 * An inactive user (row exists, no approved access request yet) is granted
 * no authorities at all, so every {@code @PreAuthorize("hasRole('USER')")}
 * check correctly locks them out until they're provisioned.
 */
@Component
@RequiredArgsConstructor
public class GridlineJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    @Value("${app.bootstrap-superadmin-oid:}")
    private String bootstrapSuperAdminOid;

    @Override
    @Transactional
    public AbstractAuthenticationToken convert(Jwt jwt) {
        User user = userRepository.findByAuthId(jwt.getSubject()).orElseGet(() -> provisionInertUser(jwt));

        List<GrantedAuthority> authorities = user.isActive()
                ? List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                : List.of();

        return new JwtAuthenticationToken(jwt, authorities, user.getEmail());
    }

    private User provisionInertUser(Jwt jwt) {
        String email = firstPresent(
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("upn"));
        if (email == null) {
            throw new IllegalArgumentException(
                    "The access token must include preferred_username, email, or upn to provision a local user.");
        }
        String fullName = jwt.getClaimAsString("name");
        boolean isBootstrapSuperAdmin = jwt.getSubject().equals(bootstrapSuperAdminOid);

        return userRepository.save(User.builder()
                .authId(jwt.getSubject())
                .email(email)
                .fullName(fullName != null ? fullName : email)
                .role(isBootstrapSuperAdmin ? Role.SUPERADMIN : Role.USER)
                .active(isBootstrapSuperAdmin)
                .build());
    }

    private String firstPresent(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
