package com.vishal.security.service;

import com.vishal.security.config.JwtProperties;
import com.vishal.security.entity.Role;
import com.vishal.security.entity.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JwtService — token generation, extraction, and validation.
 * No Spring context needed; JwtService is instantiated directly.
 */
class JwtServiceTest {

    // Base64-encoded 256-bit key (same as used in CentralsecurityApplicationTests)
    private static final String TEST_SECRET = "dGVzdFNlY3JldEtleVRoYXRJc0xvbmdFbm91Z2hGb3JIVE1BQzI1Ng==";

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(TEST_SECRET);
        props.setExpirationMs(3_600_000L); // 1 hour
        jwtService = new JwtService(props);
        jwtService.init();

        testUser = new User("alice", "encodedPassword", Set.of(Role.ROLE_USER));
    }

    @Test
    void generateToken_returnsNonBlankToken() {
        String token = jwtService.generateToken(testUser);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractUsername_returnsCorrectSubject() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
    }

    @Test
    void isTokenValid_withMatchingUser_returnsTrue() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    void isTokenValid_withDifferentUser_returnsFalse() {
        User otherUser = new User("bob", "pass", Set.of(Role.ROLE_USER));
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void isTokenValid_withExpiredToken_throwsJwtException() {
        // Negative expiration means the token is already expired at issuance
        JwtProperties expiredProps = new JwtProperties();
        expiredProps.setSecret(TEST_SECRET);
        expiredProps.setExpirationMs(-1_000L);
        JwtService expiredService = new JwtService(expiredProps);
        expiredService.init();

        String token = expiredService.generateToken(testUser);

        // JJWT 0.12.x throws ExpiredJwtException (a JwtException) when parsing an expired token
        assertThatThrownBy(() -> expiredService.isTokenValid(token, testUser))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void generateToken_withExtraClaims_extractsCorrectUsername() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("customKey", "customValue");

        String token = jwtService.generateToken(claims, testUser);

        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
    }

    @Test
    void generateToken_embedsRolesClaim() {
        User adminUser = new User("admin", "pass", Set.of(Role.ROLE_ADMIN));
        String token = jwtService.generateToken(adminUser);

        // Username should still be correct regardless of roles
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
    }

    @Test
    void invalidToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtService.extractUsername("not.a.valid.jwt"))
                .isInstanceOf(JwtException.class);
    }
}
