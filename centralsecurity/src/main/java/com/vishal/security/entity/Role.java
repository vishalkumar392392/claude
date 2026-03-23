package com.vishal.security.entity;

/**
 * Application roles. The ROLE_ prefix is required by Spring Security's hasRole() expressions.
 * Stored as strings in the user_roles join table via @Enumerated(EnumType.STRING).
 */
public enum Role {
    ROLE_USER,
    ROLE_ADMIN
}
