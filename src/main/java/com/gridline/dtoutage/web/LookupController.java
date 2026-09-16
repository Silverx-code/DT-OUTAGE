package com.gridline.dtoutage.web;

import com.gridline.dtoutage.repository.ChallengeCategoryRepository;
import com.gridline.dtoutage.repository.FaultCategoryRepository;
import com.gridline.dtoutage.web.dto.ChallengeCategoryResponse;
import com.gridline.dtoutage.web.dto.FaultCategoryResponse;
import com.gridline.dtoutage.web.dto.LookupCategoryRequest;
import jakarta.validation.Valid;
import com.gridline.dtoutage.domain.FaultCategory;
import com.gridline.dtoutage.domain.ChallengeCategory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/admin/fault-categories")
    @PreAuthorize("hasRole('ADMIN')")
    public List<FaultCategoryResponse> allFaultCategories() {
        return faultCategoryRepository.findAllByOrderBySortOrderAscCategoryNameAsc().stream().map(FaultCategoryResponse::from).toList();
    }

    @PostMapping("/fault-categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FaultCategoryResponse> createFaultCategory(@Valid @RequestBody LookupCategoryRequest request) {
        FaultCategory category = faultCategoryRepository.save(FaultCategory.builder().categoryName(request.name().trim())
                .sortOrder(request.sortOrder() == null ? 0 : request.sortOrder()).active(request.active() == null || request.active()).build());
        return ResponseEntity.status(HttpStatus.CREATED).body(FaultCategoryResponse.from(category));
    }

    @PutMapping("/fault-categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public FaultCategoryResponse updateFaultCategory(@PathVariable Integer id, @Valid @RequestBody LookupCategoryRequest request) {
        FaultCategory category = faultCategoryRepository.findById(id).orElseThrow();
        category.setCategoryName(request.name().trim()); category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setActive(request.active() == null || request.active());
        return FaultCategoryResponse.from(faultCategoryRepository.save(category));
    }

    @GetMapping("/admin/challenge-categories")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ChallengeCategoryResponse> allChallengeCategories() {
        return challengeCategoryRepository.findAllByOrderBySortOrderAscChallengeNameAsc().stream().map(ChallengeCategoryResponse::from).toList();
    }

    @PostMapping("/challenge-categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChallengeCategoryResponse> createChallengeCategory(@Valid @RequestBody LookupCategoryRequest request) {
        ChallengeCategory category = challengeCategoryRepository.save(ChallengeCategory.builder().challengeName(request.name().trim())
                .sortOrder(request.sortOrder() == null ? 0 : request.sortOrder()).active(request.active() == null || request.active()).build());
        return ResponseEntity.status(HttpStatus.CREATED).body(ChallengeCategoryResponse.from(category));
    }

    @PutMapping("/challenge-categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ChallengeCategoryResponse updateChallengeCategory(@PathVariable Integer id, @Valid @RequestBody LookupCategoryRequest request) {
        ChallengeCategory category = challengeCategoryRepository.findById(id).orElseThrow();
        category.setChallengeName(request.name().trim()); category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setActive(request.active() == null || request.active());
        return ChallengeCategoryResponse.from(challengeCategoryRepository.save(category));
    }
}
