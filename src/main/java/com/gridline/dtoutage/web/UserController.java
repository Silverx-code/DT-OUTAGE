package com.gridline.dtoutage.web;

import com.gridline.dtoutage.domain.Role;
import com.gridline.dtoutage.domain.User;
import com.gridline.dtoutage.exception.ResourceNotFoundException;
import com.gridline.dtoutage.repository.UserRepository;
import com.gridline.dtoutage.web.dto.CreateUserRequest;
import com.gridline.dtoutage.web.dto.UserSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
        if (userRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new IllegalArgumentException("A user with this email already exists.");
        }

        User user = userRepository.save(User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .role(request.role())
                .businessUnit(request.businessUnit())
                .active(true)
                .build());
        return UserSummaryResponse.from(user);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserSummaryResponse remove(
            @PathVariable UUID userId,
            Authentication authentication) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        boolean isSuperAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_SUPERADMIN"));

        if (authentication.getName().equalsIgnoreCase(target.getEmail())) {
            throw new AccessDeniedException("You cannot remove your own account.");
        }
        if (!isSuperAdmin && target.getRole() != Role.USER && target.getRole() != Role.PAT) {
            throw new AccessDeniedException("Only a SuperAdmin can remove Admin or SuperAdmin users.");
        }

        // Keep the row and its outage/audit references; removal revokes access.
        target.setActive(false);
        return UserSummaryResponse.from(userRepository.save(target));
    }
}
