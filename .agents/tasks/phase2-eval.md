# Avaliação da Fase 2 — Segurança JWT e Autenticação

> Avaliação nova e completa (fresh), feita apenas a partir da leitura do código atual. Nenhuma dependência de relatório anterior.
> Verificação de build executada: `./mvnw.cmd -o compile` → **BUILD SUCCESS** (41 arquivos `.class`). O projeto compila. Os problemas abaixo são de **tempo de execução/segurança**, não de compilação.
> Escopo: análise estática. Não executei a aplicação (exige PostgreSQL/Testcontainers), então o comportamento em runtime é inferido a partir do código e do comportamento conhecido do Spring Data JPA / Spring Security. Indico explicitamente o que não pude confirmar em runtime.

## Resumo (resposta curta)

A Fase 2 está **parcialmente implementada**: a maior parte do esqueleto existe (SecurityConfig com BCrypt, filtro JWT, UserDetailsService, endpoints de auth, STATELESS, CSRF desativado), porém **a autenticação está quebrada em runtime** por um defeito central: `ProfessorRepository.findByEmail()` retorna `UserDetails` diretamente, mas a entidade `Professor` **não** implementa `UserDetails`, então o Spring Data JPA cria uma **projeção (proxy JDK)** com `username=null`, `password=null` e `authorities=null`. Isso faz o login falhar com BadCredentials e as requisições autenticadas estourarem. Existe uma classe `ProfessorUserDetails` que faria o wrapping corretamente, mas ela **não é usada em lugar nenhum**.

| # | Tarefa | Status | Resumo |
|---|-------|--------|--------|
| 1 | PasswordEncoder bean + hash no ProfessorService | **Parcial** | Bean BCrypt existe; hash aplicado só no `/auth/register` (com instância nova, não o bean). `ProfessorService.create`/`update` **não** fazem hash → senha em texto puro via `POST /professores` e `PUT /professores`. |
| 2 | JwtService (jjwt 0.12.6, extractProfessorId) | **Parcial / Desvio** | Existe `TokenService` que gera/valida JWT, mas usa **`com.auth0:java-jwt` 4.6.2**, não `jjwt` (que está no pom porém **não usado**). O subject do token é o **email**, não há claim `professorId` nem método `extractProfessorId`. |
| 3 | JwtAuthenticationFilter | **Parcial (com bug)** | Existe `SecurityFilter` (OncePerRequestFilter) que extrai Bearer, valida e popula SecurityContext. Mas carrega o usuário via `findByEmail` (projeção quebrada) e chama `user.getAuthorities()` → `null` → exceção. Sem tratamento de token inválido/expirado. |
| 4 | UserDetailsService + findByEmail | **Parcial (com bug)** | `ProfessorUserDetailsService` existe; `findByEmail(String)` existe. Mas o tipo de retorno é `UserDetails` (projeção quebrada) e o wrapper `ProfessorUserDetails` está **órfão/não usado**. |
| 5 | Endpoints de auth + DTOs | **Parcial** | `POST /auth/login` e `POST /auth/register` existem e devolvem `LoginResponseDTO(token)`. DTOs são `AuthenticationDTO`/`LoginResponseDTO` (nomes diferem do esperado `LoginRequest`/`LoginResponse`). `AuthenticationDTO` **sem nenhuma anotação de validação**. Lógica no controller (sem `AuthService`). Login lança `ClassCastException` latente (cast `(Professor) auth.getPrincipal()` sobre a projeção). |
| 6 | SecurityConfig — rotas, CSRF, STATELESS, filtro | **Concluído (com ressalvas)** | CSRF off, STATELESS, `/auth/login`+`/auth/register` permitAll, `anyRequest().authenticated()`, filtro wired via `addFilterBefore`. Ressalvas: `POST /professores` exige `ROLE_PROFESSOR` (contraditório com `/auth/register` público que cria professor). |
| 7 | Credenciais mascaradas em application.properties | **Parcial** | `api.security.token.secret=${JWT_SECRET:default_secret}` usa env var (com fallback fraco `default_secret`). **`username=postgres` e `password=12345678` estão fixos em texto puro** — não usam `${DB_USERNAME}`/`${DB_PASSWORD}`. |

**Problemas de segurança críticos a destacar:** (1) credenciais de banco em texto puro; (2) fallback de secret JWT fraco e hardcoded; (3) `ProfessorService.create`/`update` armazenam senha sem hash (texto puro) via endpoints de professor; (4) autenticação quebrada em runtime (projeção `UserDetails`); (5) `AuthenticationController.register` instância `new BCryptPasswordEncoder()` em vez de injetar o bean; (6) `SecurityFilter` não trata token inválido/expirado (vira 500, não 401).

---

## Evidência por tarefa

### Tarefa 1 — PasswordEncoder bean e hash da senha

**PasswordEncoder bean:** ✅ presente em `config/SecurityConfig.java`:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Hash aplicado — só no registro de auth:** `controller/AuthenticationController.java`:
```java
String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
Professor newProfessor = new Professor(data.nome(), data.login(), encryptedPassword);
this.professorRepository.save(newProfessor);
```
Observação: instancia `new BCryptPasswordEncoder()` em vez de injetar o bean `passwordEncoder` configurado. Funciona, mas ignora a configuração central e impede troca fácil de algoritmo.

**Hash NÃO aplicado em ProfessorService (criar e atualizar):** `service/ProfessorService.java`:
```java
public Professor create(Professor professor) {
    if (professor == null) { throw new BusinessException(...); }
    return professorRepository.save(professor);   // <- salva senha sem hash
}

public void updateProfessor(Professor professor, Professor obj) {
    professor.setNome(obj.getNome());
    professor.setEmail(obj.getEmail());
    professor.setSenhaHash(obj.getSenhaHash());   // <- grava o valor bruto, sem hash
    professor.setIntervaloMinimo(obj.getIntervaloMinimo());
}
```
`ProfessorController.create`/`update` chamam `ProfessorService` passando `professorRequestDTO.toEntity()`, que copia `senhaHash` diretamente do DTO:
```java
// dto/request/ProfessorRequestDTO.java
professor.setSenhaHash(this.senhaHash);   // valor que veio do request, sem hash
```
Resultado: `POST /professores` e `PUT /professores/{id}` persistem a senha em texto puro. Violação direta do requisito ("must hash the password via `passwordEncoder.encode(...)` before persisting"). O campo da entidade é corretamente `senhaHash` (`entities/Professor.java`, coluna `senha_hash`), mas o valor armazenado pode ser texto puro.

**Veredito:** Parcial. Bean existe; hashing correto só no caminho de registro de auth; ausente no service de Professor (create/update).

---

### Tarefa 2 — JwtService (jjwt 0.12.6, extrair professorId)

`config/TokenService.java` faz geração e validação de JWT:
```java
public String gerarToken(Professor professor) {
    Algorithm algorithm = Algorithm.HMAC256(secret);
    String token = JWT.create()
        .withIssuer("auth-api")
        .withSubject(professor.getEmail())   // <- subject = email, não professorId
        .withExpiresAt(dataExpiracao())
        .sign(algorithm);
    return token;
}
public String validateToken(String token) {
    Algorithm algorithm = Algorithm.HMAC256(secret);
    return JWT.require(algorithm).withIssuer("auth-api").build()
        .verify(token).getSubject();        // <- devolve o email
}
```

Discrepâncias com o brief:
- **Biblioteca errada:** usa `com.auth0.jwt` (`com.auth0:java-jwt:4.6.2`), não `io.jsonwebtoken:jjwt-* 0.12.6`. O `jjwt` está declarado no `pom.xml` (jjwt-api/impl/jackson 0.12.6) porém **não é referenciado por nenhuma classe** — dependência inutilizada.
- **Sem `professorId` no token:** o subject é `professor.getEmail()`. Não há claim de `professorId` e nem método `extractProfessorId`. `validateToken` devolve o email.
- Nomes em português (`gerarToken`/`validateToken`) em vez de `generateToken`/`validateToken`/`extractProfessorId` (cosmético, mas difere do esperado).

Impacto: para a Fase 3 (isolamento multi-tenant via `professorId` extraído do token), o `SecurityContext` hoje carrega o email, não o id. Será preciso adicionar o claim `professorId` ou buscar pelo email.

**Veredito:** Parcial / desvio. Funcionalidade de gerar/validar JWT existe e está correta em essência, mas usa a biblioteca `java-jwt` (não `jjwt`) e não expõe `professorId`.

---

### Tarefa 3 — JwtAuthenticationFilter

`config/SecurityFilter.java` (equivalente ao `JwtAuthenticationFilter`):
```java
@Component
public class SecurityFilter extends OncePerRequestFilter {
    @Autowired TokenService tokenService;
    @Autowired ProfessorRepository professorRepository;

    @Override
    protected void doFilterInternal(...) {
        var token = this.recoverToken(request);
        if (token != null) {
            var login = tokenService.validateToken(token);
            UserDetails user = professorRepository.findByEmail(login);
            var authentication = new UsernamePasswordAuthenticationToken(user, login, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
    private String recoverToken(HttpServletRequest request){
        var authorizationHeader = request.getHeader("Authorization");
        if(authorizationHeader != null){ return authorizationHeader.replace("Bearer ", ""); }
        return null;
    }
}
```

Problemas:
- **Bug central de runtime:** `professorRepository.findByEmail(login)` devolve uma **projeção `UserDetails`** (ver tarefa 4) com `authorities == null`. `user.getAuthorities()` é `null`, e o construtor de 3 args de `UsernamePasswordAuthenticationToken` lança `IllegalArgumentException` quando `authorities` é nulo. → toda requisição autenticada vira erro 500.
- **Sem null-check:** se o usuário foi excluído, `user` é `null` → `user.getAuthorities()` lança `NullPointerException`.
- **Sem tratamento de exceção:** `tokenService.validateToken` lança `RuntimeException` para token inválido/expirado; o filtro não captura → a exceção propaga como 500 em vez de devolver 401/ignorar.
- **`replace("Bearer ", "")`** funciona para o caso normal, mas troca todas as ocorrências da substring no header (não apenas o prefixo). Reescrever com `substring` seria mais seguro.

O filtro **está** conectado à cadeia em `SecurityConfig` via `addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)`. ✓

**Veredito:** Parcial (com bug crítico de runtime). Estrutura certa, mas a população do `SecurityContext` quebra em runtime e não trata erros de token.

---

### Tarefa 4 — UserDetailsService + findByEmail

`repositories/ProfessorRepository.java`:
```java
public interface ProfessorRepository extends JpaRepository<Professor, UUID> {
    UserDetails findByEmail(String email);
}
```

`service/ProfessorUserDetailsService.java`:
```java
@Service
public class ProfessorUserDetailsService implements UserDetailsService {
    @Autowired ProfessorRepository professorRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return professorRepository.findByEmail(email);   // devolve a projeção
    }
}
```

`security/ProfessorUserDetails.java` (wrapper correto, **porém não usado**):
```java
public class ProfessorUserDetails implements UserDetails {
    private final Professor professor;
    public ProfessorUserDetails(Professor professor) { this.professor = professor; }
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_PROFESSOR"));
    }
    public String getPassword() { return professor.getSenhaHash(); }
    public String getUsername() { return professor.getEmail(); }
    ...
}
```

**O defeito central da Fase 2:** `Professor` **não** implementa `UserDetails` (ver `entities/Professor.java` — classe sem `implements UserDetails`). Como `findByEmail` declara retorno `UserDetails`, o Spring Data JPA trata `UserDetails` como **interface de projeção** e cria um proxy JDK apoiado na entidade. Esse proxy resolve os getters por propriedade:
- `getUsername()` → não existe `username` em `Professor` (existe `getEmail()`) → **null**
- `getPassword()` → não existe `password` (existe `getSenhaHash()`) → **null**
- `getAuthorities()` → não existe `authorities` → **null**

Consequências em runtime (inferidas por análise estática do comportamento do Spring Data/Security; **não verificadas executando**):
- **Login:** `DaoAuthenticationProvider` chama `ProfessorUserDetailsService.loadUserByUsername(email)` → recebe a projeção com `password=null`. Em `additionalAuthenticationChecks`, `BCryptPasswordEncoder.matches(senhaDigitada, null)` devolve `false` → `BadCredentialsException` (401). Login nunca funciona.
- **Requisições autenticadas:** o `SecurityFilter` chama `user.getAuthorities()` → `null` → `IllegalArgumentException` (500).

A classe `ProfessorUserDetails` está **órfã**: nem o repositório nem o `UserDetailsService` a instanciam. É exatamente o que faltaria para corrigir o fluxo (devolver `Professor` do repositório e envelopar com `ProfessorUserDetails`).

**Veredito:** Parcial (com bug crítico). `findByEmail` existe, mas com tipo de retorno errado, e o wrapper correto existe mas não é conectado. Autenticação não funciona em runtime.

---

### Tarefa 5 — Endpoints de auth e DTOs

`controller/AuthenticationController.java`:
```java
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var token = tokenService.gerarToken((Professor) auth.getPrincipal());  // <- cast sobre projeção
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid AuthenticationDTO data) {
        if(this.professorRepository.findByEmail(data.login()) != null) return ResponseEntity.badRequest().build();
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        Professor newProfessor = new Professor(data.nome(), data.login(), encryptedPassword);
        this.professorRepository.save(newProfessor);
        return ResponseEntity.ok().build();
    }
}
```

`dto/AuthenticationDTO.java`:
```java
public record AuthenticationDTO(String nome, String login, String password) { }   // sem validação
```
`dto/LoginResponseDTO.java`:
```java
public record LoginResponseDTO(String token) { }
```

Pontos:
- `POST /auth/login` devolve `LoginResponseDTO(token)` ✓ (mas o fluxo quebra na autenticação — ver tarefa 4).
- **Cast latente problemático:** `(Professor) auth.getPrincipal()`. `auth.getPrincipal()` é o `UserDetails` retornado por `loadUserByUsername` (a projeção), **não** um `Professor`. Esse cast lança `ClassCastException`. Hoje não é alcançado porque `authenticate()` falha antes (401), mas é um bug latente: corrigir a projeção ingenuamente (fazer `findByEmail` devolver `Professor`) faria o cast estourar.
- `POST /auth/register` faz hash ✓, verifica duplicidade de email ✓, cria professor ✓. Usa `new BCryptPasswordEncoder()` (não o bean).
- **Nomes dos DTOs:** `AuthenticationDTO`/`LoginResponseDTO` em vez de `LoginRequest`/`LoginResponse` (cosmético).
- **DTO compartilhado:** `AuthenticationDTO` serve para login e registro, mas login não usa `nome` e registro precisa de `nome`. Sem anotações de validação, o registro pode criar um `Professor` com `nome=null` e email inválido.
- **Sem `AuthService`:** a lógica fica no controller (encontrado OK para o escopo, mas não é o ideal).
- `register` não devolve a entidade/token criados (apenas 200 vazio).

**Veredito:** Parcial. Endpoints existem e a forma está correta; login quebra em runtime; DTOs sem validação e com cast latente.

---

### Tarefa 6 — SecurityConfig: rotas, CSRF, STATELESS, filtro

`config/SecurityConfig.java`:
```java
.csrf(csrf -> csrf.disable())
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
.authorizeHttpRequests(authorize -> authorize
    .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
    .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()   // "temporariamente"
    .requestMatchers(HttpMethod.POST, "/professores").hasRole("PROFESSOR")
    .anyRequest().authenticated())
.addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
```
- CSRF desativado ✓ (stateless)
- STATELESS ✓
- `/auth/login` e `/auth/register` públicos ✓ (equivalente funcional a `/auth/**` permitAll, já que só existem esses dois endpoints em `/auth`)
- `anyRequest().authenticated()` ✓ — todas as demais rotas exigem autenticação
- Filtro wired com `addFilterBefore` ✓
- `AuthenticationManager` bean exposto ✓

Ressalvas:
- `POST /professores` exige `ROLE_PROFESSOR` — contradiz o `/auth/register` público que também cria professores. É um caminho duplicado e, pior, esse caminho **não faz hash** (ver tarefa 1). Recomenda-se remover a role de `POST /professores` (ou torná-lo admin-only) e/ou obrigá-lo a usar hash.
- O comentário "register tem permitAll temporariamente" sugere intenção de fechar depois; registrar professor público é aceitável para um sistema onde só existem professores, mas vale documentar/decidir.

**Veredito:** Concluído (com ressalva). Todas as exigências (CSRF off, STATELESS, rotas públicas/privadas, filtro conectado) estão atendidas; a role em `POST /professores` é discutível e o registro público é temporário.

---

### Tarefa 7 — Credenciais mascaradas em application.properties

`src/main/resources/application.properties`:
```properties
spring.datasource.username=postgres
spring.datasource.password=12345678
api.security.token.secret=${JWT_SECRET:default_secret}
```
- `username` e `password` do banco **fixos em texto puro** — não usam `${DB_USERNAME}`/`${DB_PASSWORD}`. **Falha direta do requisito.** A senha `12345678` exposta é um risco de segurança (e está versionada no repo).
- `api.security.token.secret=${JWT_SECRET:default_secret}` — usa a variável de ambiente ✓, mas tem fallback `default_secret` fraco e hardcoded: se `JWT_SECRET` não estiver definida, a aplicação sobe com um segredo conhecido e trivial. Recomendado: não ter fallback (falhar na inicialização) ou usar um default longo/aleatório apenas em dev. O `TokenService` lê via `@Value("${api.security.token.secret}")`.

**Veredito:** Parcial. Secret JWT lê de env var (com fallback fraco), mas as credenciais de banco não estão mascaradas.

---

## Problemas de segurança consolidados

1. **Credenciais de banco em texto puro** (`application.properties`: `username=postgres`, `password=12345678`) — versionadas no repositório. [Alta]
2. **Fallback de JWT secret fraco** (`default_secret`) — permite forjar tokens se `JWT_SECRET` não estiver definida. [Média-Alta]
3. **Senha persistida sem hash em `ProfessorService.create`/`update`** — `POST /professores` e `PUT /professores/{id}` gravam texto puro. [Alta]
4. **Autenticação quebrada em runtime** por `findByEmail` devolver projeção `UserDetails` (com `Professor` que não implementa `UserDetails`) e `ProfessorUserDetails` órfão. Login vira 401 e requisições autenticadas viram 500. [Crítica — bloqueia a Fase 2 funcionar]
5. **`AuthenticationController.register`** instancia `new BCryptPasswordEncoder()` em vez de injetar o bean. [Baixa]
6. **`SecurityFilter` não trata token inválido/expirado** — lança `RuntimeException` → 500 em vez de 401; sem null-check no usuário carregado. [Média]
7. **`AuthenticationDTO` sem validação** — registro aceita `nome=null`/email inválido. [Média]
8. **Cast `(Professor) auth.getPrincipal()`** em `/auth/login` — `ClassCastException` latente (não alcançado hoje porque auth falha antes). [Média (latente)]
9. **`jjwt 0.12.6` declarado e não usado**; a implementação usa `java-jwt` (auth0). [Baixa — dependência morta + desvio do brief]
10. **Token sem `professorId`** (subject é email) — impacta a Fase 3 (multi-tenant). [Média — dependência futura]
11. **`POST /professores` exige `ROLE_PROFESSOR`** enquanto `/auth/register` é público — caminhos duplicados/contraditórios, e o primeiro não faz hash. [Média]

---

## Verificação executada

- `./mvnw.cmd -o compile` → **BUILD SUCCESS** (41 arquivos `.class`). Confirma que o código compila (incluindo `findByEmail` com retorno `UserDetails`, que é válido em tempo de compilação).
- **Não** executei a aplicação nem testes (exigem PostgreSQL/Testcontainers; escopo era read-only). Os comportamentos de runtime descritos (login 401, requests 500, ClassCastException latente) são **inferidos** do código e do comportamento conhecido do Spring Data JPA (projeção de interface) e Spring Security (DaoAuthenticationProvider / construtor de UsernamePasswordAuthenticationToken). Não confirmados executando.

---

## Conclusões e recomendações (implementar nada — só orientação)

Para fechar a Fase 2 de fato:

1. **Corrigir o fluxo de autenticação (crítico):** trocar `ProfessorRepository.findByEmail` para devolver `Professor` (ou `Optional<Professor>`) e envelopar com a classe `ProfessorUserDetails` já existente no `ProfessorUserDetailsService` (e no `SecurityFilter`). Assim `getUsername()`=`email`, `getPassword()`=`senhaHash`, `getAuthorities()`=`[ROLE_PROFESSOR]` de verdade.
2. **Adicionar `professorId` ao token:** incluir claim com o id no `TokenService.gerarToken` e um método `extractProfessorId` — prepara a Fase 3 e satisfaz o brief.
3. **Hash no service de Professor:** injetar `PasswordEncoder` em `ProfessorService` e aplicar `encode` em `create`/`update` (ou bloquear alteração de senha por esses endpoints e exigir `/auth/register`).
4. **Injetar o bean `PasswordEncoder`** no `AuthenticationController.register` em vez de `new BCryptPasswordEncoder()`.
5. **Tratar token inválido/expirado no `SecurityFilter`** (try/catch → não popular contexto / devolver 401) e adicionar null-check no usuário.
6. **Remover o cast `(Professor) auth.getPrincipal()`** em `/auth/login` — usar o `UserDetails` (ou ler do token) para gerar o token.
7. **Mascarar credenciais:** `${DB_USERNAME}`/`${DB_PASSWORD}` e remover o fallback `default_secret` do JWT secret (ou torná-lo longo/aleatório e só para dev).
8. **Decidir `POST /professores`:** remover `hasRole("PROFESSOR")` (ou tornar admin) e garantir hash, para não competir com `/auth/register`.
9. **Adicionar validação em `AuthenticationDTO`** (`@NotBlank` em login/password/nome, `@Email` em login) ou separar DTOs de login e registro.
10. **Remover `jjwt` não usado** do `pom.xml` ou migrar o `TokenService` para `jjwt` (alinhando ao brief).
