package com.gridline.dtoutage.config;

import com.gridline.dtoutage.domain.Role;
import com.gridline.dtoutage.domain.User;
import com.gridline.dtoutage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Maps an Entra identity to an Admin-provisioned local user account. */
@Component
@RequiredArgsConstructor
public class GridlineJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    @Value("${app.bootstrap-superadmin-oid:}")
    private String bootstrapSuperAdminOid;

    @Override
    @Transactional
    public AbstractAuthenticationToken convert(Jwt jwt) {
        User user = userRepository.findByAuthId(jwt.getSubject()).orElse(null);

        // The configured bootstrap identity is the one exception to the
        // Admin-created-user rule and becomes the first SuperAdmin.
        if (user == null && jwt.getSubject().equals(bootstrapSuperAdminOid)) {
            String email = firstPresent(jwt.getClaimAsString("preferred_username"),
                    jwt.getClaimAsString("email"), jwt.getClaimAsString("upn"));
            if (email == null) {
                throw new IllegalArgumentException("The bootstrap token must include an email claim.");
            }
            user = userRepository.save(User.builder()
                    .authId(jwt.getSubject())
                    .fullName(firstPresent(jwt.getClaimAsString("name"), email))
                    .email(email)
                    .role(Role.SUPERADMIN)
                    .active(true)
                    .build());
        } else if (user != null && jwt.getSubject().equals(bootstrapSuperAdminOid) && !user.isActive()) {
            user.setRole(Role.SUPERADMIN);
            user.setActive(true);
            user = userRepository.save(user);
        }

        List<GrantedAuthority> authorities = user != null && user.isActive()
                ? List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                : List.of();
        return new JwtAuthenticationToken(jwt, authorities, user != null ? user.getEmail() : jwt.getSubject());
    }

    private String firstPresent(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }
}
