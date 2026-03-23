package com.vishal.security.service;

import com.vishal.security.config.JwtProperties;
import com.vishal.security.entity.RefreshToken;
import com.vishal.security.entity.Role;
import com.vishal.security.entity.User;
import com.vishal.security.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RefreshTokenService using Mockito.
 * Covers token creation, lookup, expiration verification, and revocation.
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;
    private User testUser;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setRefreshTokenExpirationMs(604_800_000L); // 7 days
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, props);
        testUser = new User("alice", "encodedPassword", Set.of(Role.ROLE_USER));
    }

    @Test
    void createRefreshToken_savesTokenWithCorrectUser() {
        RefreshToken saved = new RefreshToken("uuid-token", testUser, Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(saved);

        RefreshToken result = refreshTokenService.createRefreshToken(testUser);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("uuid-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void findByToken_delegatesToRepository() {
        RefreshToken token = new RefreshToken("my-token", testUser, Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.findByToken("my-token")).thenReturn(Optional.of(token));

        Optional<RefreshToken> result = refreshTokenService.findByToken("my-token");

        assertThat(result).isPresent().contains(token);
    }

    @Test
    void findByToken_notFound_returnsEmpty() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        Optional<RefreshToken> result = refreshTokenService.findByToken("missing");

        assertThat(result).isEmpty();
    }

    @Test
    void verifyExpiration_nonExpiredToken_returnsToken() {
        RefreshToken token = new RefreshToken("valid", testUser, Instant.now().plusSeconds(3600));

        RefreshToken result = refreshTokenService.verifyExpiration(token);

        assertThat(result).isSameAs(token);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void verifyExpiration_expiredToken_deletesAndThrows() {
        RefreshToken expired = new RefreshToken("old", testUser, Instant.now().minusSeconds(1));

        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(expired))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Refresh token expired");

        verify(refreshTokenRepository).delete(expired);
    }

    @Test
    void revokeAllForUser_callsDeleteByUser() {
        refreshTokenService.revokeAllForUser(testUser);

        verify(refreshTokenRepository).deleteByUser(testUser);
    }

    @Test
    void revokeToken_whenTokenExists_deletesIt() {
        RefreshToken token = new RefreshToken("to-revoke", testUser, Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.findByToken("to-revoke")).thenReturn(Optional.of(token));

        refreshTokenService.revokeToken("to-revoke");

        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void revokeToken_whenTokenNotFound_doesNothing() {
        when(refreshTokenRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

        refreshTokenService.revokeToken("nonexistent");

        verify(refreshTokenRepository, never()).delete(any());
    }
}
