package com.gridline.dtoutage.repository;

import com.gridline.dtoutage.domain.ChallengeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeCategoryRepository extends JpaRepository<ChallengeCategory, Integer> {
    List<ChallengeCategory> findByActiveTrueOrderBySortOrder();
}
