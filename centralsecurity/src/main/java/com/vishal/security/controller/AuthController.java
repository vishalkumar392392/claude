package com.vishal.security.controller;

import com.vishal.security.entity.RefreshToken;
import com.vishal.security.entity.Role;
import com.vishal.security.entity.User;
import com.vishal.security.repository.UserRepository;
import com.vishal.security.service.JwtService;
import com.vishal.security.service.RefreshTokenService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Exposes auth endpoints.
 * Enabled by default; disable via: jwt.auth-endpoints-enabled=false
 *
 * POST /api/auth/login    — authenticate and receive access + refresh tokens
 * POST /api/auth/register — create a new user and receive access + refresh tokens
 * POST /api/auth/refresh  — exchange a refresh token for new access + refresh tokens
 * POST /api/auth/logout   — revoke all refresh tokens for the authenticated user
 */
@RestController
@RequestMapping("/api/auth")
@ConditionalOnProperty(prefix = "jwt", name = "auth-endpoints-enabled", havingValue = "true", matchIfMissing = true)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Authenticate an existing user and return access + refresh tokens.
     *
     * Request:  { "username": "alice", "password": "secret" }
     * Response: { "accessToken": "eyJ...", "refreshToken": "uuid" }
     */
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            User user = (User) authentication.getPrincipal();
            String accessToken = jwtService.generateToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken.getToken()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    /**
     * Register a new user and return access + refresh tokens.
     *
     * Request:  { "username": "alice", "password": "secret", "roles": ["ROLE_USER"] }
     * Response: { "accessToken": "eyJ...", "refreshToken": "uuid" }
     */
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Username already exists: " + request.username());
        }

        Set<Role> roles = request.roles() != null && !request.roles().isEmpty()
                ? request.roles().stream()
                    .map(r -> Role.valueOf(r.toUpperCase()))
                    .collect(Collectors.toSet())
                : Set.of(Role.ROLE_USER);

        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                roles);

        userRepository.save(user);
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(accessToken, refreshToken.getToken()));
    }

    /**
     * Exchange a valid refresh token for a new access token + rotated refresh token.
     * The old refresh token is deleted (rotation prevents replay attacks).
     *
     * Request:  { "refreshToken": "uuid" }
     * Response: { "accessToken": "eyJ...", "refreshToken": "new-uuid" }
     */
    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@RequestBody RefreshRequest request) {
        return refreshTokenService.findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(oldToken -> {
                    User user = oldToken.getUser();
                    refreshTokenService.revokeToken(oldToken.getToken());
                    String newAccessToken = jwtService.generateToken(user);
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
                    return ResponseEntity.<Object>ok(new AuthResponse(newAccessToken, newRefreshToken.getToken()));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    /**
     * Revoke all refresh tokens for the user identified by the given refresh token.
     * The client should discard stored tokens after calling this endpoint.
     *
     * Request:  { "refreshToken": "uuid" }
     * Response: 200 OK
     */
    @PostMapping("/logout")
    public ResponseEntity<Object> logout(@RequestBody RefreshRequest request) {
        refreshTokenService.findByToken(request.refreshToken())
                .ifPresent(token -> refreshTokenService.revokeAllForUser(token.getUser()));
        return ResponseEntity.ok("Logged out successfully");
    }

    // -------------------------------------------------------------------------
    // DTOs
    // -------------------------------------------------------------------------

    public record LoginRequest(String username, String password) {}

    public record RegisterRequest(String username, String password, java.util.List<String> roles) {}

    public record RefreshRequest(String refreshToken) {}

    public record AuthResponse(String accessToken, String refreshToken) {}
}
