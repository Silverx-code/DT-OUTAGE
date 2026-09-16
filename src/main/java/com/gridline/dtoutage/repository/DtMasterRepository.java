package com.gridline.dtoutage.repository;

import com.gridline.dtoutage.domain.DtMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DtMasterRepository extends JpaRepository<DtMaster, UUID> {
    Optional<DtMaster> findByDtCode(String dtCode);

    List<DtMaster> findTop20ByActiveTrueAndDtCodeContainingIgnoreCaseOrActiveTrueAndDtNameContainingIgnoreCase(
            String dtCode, String dtName);
}
