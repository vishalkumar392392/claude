package com.vishal.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vishal.security.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AuthController endpoints (register, login, refresh, logout).
 * Uses H2 in-memory DB — no MySQL instance required.
 */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "jwt.secret=dGVzdFNlY3JldEtleVRoYXRJc0xvbmdFbm91Z2hGb3JIVE1BQzI1Ng==",
    "jwt.expiration-ms=3600000",
    "jwt.public-paths=/api/auth/**",
    "centralsecurity.datasource.url=jdbc:h2:mem:authctrltest;DB_CLOSE_DELAY=-1",
    "centralsecurity.datasource.driver-class-name=org.h2.Driver",
    "centralsecurity.datasource.username=sa",
    "centralsecurity.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // /api/auth/register
    // -------------------------------------------------------------------------

    @Test
    void register_newUser_returns201WithTokens() throws Exception {
        var request = new AuthController.RegisterRequest("user_reg1", "password123", null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void register_withExplicitRoles_returns201() throws Exception {
        var request = new AuthController.RegisterRequest(
                "user_reg2", "password123", java.util.List.of("ROLE_ADMIN"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void register_duplicateUsername_returns409() throws Exception {
        var request = new AuthController.RegisterRequest("user_dup", "password123", null);

        // First registration succeeds
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second registration with same username returns 409 Conflict
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // -------------------------------------------------------------------------
    // /api/auth/login
    // -------------------------------------------------------------------------

    @Test
    void login_validCredentials_returns200WithTokens() throws Exception {
        // Register first
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthController.RegisterRequest("user_login1", "secret", null))))
                .andExpect(status().isCreated());

        // Login with correct credentials
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthController.LoginRequest("user_login1", "secret"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        // Register first
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthController.RegisterRequest("user_login2", "correctpass", null))))
                .andExpect(status().isCreated());

        // Login with wrong password
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthController.LoginRequest("user_login2", "wrongpass"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_unknownUser_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthController.LoginRequest("nobody", "pass"))))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // /api/auth/refresh
    // -------------------------------------------------------------------------

    @Test
    void refresh_validRefreshToken_returnsNewTokens() throws Exception {
        // Register and extract the refresh token
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthController.RegisterRequest("user_refresh1", "pass", null))))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(body).get("refreshToken").asText();

        // Exchange the refresh token for new tokens
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthController.RefreshRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void refresh_invalidRefreshToken_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthController.RefreshRequest("not-a-real-token"))))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // /api/auth/logout
    // -------------------------------------------------------------------------

    @Test
    void logout_validRefreshToken_returns200() throws Exception {
        // Register and get tokens
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthController.RegisterRequest("user_logout1", "pass", null))))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(body).get("refreshToken").asText();

        // Logout
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthController.RefreshRequest(refreshToken))))
                .andExpect(status().isOk());
    }

    @Test
    void logout_withInvalidToken_stillReturns200() throws Exception {
        // Logout is idempotent — an unrecognised token just does nothing
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthController.RefreshRequest("unknown-token"))))
                .andExpect(status().isOk());
    }

    @Test
    void refreshToken_afterLogout_returns401() throws Exception {
        // Register
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthController.RegisterRequest("user_logout2", "pass", null))))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(body).get("refreshToken").asText();

        // Logout revokes all tokens
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthController.RefreshRequest(refreshToken))))
                .andExpect(status().isOk());

        // Attempting refresh with the revoked token now returns 401
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthController.RefreshRequest(refreshToken))))
                .andExpect(status().isUnauthorized());
    }
}
