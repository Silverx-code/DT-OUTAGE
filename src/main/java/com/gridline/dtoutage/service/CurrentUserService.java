package com.gridline.dtoutage.service;
import com.gridline.dtoutage.domain.User;
import com.gridline.dtoutage.exception.ResourceNotFoundException;
import com.gridline.dtoutage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.UUID;
@Service @RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository users;
    public User getOrCreateCurrentUser() {
        String subject = SecurityContextHolder.getContext().getAuthentication().getName();
        try { return users.findById(UUID.fromString(subject)).orElseThrow(); }
        catch (Exception ignored) { return users.findByEmailIgnoreCase(subject).orElseThrow(() -> new ResourceNotFoundException("Current user not found.")); }
    }
}
