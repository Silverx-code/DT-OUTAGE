package com.gridline.dtoutage.web;

import com.gridline.dtoutage.domain.DtMaster;
import com.gridline.dtoutage.exception.ResourceNotFoundException;
import com.gridline.dtoutage.repository.DtMasterRepository;
import com.gridline.dtoutage.web.dto.DtMasterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dt-master")
@RequiredArgsConstructor
public class DtMasterController {

    private final DtMasterRepository dtMasterRepository;

    /** Typeahead search backing the "Search DT by code, street or feeder" field on Report Outage. */
    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public List<DtMasterResponse> search(@RequestParam String q) {
        return dtMasterRepository
                .findTop20ByActiveTrueAndDtCodeContainingIgnoreCaseOrActiveTrueAndDtNameContainingIgnoreCase(q, q)
                .stream()
                .map(DtMasterResponse::from)
                .toList();
    }

    @GetMapping("/{dtId}")
    @PreAuthorize("hasRole('USER')")
    public DtMasterResponse get(@PathVariable UUID dtId) {
        return dtMasterRepository.findById(dtId)
                .map(DtMasterResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("DT not found: " + dtId));
    }

    // DT Master is immutable to normal users by design (Section 7 of the
    // design doc) — only SuperAdmin can create/edit entries.
    @PostMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public DtMasterResponse create(@RequestBody DtMaster dtMaster) {
        return DtMasterResponse.from(dtMasterRepository.save(dtMaster));
    }

    @PutMapping("/{dtId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public DtMasterResponse update(@PathVariable UUID dtId, @RequestBody DtMaster updated) {
        DtMaster existing = dtMasterRepository.findById(dtId)
                .orElseThrow(() -> new ResourceNotFoundException("DT not found: " + dtId));
        existing.setDtName(updated.getDtName());
        existing.setBusinessUnit(updated.getBusinessUnit());
        existing.setUndertaking(updated.getUndertaking());
        existing.setFeeder(updated.getFeeder());
        existing.setCapacityKva(updated.getCapacityKva());
        existing.setSupplyBand(updated.getSupplyBand());
        existing.setActive(updated.isActive());
        return DtMasterResponse.from(dtMasterRepository.save(existing));
    }
}
