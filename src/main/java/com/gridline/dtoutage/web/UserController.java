package com.gridline.dtoutage.web;

import com.gridline.dtoutage.domain.Role;
import com.gridline.dtoutage.domain.User;
import com.gridline.dtoutage.repository.UserRepository;
import com.gridline.dtoutage.web.dto.CreateUserRequest;
import com.gridline.dtoutage.web.dto.UserSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserSummaryResponse> list() {
        return userRepository.findAllByOrderByFullNameAsc().stream()
                .map(UserSummaryResponse::from)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserSummaryResponse create(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {
        boolean isSuperAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_SUPERADMIN"));

        if (!isSuperAdmin && request.role() != Role.USER && request.role() != Role.PAT) {
            throw new AccessDeniedException("Only a SuperAdmin can create Admin or SuperAdmin users.");
        }
        if (userRepository.findByAuthId(request.authId()).isPresent()) {
            throw new IllegalArgumentException("A user with this Entra Object ID already exists.");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("A user with this email already exists.");
        }

        User user = userRepository.save(User.builder()
                .authId(request.authId())
                .fullName(request.fullName())
                .email(request.email())
                .role(request.role())
                .businessUnit(request.businessUnit())
                .active(true)
                .build());
        return UserSummaryResponse.from(user);
    }
}
