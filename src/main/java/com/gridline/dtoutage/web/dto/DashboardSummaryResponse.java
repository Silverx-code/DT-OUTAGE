package com.gridline.dtoutage.web.dto;

import java.util.List;

public record DashboardSummaryResponse(
        long outNow,
        long restoredToday,
        double restoredTodayDeltaPct,
        long oldestOutageAgeDays,
        String oldestOutageDtCode,
        List<AgeingBucketResponse> ageingBuckets,
        List<DtOutageResponse> activeOutages
) {}
