package com.vishal.security.config;

import com.vishal.security.filter.JwtAuthenticationFilter;
import com.vishal.security.repository.RefreshTokenRepository;
import com.vishal.security.repository.UserRepository;
import com.vishal.security.service.JwtService;
import com.vishal.security.service.RefreshTokenService;
import com.vishal.security.service.UserDetailsServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;

/**
 * Spring Boot Auto-configuration entry point for the centralsecurity library.
 *
 * Registered via:
 *   META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 *
 * Responsibilities:
 *  - Provides the central DataSource (MySQL, hardcoded defaults, overridable via centralsecurity.datasource.*)
 *  - Registers com.vishal.security entities for JPA scanning (@AutoConfigurationPackage)
 *  - Sets low-priority JPA defaults via @PropertySource (ddl-auto=update, open-in-view=false)
 *  - Wires all JWT security beans and the SecurityFilterChain
 *
 * Consuming services need ZERO datasource or security configuration — just add the dependency.
 */
@AutoConfiguration(beforeName = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
@AutoConfigurationPackage(basePackages = "com.vishal.security")
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({JwtProperties.class, SecurityDatabaseProperties.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@PropertySource("classpath:centralsecurity-defaults.properties")
public class SecurityAutoConfiguration {

    // -------------------------------------------------------------------------
    // DataSource — central user database, owned by this security module.
    // All microservices share this one DB for user management.
    // Override connection details via centralsecurity.datasource.* properties.
    // -------------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource(SecurityDatabaseProperties dbProps) {
        return DataSourceBuilder.create()
                .url(dbProps.getUrl())
                .username(dbProps.getUsername())
                .password(dbProps.getPassword())
                .driverClassName(dbProps.getDriverClassName())
                .build();
    }

    // -------------------------------------------------------------------------
    // Security beans
    // -------------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean(JwtService.class)
    public JwtService jwtService(JwtProperties jwtProperties) {
        return new JwtService(jwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean(RefreshTokenService.class)
    public RefreshTokenService refreshTokenService(RefreshTokenRepository refreshTokenRepository,
                                                   JwtProperties jwtProperties) {
        return new RefreshTokenService(refreshTokenRepository, jwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new UserDetailsServiceImpl(userRepository);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService,
                                                            UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                    JwtAuthenticationFilter jwtFilter,
                                                    AuthenticationProvider authenticationProvider,
                                                    JwtProperties jwtProperties) {

        String[] publicPaths = jwtProperties.getPublicPaths().toArray(new String[0]);

        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(publicPaths).permitAll()
                .anyRequest().authenticated())
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
