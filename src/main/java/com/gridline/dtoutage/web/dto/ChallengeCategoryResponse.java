package com.gridline.dtoutage.web.dto;

import com.gridline.dtoutage.domain.ChallengeCategory;

public record ChallengeCategoryResponse(Integer challengeId, String challengeName) {
    public static ChallengeCategoryResponse from(ChallengeCategory c) {
        return new ChallengeCategoryResponse(c.getChallengeId(), c.getChallengeName());
    }
}
