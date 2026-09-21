package com.gridline.dtoutage.web;
import com.gridline.dtoutage.service.AuthenticationService;
import com.gridline.dtoutage.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService auth;
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }
    @PostMapping("/forgot-password") public void forgot(@Valid @RequestBody ForgotPasswordRequest request) { auth.requestReset(request.email()); }
    @PostMapping("/reset-password") public void reset(@Valid @RequestBody ResetPasswordRequest request) { auth.reset(request); }
}
