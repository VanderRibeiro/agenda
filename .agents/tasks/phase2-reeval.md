# Phase 2 Re-Evaluation — Segurança JWT e Autenticação

**Project:** `c:\Users\Clarice\Documents\agenda-api` (Spring Boot 4.1.0, Java 21)
**Date of re-evaluation:** fresh read of all source files
**Build status:** `./mvnw.cmd -o compile` → **BUILD SUCCESS** (4.8s). Code compiles.

---

## Summary Answer

Phase 2 is **only partially done**. Of the 11 previously identified critical issues, only **3 are fixed** (#1 findByEmail projection, #5 invalid/expired token 500, #7 ClassCastException cast). The other **8 are still open**, including the two most security-sensitive ones: plaintext password storage in `ProfessorService` and plaintext database credentials in `application.properties`.

A **new bug** was also introduced in `AuthenticationController.register`: the email-duplication check compares an `Optional` to `null`, which can never be true, so duplicate registrations are never rejected.

Concrete scorecard:

| # | Issue | Status |
|---|-------|--------|
| 1 | `findByEmail` returned `UserDetails` projection | ✅ Fixed |
| 2 | `ProfessorService.create/update` plaintext password | ❌ Not fixed |
| 3 | `application.properties` plaintext DB credentials | ❌ Not fixed |
| 4 | JWT secret weak `default_secret` fallback | ❌ Not fixed |
| 5 | `SecurityFilter` 500 on invalid/expired token | ✅ Fixed |
| 6 | `AuthenticationController.register` uses `new BCryptPasswordEncoder()` | ❌ Not fixed |
| 7 | `(Professor) auth.getPrincipal()` ClassCastException | ✅ Fixed |
| 8 | `AuthenticationDTO` no validation | ❌ Not fixed |
| 9 | Token subject = email, no `professorId` claim | ❌ Not fixed |
| 10 | `jjwt` declared but unused (impl uses `java-jwt`) | ❌ Not fixed |

**Phase 2 task scorecard:**

| Task | Status |
|------|--------|
| 1. PasswordEncoder bean + hashing in ProfessorService | ⚠️ Partial (bean only) |
| 2. JwtService / TokenService (library, professorId claim) | ❌ No professorId claim |
| 3. SecurityFilter (invalid/expired handling) | ✅ Fixed (no professorId extraction) |
| 4. UserDetailsService + findByEmail | ✅ Fixed |
| 5. Auth endpoints | ❌ Not fixed (register issues) |
| 6. SecurityConfig | ⚠️ Partial (role contradiction) |
| 7. Masked credentials | ❌ Not fixed |

---

## Evidence — Phase 2 Tasks

### Task 1 — PasswordEncoder bean + hashing in ProfessorService → ⚠️ PARTIAL

The BCrypt bean exists in `SecurityConfig.java` (lines 45-47):

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

But `ProfessorService.java` injects **only** `ProfessorRepository` and performs **no password hashing**:

```java
public ProfessorService(ProfessorRepository professorRepository) {
    this.professorRepository = professorRepository;
}
...
public Professor create(Professor professor) {
    if (professor == null) { throw new BusinessException(...); }
    return professorRepository.save(professor);   // saved as received — plaintext
}
...
public void updateProfessor(Professor professor, Professor obj) {
    professor.setNome(obj.getNome());
    professor.setEmail(obj.getEmail());
    professor.setPassword(obj.getPassword());     // written verbatim — plaintext
    professor.setIntervaloMinimo(obj.getIntervaloMinimo());
}
```

A grep for `PasswordEncoder|passwordEncoder|BCrypt` across the codebase returns matches in only two files: `SecurityConfig.java` (the bean) and `AuthenticationController.java` (`new BCryptPasswordEncoder()`). `ProfessorService` has **zero** references. Passwords reaching `POST /professores` (via `ProfessorRequestDTO.toEntity()` → `professor.setPassword(this.senhaHash)`) are stored as-is.

**Conclusion:** bean present, hashing absent in the service. NOT complete.

### Task 2 — JwtService / TokenService (library, professorId claim) → ❌ NOT FIXED

There is no separate `JwtService`; the work is done by `config/TokenService.java`. It uses the **`com.auth0.jwt` (java-jwt)** library (not jjwt):

```java
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
```

Token generation sets the subject to **email** and adds **no `professorId` claim**:

```java
String token = JWT.create()
        .withIssuer("auth-api")
        .withSubject(professor.getEmail())
        .withExpiresAt(dataExpiracao())
        .sign(algorithm);
```

`validateToken` returns only the subject (email). There is **no `extractProfessorId` method** anywhere.

```java
public String validateToken(String token) {
    try {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.require(algorithm).withIssuer("auth-api").build()
                .verify(token).getSubject();
    } catch (Exception exception) {
        throw new RuntimeException("Token JWT inválido ou expirado", exception);
    }
}
```

**Conclusion:** no `professorId` claim, no extractor. The `jjwt` dependency in `pom.xml` remains unused.

### Task 3 — SecurityFilter (invalid/expired handling) → ✅ FIXED (professorId extraction absent)

`SecurityFilter.java` now wraps token validation in try/catch and clears the context on failure, so an invalid/expired token no longer throws a 500:

```java
if (token != null) {
    try {
        var login = tokenService.validateToken(token);
        professorRepository.findByEmail(login).ifPresent(professor -> {
            var user = new ProfessorUserDetails(professor);
            var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        });
    } catch (RuntimeException e) {
        SecurityContextHolder.clearContext();
    }
}
```

`ifPresent` avoids calling `getAuthorities()` on a null principal. Bearer extraction works.

However, the filter does **not** extract a `professorId` (because the token carries none). It re-resolves the professor from the DB by email on every authenticated request. Functionally OK for auth, but does not satisfy the professorId-claim requirement.

### Task 4 — UserDetailsService + findByEmail → ✅ FIXED

`ProfessorRepository.findByEmail` now returns `Optional<Professor>`, not a `UserDetails` projection:

```java
public interface ProfessorRepository extends JpaRepository<Professor, UUID> {
    Optional<Professor> findByEmail(String email);
}
```

`ProfessorUserDetailsService` wraps the entity in `ProfessorUserDetails`:

```java
return professorRepository.findByEmail(email)
        .map(ProfessorUserDetails::new)
        .orElseThrow(() -> new UsernameNotFoundException("Professor não encontrado: " + email));
```

`ProfessorUserDetails` implements `UserDetails`, returns `ROLE_PROFESSOR`, exposes `getProfessor()`. Clean and correct.

### Task 5 — Auth endpoints → ❌ NOT FIXED (multiple problems)

`AuthenticationController.java`:

- **Login** is fine: authenticates via `AuthenticationManager`, casts principal to `ProfessorUserDetails` (which is now the actual type, so the cast is safe), and returns a token.
- **Register** has two problems:

  (a) Still uses `new BCryptPasswordEncoder()` instead of injecting the bean (issue #6):
  ```java
  String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
  ```
  Note the file even imports `PasswordEncoder` but never uses the injected bean.

  (b) **NEW BUG** — the email-duplication check is broken because `findByEmail` now returns `Optional`, which is never `null`:
  ```java
  if(this.professorRepository.findByEmail(data.login()) != null) return ResponseEntity.badRequest().build();
  ```
  An `Optional` (empty or not) is always `!= null`, so this condition is **always false**. Duplicate emails are never rejected; `save()` then throws on the unique constraint at the DB level instead of returning 400.

- **DTOs lack validation** (issue #8). `AuthenticationDTO` is a bare record with no annotations, even though both endpoints use `@Valid`:
  ```java
  public record AuthenticationDTO(String nome, String login, String password) {
  }
  ```
  Blank login/password (and blank nome on register) are not caught at the controller layer.

### Task 6 — SecurityConfig → ⚠️ PARTIAL (role contradiction)

`SecurityConfig.java` configures CSRF off, STATELESS sessions, public login/register, everything else authenticated, and wires the filter via `addFilterBefore` — all correct:

```java
.requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
.requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
.requestMatchers(HttpMethod.POST, "/professores").hasRole("PROFESSOR")
.anyRequest().authenticated()
.addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
```

The contradiction from issue #11 remains: `POST /professores` requires `ROLE_PROFESSOR` while `/auth/register` is public. A brand-new user can self-register via `/auth/register`, but creating any other professor via `/professores` requires an already-authenticated professor. The two endpoints overlap in purpose with inconsistent access rules. (Also note `/auth/**` is not used as a wildcard; specific paths are permitted — acceptable, arguably more precise.)

### Task 7 — Masked credentials → ❌ NOT FIXED

`application.properties` still has plaintext DB credentials:

```properties
spring.datasource.username=postgres
spring.datasource.password=12345678
api.security.token.secret=${JWT_SECRET:default_secret}
```

Neither `${DB_USERNAME}` / `${DB_PASSWORD}` is used, and the JWT secret still falls back to `default_secret`.

---

## Evidence — Previous 11 Issues

**Issue #1 — `findByEmail` projection → ✅ FIXED.** Returns `Optional<Professor>` (see Task 4).

**Issue #2 — `ProfessorService` plaintext password → ❌ NOT FIXED.** No `PasswordEncoder` is injected or used in `create`/`updateProfessor` (see Task 1). Passwords from `POST /professores` and from `PUT /professores` are persisted verbatim.

**Issue #3 — plaintext DB credentials → ❌ NOT FIXED.** `application.properties` keeps `username=postgres` / `password=12345678` in the clear (see Task 7).

**Issue #4 — weak JWT secret fallback → ❌ NOT FIXED.** `${JWT_SECRET:default_secret}` is still present; the `default_secret` fallback remains (see Task 7).

**Issue #5 — `SecurityFilter` 500 on bad token → ✅ FIXED.** Try/catch clears context, request proceeds unauthenticated (see Task 3).

**Issue #6 — `new BCryptPasswordEncoder()` in register → ❌ NOT FIXED.** `AuthenticationController.register` still instantiates it directly; the injected `PasswordEncoder` bean is not used (see Task 5a).

**Issue #7 — `(Professor) auth.getPrincipal()` ClassCastException → ✅ FIXED.** The cast is now `(ProfessorUserDetails) auth.getPrincipal()`, and the principal produced by `ProfessorUserDetailsService` is a `ProfessorUserDetails`. The latent cast is resolved.

**Issue #8 — `AuthenticationDTO` no validation → ❌ NOT FIXED.** The record has no bean-validation annotations (see Task 5). `@Valid` on the controller is inert.

**Issue #9 — token subject = email, no `professorId` → ❌ NOT FIXED.** `gerarToken` sets `withSubject(professor.getEmail())` and adds no id claim; no extractor exists (see Task 2).

**Issue #10 — `jjwt` declared but unused → ❌ NOT FIXED.** `pom.xml` declares both `com.auth0:java-jwt:4.6.2` (used by `TokenService`) and `io.jsonwebtoken:jjwt-api/jjwt-impl/jjwt-jackson:0.12.6` (unused). The implementation uses only java-jwt.

**Issue #11 — `POST /professores` role vs public register → ❌ NOT FIXED.** `POST /professores` requires `hasRole("PROFESSOR")` while `/auth/register` is public (see Task 6).

---

## New Issues Found

1. **Broken email-duplication check (HIGH).** `AuthenticationController.register` does `professorRepository.findByEmail(data.login()) != null`. Since `findByEmail` returns `Optional` (never null), the check is always false. Duplicate emails are not rejected at the controller and instead surface as a DB constraint violation. Fix: use `professorRepository.findByEmail(data.login()).isPresent()`.

2. **`ProfessorRequestDTO.senhaHash` is misleading (MEDIUM).** The field is named `senhaHash` (implying a hash) but it receives plaintext from the client and is written straight to `professor.setPassword(...)` via `toEntity()`, with no hashing in `ProfessorService.create`. The naming hides the fact that passwords are stored in plaintext through `POST /professores`.

3. **`PasswordEncoder` import dead code (LOW).** `AuthenticationController` imports `PasswordEncoder` but never uses the bean (it uses `new BCryptPasswordEncoder()`). Confusing and signals the intended fix was started but not finished.

---

## Conclusions and Recommendations

The three issues that were fixed (#1, #5, #7) are correctly and cleanly implemented — the filter, the user-details wrapper, and the repository return type are all good. But the core security hardening of Phase 2 is largely unaddressed. Recommended fixes (implement nothing here):

1. **Inject `PasswordEncoder` into `ProfessorService`** and hash in both `create` and `updateProfessor` (only hash when the incoming password changed). This closes the plaintext-storage path through `POST /professores` and `PUT /professores`. Also fix `AuthenticationController.register` to inject the same bean instead of `new BCryptPasswordEncoder()`.
2. **Mask DB credentials**: replace `spring.datasource.username`/`password` with `${DB_USERNAME}`/`${DB_PASSWORD}`. Drop the `default_secret` fallback on `api.security.token.secret` (use `${JWT_SECRET}` with no default, failing fast if unset).
3. **Fix the duplicate-email check** to `.isPresent()`.
4. **Add validation** to `AuthenticationDTO` (`@NotBlank` on login/password; `@NotBlank`/`@Size` on nome) so `@Valid` actually enforces anything.
5. **Add a `professorId` claim** to the token (e.g., `.withClaim("professorId", professor.getId().toString())`) and an `extractProfessorId` method in `TokenService`; optionally have `SecurityFilter` use it instead of re-querying by email.
6. **Remove the unused `jjwt` dependencies** from `pom.xml` (or switch the implementation to jjwt and drop java-jwt) — pick one library.
7. **Reconcile the `POST /professores` vs `/auth/register` access rules** (issue #11): either make professor creation admin-only and keep register public for self-signup, or consolidate creation into one path. The current split is contradictory.

Build is green, so none of these are compile blockers — they are correctness/security gaps.
