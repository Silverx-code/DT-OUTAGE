package com.gridline.dtoutage.repository;

import com.gridline.dtoutage.domain.FaultCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaultCategoryRepository extends JpaRepository<FaultCategory, Integer> {
    List<FaultCategory> findByActiveTrueOrderBySortOrder();
}
