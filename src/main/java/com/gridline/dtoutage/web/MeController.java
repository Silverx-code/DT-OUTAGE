package com.gridline.dtoutage.web;

import com.gridline.dtoutage.service.CurrentUserService;
import com.gridline.dtoutage.web.dto.UserSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MeController {

    private final CurrentUserService currentUserService;

    /** Frontend calls this right after SSO login to know the caller's role/BU/active status. */
    @GetMapping("/me")
    public UserSummaryResponse me() {
        return UserSummaryResponse.from(currentUserService.getOrCreateCurrentUser());
    }
}
