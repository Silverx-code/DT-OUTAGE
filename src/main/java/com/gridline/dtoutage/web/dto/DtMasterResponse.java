package com.gridline.dtoutage.web.dto;

import com.gridline.dtoutage.domain.DtMaster;

import java.math.BigDecimal;
import java.util.UUID;

public record DtMasterResponse(
        UUID dtId,
        String dtCode,
        String dtName,
        String businessUnit,
        String undertaking,
        String feeder,
        BigDecimal capacityKva,
        String supplyBand,
        boolean isActive
) {
    public static DtMasterResponse from(DtMaster dt) {
        return new DtMasterResponse(
                dt.getDtId(), dt.getDtCode(), dt.getDtName(), dt.getBusinessUnit(),
                dt.getUndertaking(), dt.getFeeder(), dt.getCapacityKva(), dt.getSupplyBand(), dt.isActive());
    }
}
