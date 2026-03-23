package com.vishal.security;

import com.vishal.security.config.SecurityAutoConfiguration;
import com.vishal.security.filter.JwtAuthenticationFilter;
import com.vishal.security.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test: verifies that SecurityAutoConfiguration wires all beans
 * correctly when jwt.secret is provided. Uses H2 in-memory DB (no MySQL needed).
 */
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(properties = {
    "jwt.secret=dGVzdFNlY3JldEtleVRoYXRJc0xvbmdFbm91Z2hGb3JIVE1BQzI1Ng==",
    "jwt.expiration-ms=3600000",
    "jwt.public-paths=/api/auth/**",
    // Override the centralized MySQL datasource with H2 for tests (no MySQL required)
    "centralsecurity.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "centralsecurity.datasource.driver-class-name=org.h2.Driver",
    "centralsecurity.datasource.username=sa",
    "centralsecurity.datasource.password=",
    // Override the ddl-auto default (update→create-drop) to keep test DB clean
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CentralsecurityApplicationTests {

    @Autowired
    private SecurityAutoConfiguration securityAutoConfiguration;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    void contextLoads() {
        // Verifies the full Spring context starts without errors
    }

    @Test
    void allSecurityBeansAreWired() {
        assertThat(securityAutoConfiguration).isNotNull();
        assertThat(jwtService).isNotNull();
        assertThat(jwtAuthenticationFilter).isNotNull();
        assertThat(securityFilterChain).isNotNull();
    }
}
