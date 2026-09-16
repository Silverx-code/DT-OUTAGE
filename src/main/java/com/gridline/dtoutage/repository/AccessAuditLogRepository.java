package com.gridline.dtoutage.repository;

import com.gridline.dtoutage.domain.AccessAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccessAuditLogRepository extends JpaRepository<AccessAuditLog, Long> {
    List<AccessAuditLog> findByRequestIdOrderByPerformedAtAsc(UUID requestId);
}
