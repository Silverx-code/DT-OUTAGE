package com.gridline.dtoutage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

/**
 * A SuperAdmin automatically satisfies {@code hasRole('ADMIN')} or
 * {@code hasRole('USER')}, and an Admin automatically satisfies
 * {@code hasRole('USER')} — so controllers only ever need to declare the
 * *minimum* tier required, exactly like Section 3's permission table.
 */
@Configuration
public class RoleHierarchyConfig {

    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("SUPERADMIN").implies("ADMIN")
                .role("ADMIN").implies("USER")
                .build();
    }

    /**
     * Must be a static bean method — Spring Security resolves this very
     * early, before normal bean post-processing, so a non-static @Bean
     * method here can silently fail to apply the hierarchy.
     */
    @Bean
    static DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }
}
