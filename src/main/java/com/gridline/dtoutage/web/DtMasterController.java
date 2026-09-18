package com.gridline.dtoutage.web;

import com.gridline.dtoutage.domain.DtMaster;
import com.gridline.dtoutage.exception.ResourceNotFoundException;
import com.gridline.dtoutage.repository.DtMasterRepository;
import com.gridline.dtoutage.web.dto.DtMasterResponse;
import com.gridline.dtoutage.web.dto.DtMasterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;

@RestController
@RequestMapping("/dt-master")
@RequiredArgsConstructor
public class DtMasterController {

    private final DtMasterRepository dtMasterRepository;

    /** Browse the active transformer master list for all authenticated users. */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<DtMasterResponse> allActive() {
        return dtMasterRepository.findAllByOrderByDtCodeAsc().stream()
                .filter(DtMaster::isActive)
                .map(DtMasterResponse::from)
                .toList();
    }

    /** Typeahead search backing the "Search DT by code, street or feeder" field on Report Outage. */
    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public List<DtMasterResponse> search(@RequestParam String q) {
        return dtMasterRepository.searchActive(q, PageRequest.of(0, 20))
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

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DtMasterResponse> allForAdmin() {
        return dtMasterRepository.findAllByOrderByDtCodeAsc().stream()
                .map(DtMasterResponse::from).toList();
    }

    // DT Master is immutable to normal users by design (Section 7 of the
    // design doc) — only SuperAdmin can create/edit entries.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public DtMasterResponse create(@Valid @RequestBody DtMasterRequest request) {
        DtMaster dtMaster = DtMaster.builder()
                .dtCode(request.dtCode()).dtName(request.dtName()).businessUnit(request.businessUnit())
                .undertaking(request.undertaking()).feeder(request.feeder()).capacityKva(request.capacityKva())
                .supplyBand(request.supplyBand()).active(request.active() == null || request.active()).build();
        return DtMasterResponse.from(dtMasterRepository.save(dtMaster));
    }

    @PutMapping("/{dtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public DtMasterResponse update(@PathVariable UUID dtId, @Valid @RequestBody DtMasterRequest updated) {
        DtMaster existing = dtMasterRepository.findById(dtId)
                .orElseThrow(() -> new ResourceNotFoundException("DT not found: " + dtId));
        existing.setDtCode(updated.dtCode());
        existing.setDtName(updated.dtName());
        existing.setBusinessUnit(updated.businessUnit());
        existing.setUndertaking(updated.undertaking());
        existing.setFeeder(updated.feeder());
        existing.setCapacityKva(updated.capacityKva());
        existing.setSupplyBand(updated.supplyBand());
        existing.setActive(updated.active() == null || updated.active());
        return DtMasterResponse.from(dtMasterRepository.save(existing));
    }
}
