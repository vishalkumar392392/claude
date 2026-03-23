package com.vishal.security.filter;

import com.vishal.security.entity.Role;
import com.vishal.security.entity.User;
import com.vishal.security.service.JwtService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter.
 * Uses Spring mock request/response objects to invoke doFilter without a full server.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noAuthorizationHeader_passesThrough_noAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/data");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull(); // filter chain was called
    }

    @Test
    void authorizationHeaderWithoutBearerPrefix_passesThrough_noAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/data");
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService);
    }

    @Test
    void validBearerToken_authenticatesUser() throws Exception {
        User user = new User("alice", "pass", Set.of(Role.ROLE_USER));
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("alice");
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(user);
        when(jwtService.isTokenValid("valid.jwt.token", user)).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/data");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("alice");
    }

    @Test
    void invalidJwtToken_passesThrough_noAuthentication() throws Exception {
        when(jwtService.extractUsername("bad.token")).thenThrow(new JwtException("Invalid token"));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/data");
        request.addHeader("Authorization", "Bearer bad.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void validTokenButTokenNotValid_doesNotAuthenticate() throws Exception {
        User user = new User("alice", "pass", Set.of(Role.ROLE_USER));
        when(jwtService.extractUsername("mismatched.token")).thenReturn("alice");
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(user);
        when(jwtService.isTokenValid("mismatched.token", user)).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/data");
        request.addHeader("Authorization", "Bearer mismatched.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void userNotFound_passesThrough_noAuthentication() throws Exception {
        when(jwtService.extractUsername("unknown.user.token")).thenReturn("ghost");
        when(userDetailsService.loadUserByUsername("ghost"))
                .thenThrow(new UsernameNotFoundException("User not found: ghost"));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/data");
        request.addHeader("Authorization", "Bearer unknown.user.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }
}
