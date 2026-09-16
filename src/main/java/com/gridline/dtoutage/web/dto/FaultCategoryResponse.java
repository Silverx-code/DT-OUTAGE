package com.gridline.dtoutage.web.dto;

import com.gridline.dtoutage.domain.FaultCategory;

public record FaultCategoryResponse(Integer categoryId, String categoryName) {
    public static FaultCategoryResponse from(FaultCategory c) {
        return new FaultCategoryResponse(c.getCategoryId(), c.getCategoryName());
    }
}
