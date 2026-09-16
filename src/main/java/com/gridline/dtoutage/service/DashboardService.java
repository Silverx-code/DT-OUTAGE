package com.gridline.dtoutage.service;

import com.gridline.dtoutage.domain.DtOutage;
import com.gridline.dtoutage.domain.OutageStatus;
import com.gridline.dtoutage.repository.DtOutageRepository;
import com.gridline.dtoutage.web.dto.AgeingBucketResponse;
import com.gridline.dtoutage.web.dto.DashboardSummaryResponse;
import com.gridline.dtoutage.web.dto.DtOutageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final List<String> BUCKET_ORDER = List.of(
            "0-2 days", "3-5 days", "6-12 days", "13-25 days", "26-30 days", "1-2 months", "More than 2 months");

    private final DtOutageRepository outageRepository;

    public DashboardSummaryResponse buildSummary(String businessUnit) {
        List<DtOutage> active = businessUnit == null
                ? outageRepository.findByStatusOrderByOutageDateDescOutageTimeDesc(OutageStatus.OUT)
                : outageRepository.findByBusinessUnitSnapshotAndStatusOrderByOutageDateDescOutageTimeDesc(
                        businessUnit, OutageStatus.OUT);

        List<DtOutageResponse> activeResponses = active.stream()
                .map(DtOutageResponse::from)
                .sorted(Comparator.comparingLong(DtOutageResponse::ageDays).reversed())
                .toList();

        LocalDate today = LocalDate.now();
        long restoredToday = outageRepository.findByStatusOrderByOutageDateDescOutageTimeDesc(OutageStatus.RESTORED)
                .stream()
                .filter(o -> o.getRestorationDate() != null && o.getRestorationDate().isEqual(today))
                .count();

        Map<String, Long> bucketCounts = new LinkedHashMap<>();
        BUCKET_ORDER.forEach(b -> bucketCounts.put(b, 0L));
        activeResponses.forEach(o -> bucketCounts.merge(o.ageingBucket(), 1L, Long::sum));

        List<AgeingBucketResponse> buckets = bucketCounts.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(e -> new AgeingBucketResponse(e.getKey(), e.getValue()))
                .toList();

        DtOutageResponse oldest = activeResponses.stream().findFirst().orElse(null);

        return new DashboardSummaryResponse(
                active.size(),
                restoredToday,
                0.0, // TODO: compute vs. yesterday once historical rollups exist
                oldest != null ? oldest.ageDays() : 0,
                oldest != null ? oldest.dtCodeSnapshot() : "-",
                buckets,
                activeResponses
        );
    }
}
