package com.gridline.dtoutage.repository;

import com.gridline.dtoutage.domain.DtMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DtMasterRepository extends JpaRepository<DtMaster, UUID> {
    Optional<DtMaster> findByDtCode(String dtCode);

    List<DtMaster> findAllByOrderByDtCodeAsc();

    @Query("select d from DtMaster d where d.active = true and (" +
            "lower(d.dtCode) like lower(concat('%', :q, '%')) or " +
            "lower(d.dtName) like lower(concat('%', :q, '%')) or " +
            "lower(d.businessUnit) like lower(concat('%', :q, '%')) or " +
            "lower(d.undertaking) like lower(concat('%', :q, '%')) or " +
            "lower(d.feeder) like lower(concat('%', :q, '%'))) " +
            "order by d.dtCode asc")
    List<DtMaster> searchActive(@Param("q") String query, org.springframework.data.domain.Pageable pageable);
}
