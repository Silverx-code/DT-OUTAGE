package com.gridline.dtoutage.config;
import com.gridline.dtoutage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import java.util.*;
@Component @RequiredArgsConstructor
public class LocalJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final UserRepository users;
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID id; try { id = UUID.fromString(jwt.getSubject()); } catch (Exception e) { throw new IllegalArgumentException("Invalid session"); }
        var user = users.findById(id).filter(u -> u.isActive()).orElseThrow(() -> new IllegalArgumentException("Inactive account"));
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()), new SimpleGrantedAuthority("ROLE_AUTHENTICATED")), user.getEmail());
    }
}
