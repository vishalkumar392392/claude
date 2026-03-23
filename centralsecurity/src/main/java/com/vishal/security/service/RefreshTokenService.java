package com.vishal.security.service;

import com.vishal.security.config.JwtProperties;
import com.vishal.security.entity.RefreshToken;
import com.vishal.security.entity.User;
import com.vishal.security.repository.RefreshTokenRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages refresh token lifecycle: creation, validation, rotation, and revocation.
 * Declared as a bean in SecurityAutoConfiguration — not annotated with @Service.
 */
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    /** Create and persist a new refresh token for the given user. */
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID().toString(),
                user,
                Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs())
        );
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Verify the token is not expired.
     * If expired, deletes it from the DB and throws so the client must re-login.
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired. Please log in again.");
        }
        return token;
    }

    /** Revoke all refresh tokens for a user (called on logout). */
    @Transactional
    public void revokeAllForUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    /** Revoke a single refresh token by its value. */
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }
}
