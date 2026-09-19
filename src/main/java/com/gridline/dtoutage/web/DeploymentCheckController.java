package com.gridline.dtoutage.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Temporary deployment verification endpoint; remove after Render is confirmed. */
@RestController
public class DeploymentCheckController {

    @GetMapping("/deployment-check")
    public Map<String, String> check() {
        return Map.of(
                "status", "ok",
                "marker", "gridline-backend-deploy-check-20260919-v1"
        );
    }
}
