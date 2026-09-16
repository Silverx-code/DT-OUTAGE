package com.gridline.dtoutage.repository;

import com.gridline.dtoutage.domain.DtOutage;
import com.gridline.dtoutage.domain.OutageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DtOutageRepository extends JpaRepository<DtOutage, UUID> {
    List<DtOutage> findByStatusOrderByOutageDateDescOutageTimeDesc(OutageStatus status);

    List<DtOutage> findByBusinessUnitSnapshotAndStatusOrderByOutageDateDescOutageTimeDesc(
            String businessUnit, OutageStatus status);

    Optional<DtOutage> findByOutageRef(String outageRef);

    boolean existsByDt_DtIdAndStatus(UUID dtId, OutageStatus status);

    long countByStatus(OutageStatus status);
}
