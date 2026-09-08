# Security Design

## Authentication

- Spring Security form login (`/auth/login`), backed by `CustomUserDetailsService` which loads `User` by email and wraps it in `CustomUserDetails`.
- Passwords are hashed with BCrypt (`PasswordEncoder` bean) — never stored or compared in plain text.
- `DaoAuthenticationProvider` ties the user-details service and password encoder together.

## Authorization

Configured centrally in `SecurityConfig.filterChain`:

| URL pattern | Requirement |
|---|---|
| `/`, `/products/**`, `/categories/**`, `/auth/**`, static assets, `/uploads/**` | Public |
| `/admin/**` | `ROLE_ADMIN` |
| `/cart/**`, `/checkout/**`, `/payment/**`, `/orders/**`, `/profile/**` | Any authenticated user (`ROLE_CUSTOMER` or `ROLE_ADMIN`) |
| anything else | Authenticated |

Role checks are also expressed in Thymeleaf views via `sec:authorize` (e.g., hiding "Add to Cart" from anonymous visitors, showing the Admin nav link only to admins) — this is a UX convenience only; the real enforcement is server-side in `SecurityConfig`.

## Ownership Checks

Beyond role-based access, some resources are further scoped to their owner in the service layer:
- `OrderService.getByIdForUser(id, user)` throws `ResourceNotFoundException` if the order doesn't belong to the requesting user — so a customer can't view another customer's order just by guessing an ID, even though `/orders/{id}` is open to any authenticated user.

## Password & Account Notes

- Minimum password length of 6 characters is enforced via Bean Validation on `RegisterRequest` (`@Size(min = 6)`) — consider raising this and adding complexity rules for production use.
- The seeded demo admin (`admin@shop.com` / `admin123`) is for local development only. **Change or remove it before deploying anywhere reachable by real users.**

## CSRF

CSRF protection is currently disabled (`http.csrf(AbstractHttpConfigurer::disable)`) to keep the plain HTML forms simple during development. Before production deployment, re-enable CSRF and add the Thymeleaf CSRF hidden input (`<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">`) to every state-changing form (Spring Security auto-injects this if you use `th:action` and leave CSRF enabled with the Thymeleaf integration).

## File Uploads

Product images are validated only by size (`spring.servlet.multipart.max-file-size=5MB`) and are stored with a randomly generated filename (`FileStorageService`), which avoids path traversal and filename collisions. Consider adding content-type/magic-byte validation before accepting uploads in production.
