# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

`centralsecurity` is a **Spring Boot Auto-configuration Library** (a reusable starter JAR), not a runnable application. Any Spring Boot microservice can add it as a Maven dependency to get JWT-based authentication and a centralized user database automatically — zero configuration required in the consuming service.

## Build & test commands

```bash
# Build and install to local Maven repo (~/.m2) — required before any consuming service can use it
./mvnw clean install

# Skip tests during iteration
./mvnw clean install -DskipTests

# Run tests only
./mvnw test

# Run a single test class
./mvnw test -Dtest=CentralsecurityApplicationTests

# Run a single test method
./mvnw test -Dtest=CentralsecurityApplicationTests#allSecurityBeansAreWired
```

## Architecture

### How auto-configuration works

The single entry point is [src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports](src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports), which registers `SecurityAutoConfiguration`. Spring Boot scans this file from every JAR on the classpath at startup — this is what makes the library zero-config for consumers.

**Bean wiring chain** (all declared in `SecurityAutoConfiguration`, not via `@Component` scanning):

```
SecurityDatabaseProperties ──► DataSource (MySQL, created before DataSourceAutoConfiguration)
                                    │
                             UserRepository ──► UserDetailsServiceImpl
                             RefreshTokenRepository ──► RefreshTokenService
                                                        │
JwtProperties ──► JwtService ──► JwtAuthenticationFilter
                                                        │
              PasswordEncoder ──► AuthenticationProvider│
                                                        ▼
                                        SecurityFilterChain (STATELESS, JWT filter injected)
                                                        │
                                        AuthController (conditional on jwt.auth-endpoints-enabled)
```

### Auth endpoints

All endpoints live under `/api/auth/**` (public by default, no token required):

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Create account → returns `accessToken` + `refreshToken` |
| `POST` | `/api/auth/login` | Authenticate → returns `accessToken` + `refreshToken` |
| `POST` | `/api/auth/refresh` | Exchange refresh token → new rotated `accessToken` + `refreshToken` |
| `POST` | `/api/auth/logout` | Revoke all refresh tokens for the user |

**Auth response shape** (login / register / refresh):
```json
{ "accessToken": "eyJ...", "refreshToken": "uuid-v4" }
```

**Refresh request:**
```json
{ "refreshToken": "uuid-v4" }
```

Refresh tokens are **rotated on every use** — the old token is deleted and a new one is issued, preventing replay attacks.

### Centralized database

The library owns the primary `DataSource` bean. All registered users are stored in one `users` table. The `users`, `user_roles`, and `refresh_tokens` tables are created automatically via `ddl-auto=update`.

Database credentials are resolved via a **3-level fallback chain** defined in [src/main/resources/centralsecurity-defaults.properties](src/main/resources/centralsecurity-defaults.properties) using nested Spring placeholders:

```
Level 1 — CENTRALSECURITY_DATASOURCE_* env vars   (AWS RDS / production)
      ↓ if not set
Level 2 — spring.datasource.*                      (consuming microservice's own DB config)
      ↓ if not set
Level 3 — hardcoded localhost defaults             (local development)
```

The placeholder syntax in `centralsecurity-defaults.properties`:
```properties
centralsecurity.datasource.url=${CENTRALSECURITY_DATASOURCE_URL:${spring.datasource.url:jdbc:mysql://localhost:3306/student}}
centralsecurity.datasource.username=${CENTRALSECURITY_DATASOURCE_USERNAME:${spring.datasource.username:root}}
centralsecurity.datasource.password=${CENTRALSECURITY_DATASOURCE_PASSWORD:${spring.datasource.password:8143486643}}
centralsecurity.datasource.driver-class-name=${CENTRALSECURITY_DATASOURCE_DRIVER_CLASS_NAME:${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}}
```

**Behaviour by scenario:**

| Scenario | DB used for users table |
|---|---|
| Local dev, no config anywhere | `localhost:3306/student` (hardcoded defaults) |
| Microservice has `spring.datasource.*` | That microservice's own database |
| AWS with `CENTRALSECURITY_DATASOURCE_*` env vars | AWS RDS endpoint |

**On AWS**, set these env vars in ECS task definition / EC2 / Beanstalk — Spring Boot's Relaxed Binding maps them automatically (`_` → `.`, uppercase → lowercase):
```
CENTRALSECURITY_DATASOURCE_URL      = jdbc:mysql://<rds-endpoint>:3306/<dbname>
CENTRALSECURITY_DATASOURCE_USERNAME = <rds-username>
CENTRALSECURITY_DATASOURCE_PASSWORD = <rds-password>
```

### JWT signing key

A default signing key is hardcoded in `JwtProperties.signingKey`. Consuming services require **no `jwt.secret` property**. To use a different key, set `jwt.secret=<base64-encoded key>` in the consuming service.

### Role-based access control

`@EnableMethodSecurity` is active globally (declared in `SecurityAutoConfiguration`). Consuming microservices can restrict any endpoint by annotating controller methods — no extra config needed:

```java
@GetMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")       // only ROLE_ADMIN
public List<User> getAllUsers() { ... }

@GetMapping("/dashboard")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public Dashboard getDashboard() { ... }
```

`hasRole('ADMIN')` automatically matches `ROLE_ADMIN` from the `Role` enum (Spring prepends `ROLE_` prefix). Returns HTTP 403 if the authenticated user lacks the required role.

### Key design decisions

- **No `@Component`/`@Service` on library classes** — all beans declared explicitly via `@Bean` in `SecurityAutoConfiguration`. Prevents component-scan conflicts in consuming services.
- **`@AutoConfiguration(beforeName = "...DataSourceAutoConfiguration")`** — ensures the library's DataSource is created first so all services use the central user DB, regardless of any `spring.datasource.*` properties in the consuming service.
- **`@AutoConfigurationPackage(basePackages = "com.vishal.security")`** — registers the library's entity package for JPA scanning so `User`, `user_roles`, and `refresh_tokens` are discovered without `@EntityScan` in the consuming service.
- **`@PropertySource("classpath:centralsecurity-defaults.properties")`** — provides the 3-level fallback chain for datasource credentials and sets `ddl-auto=update` and `open-in-view=false` at lowest priority; consuming service's `application.properties` overrides them.
- **`@ConditionalOnMissingBean`** on `PasswordEncoder`, `JwtService`, `UserDetailsService`, `RefreshTokenService`, and `DataSource` — consuming services can override any of these by declaring their own bean.
- **`@ConditionalOnProperty`** on `AuthController` — set `jwt.auth-endpoints-enabled=false` to suppress `/api/auth/**` endpoints.
- **`@EnableMethodSecurity`** — enables `@PreAuthorize` / `@PostAuthorize` on any bean in the consuming service without any extra configuration.
- **Refresh token rotation** — every `/api/auth/refresh` call deletes the old token and issues a new one. Expired tokens are deleted on first use. Logout deletes all tokens for the user.
- **JJWT 0.12.x API** — uses `Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)`. The old 0.9.x API is removed in this version.
- **Spring Security 6.x** — `DaoAuthenticationProvider` takes `UserDetailsService` in its constructor; `setUserDetailsService()` was removed.
- **`User` implements `UserDetails` directly** — no conversion layer between the JPA entity and Spring Security.

### Deploying to a consuming service

1. `./mvnw clean install` in this project (installs to `~/.m2`)
2. Add to the consuming service's `pom.xml`:
   ```xml
   <dependency>
       <groupId>com.vishal.security</groupId>
       <artifactId>centralsecurity</artifactId>
       <version>0.0.1-SNAPSHOT</version>
   </dependency>
   ```
3. Nothing else — no datasource config, no security config, no JWT properties needed.

Optional overrides a consuming service can set:
```properties
jwt.expiration-ms=86400000
jwt.refresh-token-expiration-ms=604800000
jwt.public-paths=/api/auth/**,/actuator/health
jwt.auth-endpoints-enabled=true
centralsecurity.datasource.url=jdbc:mysql://other-host:3306/otherdb
```

### Test setup

Tests override the MySQL datasource with H2 in-memory DB via `centralsecurity.datasource.*` properties in `@TestPropertySource` — no MySQL instance needed to run tests. `TestApplication.java` in `src/test/` provides the `@SpringBootApplication` context (there is intentionally no main application class in `src/main/`).
