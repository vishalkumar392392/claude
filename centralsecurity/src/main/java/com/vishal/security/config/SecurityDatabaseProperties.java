package com.vishal.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Datasource configuration for the centralized user database.
 * Defaults are set in centralsecurity-defaults.properties (loaded via @PropertySource
 * on SecurityAutoConfiguration) — consuming services need zero datasource configuration
 * except for the password, which must be set explicitly.
 *
 * Override any value in your application.properties:
 *   centralsecurity.datasource.url=jdbc:mysql://other-host:3306/otherdb
 *   centralsecurity.datasource.username=youruser
 *   centralsecurity.datasource.password=yourpassword
 *   centralsecurity.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
 */
@ConfigurationProperties(prefix = "centralsecurity.datasource")
public class SecurityDatabaseProperties {

    private String url;
    private String username;
    private String password;
    private String driverClassName;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDriverClassName() { return driverClassName; }
    public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
}
