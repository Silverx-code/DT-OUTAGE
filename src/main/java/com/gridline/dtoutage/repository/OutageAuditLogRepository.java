package com.gridline.dtoutage.repository;

import com.gridline.dtoutage.domain.OutageAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutageAuditLogRepository extends JpaRepository<OutageAuditLog, Long> {
    List<OutageAuditLog> findByOutageIdOrderByPerformedAtAsc(UUID outageId);
}
