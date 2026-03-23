package com.vishal.security.entity;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the User entity, focusing on the UserDetails contract and getAuthorities().
 */
class UserTest {

    @Test
    void getAuthorities_singleRole_returnsMappedAuthority() {
        User user = new User("alice", "pass", Set.of(Role.ROLE_USER));

        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void getAuthorities_multipleRoles_returnsAllAuthorities() {
        User user = new User("admin", "pass", Set.of(Role.ROLE_USER, Role.ROLE_ADMIN));

        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void newUser_defaultAccountStatusIsActive() {
        User user = new User("alice", "pass", Set.of(Role.ROLE_USER));

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
    }

    @Test
    void getters_returnConstructorValues() {
        Set<Role> roles = Set.of(Role.ROLE_USER);
        User user = new User("bob", "secret", roles);

        assertThat(user.getUsername()).isEqualTo("bob");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.getRoles()).containsExactlyInAnyOrderElementsOf(roles);
    }

    @Test
    void setters_updateFieldsCorrectly() {
        User user = new User();
        user.setUsername("charlie");
        user.setPassword("newpass");
        user.setRoles(Set.of(Role.ROLE_ADMIN));
        user.setEnabled(false);
        user.setAccountNonExpired(false);
        user.setCredentialsNonExpired(false);
        user.setAccountNonLocked(false);

        assertThat(user.getUsername()).isEqualTo("charlie");
        assertThat(user.getPassword()).isEqualTo("newpass");
        assertThat(user.getRoles()).containsExactly(Role.ROLE_ADMIN);
        assertThat(user.isEnabled()).isFalse();
        assertThat(user.isAccountNonExpired()).isFalse();
        assertThat(user.isCredentialsNonExpired()).isFalse();
        assertThat(user.isAccountNonLocked()).isFalse();
    }
}
