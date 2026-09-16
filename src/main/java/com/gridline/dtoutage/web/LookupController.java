package com.gridline.dtoutage.web;

import com.gridline.dtoutage.repository.ChallengeCategoryRepository;
import com.gridline.dtoutage.repository.FaultCategoryRepository;
import com.gridline.dtoutage.web.dto.ChallengeCategoryResponse;
import com.gridline.dtoutage.web.dto.FaultCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lookups")
@RequiredArgsConstructor
public class LookupController {

    private final FaultCategoryRepository faultCategoryRepository;
    private final ChallengeCategoryRepository challengeCategoryRepository;

    @GetMapping("/fault-categories")
    @PreAuthorize("hasRole('USER')")
    public List<FaultCategoryResponse> faultCategories() {
        return faultCategoryRepository.findByActiveTrueOrderBySortOrder().stream()
                .map(FaultCategoryResponse::from)
                .toList();
    }

    @GetMapping("/challenge-categories")
    @PreAuthorize("hasRole('USER')")
    public List<ChallengeCategoryResponse> challengeCategories() {
        return challengeCategoryRepository.findByActiveTrueOrderBySortOrder().stream()
                .map(ChallengeCategoryResponse::from)
                .toList();
    }
}
