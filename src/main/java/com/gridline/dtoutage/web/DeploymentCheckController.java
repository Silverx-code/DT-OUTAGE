package com.gridline.dtoutage.web;

import com.gridline.dtoutage.service.CurrentUserService;
import com.gridline.dtoutage.web.dto.UserSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Temporary deployment verification endpoint; remove after Render is confirmed. */
@RestController
@RequiredArgsConstructor
public class DeploymentCheckController {

    private final CurrentUserService currentUserService;

    @GetMapping("/deployment-check")
    public Map<String, String> check() {
        return Map.of(
                "status", "ok",
                "marker", "gridline-backend-deploy-check-20260919-v2",
                "meRoute", "enabled"
        );
    }

    @GetMapping("/me")
    public UserSummaryResponse me() {
        return UserSummaryResponse.from(currentUserService.getOrCreateCurrentUser());
    }
}
