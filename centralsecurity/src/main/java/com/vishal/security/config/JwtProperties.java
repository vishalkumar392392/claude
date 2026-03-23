package com.vishal.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds all jwt.* properties from the consuming service's application.properties.
 * Registered as a bean by SecurityAutoConfiguration — do NOT annotate with @Component.
 *
 * All properties are optional — a hardcoded default signingKey is built in.
 * Override any value in your application.properties using the jwt.* prefix.
 *
 *   jwt.signingKey=<base64-encoded key>        (optional — default key is built in)
 *   jwt.expiration-ms=86400000             (optional — default: 24 hours)
 *   jwt.public-paths=/api/auth/**          (optional)
 *   jwt.auth-endpoints-enabled=true        (optional)
 */
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Base64-encoded HMAC-SHA256 signing key.
     * Hardcoded default — override via jwt.signingKey in application.properties if needed.
     * Generated with: openssl rand -base64 32
     */
    private String signingKey = "dmlzaGFsQ2VudHJhbFNlY3VyaXR5U2VjcmV0S2V5Rm9yQWxsTWljcm9zZXJ2aWNlczIwMjY=";

    /**
     * Access token lifetime in milliseconds. Default: 86400000 (24 hours).
     */
    private long expirationMs = 86_400_000L;

    /**
     * Refresh token lifetime in milliseconds. Default: 604800000 (7 days).
     */
    private long refreshTokenExpirationMs = 604_800_000L;

    /**
     * URL patterns that bypass JWT validation (no token required).
     * Default: ["/api/auth/**"]
     */
    private List<String> publicPaths = new ArrayList<>(List.of("/api/auth/**"));

    /**
     * Whether to expose /api/auth/login and /api/auth/register endpoints.
     * Set to false if your service manages its own auth endpoints.
     * Default: true
     */
    private boolean authEndpointsEnabled = true;

    public String getSecret() { return signingKey; }
    public void setSecret(String signingKey) { this.signingKey = signingKey; }

    public long getExpirationMs() { return expirationMs; }
    public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }

    public long getRefreshTokenExpirationMs() { return refreshTokenExpirationMs; }
    public void setRefreshTokenExpirationMs(long refreshTokenExpirationMs) { this.refreshTokenExpirationMs = refreshTokenExpirationMs; }

    public List<String> getPublicPaths() { return publicPaths; }
    public void setPublicPaths(List<String> publicPaths) { this.publicPaths = publicPaths; }

    public boolean isAuthEndpointsEnabled() { return authEndpointsEnabled; }
    public void setAuthEndpointsEnabled(boolean authEndpointsEnabled) {
        this.authEndpointsEnabled = authEndpointsEnabled;
    }
}
