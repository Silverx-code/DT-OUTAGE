package com.gridline.dtoutage.service;
import com.gridline.dtoutage.domain.*;
import com.gridline.dtoutage.repository.*;
import com.gridline.dtoutage.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository users; private final PasswordResetTokenRepository tokens; private final PasswordEncoder encoder;
    private final JwtEncoder jwtEncoder;
    @Value("${app.security.jwt-expiry-minutes:480}") private long jwtMinutes;
    @Transactional public AuthResponse login(LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email().trim()).orElseThrow(() -> new BadCredentialsException("Invalid email or password."));
        if (!user.isActive() || user.getPasswordHash() == null || !encoder.matches(request.password(), user.getPasswordHash())) throw new BadCredentialsException("Invalid email or password.");
        return response(user, issueToken(user));
    }
    @Transactional public void requestReset(String email) {
        users.findByEmailIgnoreCase(email.trim()).filter(User::isActive).ifPresent(user -> { try {
            tokens.deleteByUser_UserId(user.getUserId()); String raw = randomToken();
            tokens.save(PasswordResetToken.builder().tokenHash(hash(raw)).user(user).expiresAt(Instant.now().plus(Duration.ofMinutes(30))).build());
        } catch (RuntimeException ignored) { /* keep the response indistinguishable */ } });
    }
    @Transactional public void reset(ResetPasswordRequest request) {
        PasswordResetToken token = tokens.findByTokenHash(hash(request.token())).filter(t -> t.getUsedAt() == null && t.getExpiresAt().isAfter(Instant.now())).orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link."));
        validatePassword(request.password()); User user = token.getUser(); user.setPasswordHash(encoder.encode(request.password())); user.setPasswordChangedAt(Instant.now()); user.setForcePasswordChange(false); users.save(user); token.setUsedAt(Instant.now()); tokens.save(token);
    }
    public String adminReset(UUID userId) {
        User user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
        tokens.deleteByUser_UserId(user.getUserId()); String raw = randomToken();
        tokens.save(PasswordResetToken.builder().tokenHash(hash(raw)).user(user).expiresAt(Instant.now().plus(Duration.ofMinutes(30))).build());
        return raw;
    }
    public void validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 128 || password.chars().noneMatch(Character::isUpperCase) || password.chars().noneMatch(Character::isLowerCase) || password.chars().noneMatch(Character::isDigit)) throw new IllegalArgumentException("Password must be 8-128 characters and include upper, lower, and numeric characters.");
    }
    private AuthResponse response(User u, String token) { return new AuthResponse(token, u.getEmail(), u.getFullName(), u.getRole(), u.isForcePasswordChange()); }
    private String issueToken(User u) { Instant now = Instant.now(); JwtClaimsSet claims = JwtClaimsSet.builder().issuer("gridline").subject(u.getUserId().toString()).issuedAt(now).expiresAt(now.plus(Duration.ofMinutes(jwtMinutes))).claim("email", u.getEmail()).build(); return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build(), claims)).getTokenValue(); }
    private String randomToken() { byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes); return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    private String hash(String raw) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8))); } catch (GeneralSecurityException e) { throw new IllegalStateException(e); } }
}
