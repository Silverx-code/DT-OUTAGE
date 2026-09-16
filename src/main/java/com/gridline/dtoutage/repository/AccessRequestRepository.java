package com.gridline.dtoutage.repository;

import com.gridline.dtoutage.domain.AccessRequest;
import com.gridline.dtoutage.domain.AccessRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccessRequestRepository extends JpaRepository<AccessRequest, UUID> {
    List<AccessRequest> findByStatusOrderByCreatedAtAsc(AccessRequestStatus status);
}
