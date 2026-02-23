# Package: `com.myapp.reservations.security`

This package implements JWT-based stateless authentication and authorization using Spring Security. It contains the security configuration, JWT utility, authentication filter, and entry point handler.

---

## `WebSecurityConfig.java`
**Type**: `@Configuration`
**Purpose**: Central Spring Security configuration. Defines the security filter chain, CORS policy, password encoder, and authentication manager.

### Beans

| Bean | Type | Description |
|------|------|-------------|
| `authenticationJwtTokenFilter()` | `AuthTokenFilter` | JWT authentication filter instance |
| `authenticationManager(AuthenticationConfiguration)` | `AuthenticationManager` | Delegates to Spring's default auth manager |
| `passwordEncoder()` | `PasswordEncoder` | `BCryptPasswordEncoder` for password hashing |
| `filterChain(HttpSecurity)` | `SecurityFilterChain` | Main security configuration |
| `corsConfigurationSource()` | `CorsConfigurationSource` | API-level CORS policy |

### Security Filter Chain Configuration

| Setting | Value |
|---------|-------|
| CSRF | Disabled (stateless API) |
| Session Management | `STATELESS` (no server-side sessions) |
| Auth Entry Point | `AuthEntryPointJwt` (returns 401 on auth failure) |
| JWT Filter | Added before `UsernamePasswordAuthenticationFilter` |

### Public Endpoints (permitAll)

| Pattern | Method | Description |
|---------|--------|-------------|
| `/api/auth/**` | ALL | Authentication (sign in, sign up) |
| `/uploads/**` | ALL | Static file access |
| `/api/businesses/**` | GET | Browse businesses |
| `/api/offerings/**` | GET | Browse offerings |
| `/api/availabilities/**` | GET | Check availability |
| `/api/schedules/**` | GET | View schedules |
| `/api/reviews/**` | GET | Read reviews |
| `/api/files/business-photos/**` | GET | View gallery photos |

All other requests require authentication.

### CORS Configuration

| Setting | Value |
|---------|-------|
| Allowed Origins | Configurable via `app.cors.allowed-origins` (default: `http://localhost:5173`) |
| Allowed Methods | GET, POST, PUT, PATCH, DELETE, OPTIONS |
| Allowed Headers | Authorization, Content-Type |
| Allow Credentials | true |

---

## `AuthTokenFilter.java`
**Type**: `@Component` extending `OncePerRequestFilter`
**Purpose**: Intercepts every HTTP request to extract and validate the JWT token from the `Authorization` header, then populates the Spring Security context.

### Constants

| Constant | Value | Description |
|----------|-------|-------------|
| `BEARER_` | `"Bearer "` | Authorization header prefix |
| `USER_ID_ATTRIBUTE` | `"currentUserId"` | Request attribute key for the authenticated user's UUID |

### Authentication Flow

```
1. Extract JWT from "Authorization: Bearer <token>" header
2. Validate the token (signature, expiration)
3. Extract userId from token claims
4. If userId exists:
   - Load UserDetails by userId
   - Set "currentUserId" as request attribute
5. If userId is null:
   - Fall back to loading UserDetails by username (subject)
6. Create UsernamePasswordAuthenticationToken with UserDetails + authorities
7. Set authentication in SecurityContextHolder
```

**Error Handling**: If any exception occurs during token processing, it is logged and the request continues unauthenticated (filter chain proceeds without setting SecurityContext).

---

## `JwtUtil.java`
**Type**: `@Component`
**Purpose**: Handles JWT token generation, parsing, and validation using the `jjwt` library.

### Configuration

| Property | Value | Description |
|----------|-------|-------------|
| Secret Key | HMAC-SHA256 key (hardcoded) | Signing key for JWT tokens |
| Token Expiration | 36,000,000 ms (10 hours) | Token lifetime |

### Methods

| Method | Parameters | Return | Description |
|--------|------------|--------|-------------|
| `generateToken(String, UUID)` | username, userId | `String` (JWT) | Creates a signed JWT with `sub=username`, `userId` claim, issued-at, and expiration |
| `getUserFromToken(String)` | token | `String` | Extracts the username (subject) from the token |
| `getUserIdFromToken(String)` | token | `UUID` | Extracts the `userId` custom claim from the token |
| `validateToken(String)` | token | `Boolean` | Validates the token's signature and expiration. Returns `false` on any error. |

### Token Structure

```json
{
  "sub": "username",
  "userId": "uuid-string",
  "iat": 1234567890,
  "exp": 1234603890
}
```

---

## `AuthEntryPointJwt.java`
**Type**: Implements `AuthenticationEntryPoint`
**Purpose**: Handles unauthorized access attempts. Returns HTTP `401 Unauthorized` when an unauthenticated request tries to access a protected endpoint.

### Behavior

When triggered, sends:
```
HTTP 401 Unauthorized
Body: "Unauthorized"
```
