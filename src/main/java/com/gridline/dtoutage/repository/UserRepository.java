package com.gridline.dtoutage.repository;

import com.gridline.dtoutage.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByAuthId(String authId);
    Optional<User> findByTenantIdAndAuthId(String tenantId, String authId);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    List<User> findAllByOrderByFullNameAsc();
}
