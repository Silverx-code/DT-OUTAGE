package com.gridline.dtoutage.web.dto;

import com.gridline.dtoutage.domain.DtOutage;
import com.gridline.dtoutage.domain.OutageStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record DtOutageResponse(
        UUID outageId,
        String outageRef,
        UUID dtId,
        String dtCodeSnapshot,
        String businessUnitSnapshot,
        String undertakingSnapshot,
        String feederSnapshot,
        BigDecimal capacitySnapshot,
        String bandSnapshot,
        Instant outageDatetime,
        String faultCategory,
        String faultDescription,
        String restorationChallenge,
        String additionalComment,
        OutageStatus status,
        Instant restorationDatetime,
        String restorationRemarks,
        Integer outageDurationMinutes,
        long ageDays,
        String ageingBucket,
        String reportedByName,
        String restoredByName
) {

    public static DtOutageResponse from(DtOutage o) {
        Instant referenceEnd = o.getStatus() == OutageStatus.OUT ? Instant.now() : o.getRestorationDatetime();
        long ageDays = referenceEnd == null ? 0 : ChronoUnit.DAYS.between(o.getOutageDatetime(), referenceEnd);

        return new DtOutageResponse(
                o.getOutageId(),
                o.getOutageRef(),
                o.getDt().getDtId(),
                o.getDtCodeSnapshot(),
                o.getBusinessUnitSnapshot(),
                o.getUndertakingSnapshot(),
                o.getFeederSnapshot(),
                o.getCapacitySnapshot(),
                o.getBandSnapshot(),
                o.getOutageDatetime(),
                o.getFaultCategory().getCategoryName(),
                o.getFaultDescription(),
                o.getChallenge() != null ? o.getChallenge().getChallengeName() : null,
                o.getAdditionalComment(),
                o.getStatus(),
                o.getRestorationDatetime(),
                o.getRestorationRemarks(),
                o.getOutageDurationMinutes(),
                ageDays,
                ageingBucket(ageDays),
                o.getReportedBy().getFullName(),
                o.getRestoredBy() != null ? o.getRestoredBy().getFullName() : null
        );
    }

    private static String ageingBucket(long ageDays) {
        if (ageDays <= 2) return "0-2 days";
        if (ageDays <= 5) return "3-5 days";
        if (ageDays <= 12) return "6-12 days";
        if (ageDays <= 25) return "13-25 days";
        if (ageDays <= 30) return "26-30 days";
        if (ageDays <= 60) return "1-2 months";
        return "More than 2 months";
    }
}
