package com.gridline.dtoutage.web;

import com.gridline.dtoutage.domain.DtOutage;
import com.gridline.dtoutage.domain.OutageStatus;
import com.gridline.dtoutage.service.DtOutageService;
import com.gridline.dtoutage.web.dto.DtOutageResponse;
import com.gridline.dtoutage.web.dto.ReportOutageRequest;
import com.gridline.dtoutage.web.dto.RestoreOutageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/outages")
@RequiredArgsConstructor
public class DtOutageController {

    private final DtOutageService outageService;

    @GetMapping
    @PreAuthorize("hasRole('USER')") // USER is the floor of the hierarchy — everyone authenticated passes
    public List<DtOutageResponse> list(@RequestParam(required = false) OutageStatus status) {
        List<DtOutage> outages = status == null ? outageService.findAll() : outageService.findByStatus(status);
        return outages.stream().map(DtOutageResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DtOutageResponse> report(@Valid @RequestBody ReportOutageRequest request) {
        DtOutage outage = outageService.reportOutage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtOutageResponse.from(outage));
    }

    @PatchMapping("/{outageId}/restore")
    @PreAuthorize("hasRole('USER')")
    public DtOutageResponse restore(
            @PathVariable UUID outageId,
            @Valid @RequestBody RestoreOutageRequest request) {
        DtOutage outage = outageService.restoreOutage(outageId, request);
        return DtOutageResponse.from(outage);
    }
}
