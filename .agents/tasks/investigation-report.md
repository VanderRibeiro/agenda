# Investigation Report — Agenda API

## Summary

O projeto está no **estado inicial de scaffolding**. A estrutura de pacotes existe (entities, repositories, services, controllers, config, exception), as 4 entidades principais estão modeladas corretamente, e cada entidade tem um CRUD básico completo (controller + service + repository). Porém, nenhuma regra de negócio foi implementada: não há DTOs, não há camada de exceções customizadas, não há segurança JWT funcional, não há validações Bean Validation nas entidades/DTOs, não há migrações Flyway, não há Docker, e o `application.properties` expõe credenciais em texto puro. O projeto não compilará em produção sem banco de dados local, e nenhuma das regras de negócio definidas em `regras-negocio.md` está codificada.

---

## 1. Stack e Dependências (pom.xml)

**O que existe:**
- Spring Boot 4.1.0, Java 21
- spring-boot-starter-webmvc, data-jpa, security, validation
- PostgreSQL driver + Flyway (flyway-database-postgresql)
- H2 (runtime — provavelmente para testes futuros)
- Lombok (configurado no maven-compiler-plugin como annotation processor)
- spring-boot-devtools
- springdoc-openapi 3.1.0 (Swagger UI)
- jjwt 0.12.6 (api + impl + jackson) — dependência JWT presente mas sem uso
- Testcontainers com PostgreSQL para testes de integração

**O que está incompleto/problemático:**
- `spring-boot-starter-data-jpa-test`, `spring-boot-starter-flyway-test`, `spring-boot-starter-validation-test`, `spring-boot-starter-webmvc-test` — esses artefatos (`spring-boot-starter-*-test`) **não existem no repositório Maven Central**. São nomes inválidos; o correto seria `spring-boot-starter-test` com a dependência específica incluída separadamente. Isso causará falha de build.
- Lombok declarado como `optional` mas **sem `@Data`/`@Getter`/`@Setter` em nenhuma entidade** — as entidades têm getters/setters manuais. Ou se adota Lombok nas entidades, ou remove a dependência.

---

## 2. Entidades (entities/)

### Professor.java
**Completo:** id (UUID), nome, email (unique), senhaHash, intervaloMinimo — alinhado com o ER do README.

**Ausente:**
- Anotações `@NotNull`, `@Email`, `@Size` (Bean Validation)
- `implements UserDetails` para integração com Spring Security
- Enum para papel/role (embora o sistema só tenha professores)

### Aluno.java
**Completo:** id, professorId, responsavelId, nome, telefone, endereco, formaPagamento, valorMensal, diaSemanaPadrao, horarioPadrao, duracaoPadraoMin, valorPadrao — todos os campos do ER.

**Ausente:**
- `formaPagamento` é `String` — deveria ser um `enum` (DIARIO, MENSAL) conforme RN-002 e RN-009
- `diaSemanaPadrao` é `String` — deveria ser um `enum` (DayOfWeek ou enum próprio)
- Bean Validation ausente (ex.: `@NotBlank` em nome)
- Sem relacionamento JPA (`@ManyToOne`) — usa IDs soltos

### Aula.java
**Completo:** id, professorId, alunoId, aulaOriginalId, data, horarioInicio, horarioFim, duracaoMinutos, valor, status, pago, datePagamento — todos os campos do ER.

**Ausente:**
- `status` é `String` — deveria ser um `enum` (AGENDADA, REALIZADA, CANCELADA, ADIADA) conforme RN-004
- Sem relacionamento JPA (`@ManyToOne`) — usa IDs soltos
- Bean Validation ausente
- Coluna `date_pagamento` deveria chamar `data_pagamento` (inconsistência de nome)

### Responsavel.java
**Completo:** id, professorId, nome, telefone.

**Ausente:**
- Bean Validation ausente (`@NotBlank` em nome e telefone, conforme RN-002: "nome e telefone são obrigatórios")
- Sem relacionamento JPA

---

## 3. Repositories (repositories/)

**O que existe:** 4 interfaces que estendem `JpaRepository<Entity, UUID>` — AlunoRepository, AulaRepository, ProfessorRepository, ResponsavelRepository.

**O que está completamente ausente (crítico para regras de negócio):**
- `AulaRepository`: sem query para verificar conflito de horário (RN-005), sem query por professor+data (agenda diária/mensal RN-006), sem query para indicadores financeiros (RN-010)
- `AlunoRepository`: sem `findAllByProfessorId(UUID)` — isolamento multi-tenant (RN-001)
- `ProfessorRepository`: sem `findByEmail(String)` — necessário para autenticação JWT
- `ResponsavelRepository`: sem `findAllByProfessorId(UUID)`

---

## 4. Services (service/)

**O que existe:** 4 services (AlunoService, AulaService, ProfessorService, ResponsavelService), cada um com `findAll()`, `findById()`, `create()`, `update()`, `deleteById()`.

**Problemas estruturais:**
- Todos os services fazem `findAll()` sem filtrar por `professorId` — viola RN-001 completamente. Qualquer professor veria dados de outro.
- `AulaService.update()` aceita alteração livre de todos os campos — sem validação de status, sem verificação de conflito de horário (RN-005)
- `AulaService.create()` não define `status = AGENDADA` automaticamente (RN-004: "toda nova aula inicia como AGENDADA")
- `AulaService.create()` não calcula `horarioFim` automaticamente (RN-003: "o horário final é calculado automaticamente")
- `ProfessorService`: sem lógica de hash de senha (armazena senha em texto puro)
- Mistura de `EntityNotFoundException` e `ResponseStatusException` no mesmo service para o mesmo caso (encontrar por ID) — inconsistente
- Nenhum service injeta o contexto do professor autenticado (ainda não existe SecurityContext/JWT)

**Completamente ausente:**
- Lógica de remarcação de aulas (RN-008)
- Lógica de conflito de horário (RN-005)
- Indicadores financeiros (RN-010)
- Isolamento multi-tenant (RN-001)

---

## 5. Controllers (controller/)

**O que existe:** 4 controllers REST com CRUD completo (GET all, GET by id, POST, PUT, DELETE) para Aluno, Aula, Professor, Responsavel.

**Problemas:**
- Nenhum controller usa DTOs — expõe entidades JPA diretamente no request/response (antipadrão de segurança e acoplamento)
- Sem `@Valid` / `@Validated` nas anotações `@RequestBody` — Bean Validation nunca é acionado
- Sem anotações Swagger (`@Operation`, `@Tag`) apesar do springdoc estar no classpath
- `ProfessorController` expõe `POST /professores` como cadastro livre — deveria ser endpoint de registro com hash de senha
- Nenhum endpoint de autenticação (`POST /auth/login`) existe
- Sem endpoint de remarcação, sem endpoint de indicadores financeiros, sem endpoint de agenda (visão mensal/diária)
- `AulaController` usa `PUT` puro sem endpoint específico para `PATCH /aulas/{id}/status` (alterar status é caso de uso distinto)

---

## 6. SecurityConfig (config/SecurityConfig.java)

**O que existe:** `@Configuration` + `@EnableWebSecurity` que **desabilita CSRF e permite todas as requisições sem autenticação**.

**Estado:** Placeholder/stub deliberado. O comentário na classe diz explicitamente que a autenticação está desabilitada.

**O que está completamente ausente:**
- `JwtAuthenticationFilter` (não existe)
- `JwtTokenProvider` / `JwtService` (não existe)
- `UserDetailsService` implementando busca de Professor por email (não existe)
- `AuthenticationManager` bean
- `PasswordEncoder` bean (BCrypt)
- Configuração de rotas protegidas vs. públicas (ex.: `/auth/**` público, resto autenticado)
- Extração do `professorId` do JWT para o `SecurityContext` (multi-tenant via token)

---

## 7. Camada de Exceções (exception/) — VAZIO

**O que existe:** Pacote criado, mas **nenhum arquivo Java dentro**.

**O que está completamente ausente:**
- `ResourceNotFoundException` (ou `EntityNotFoundException` customizada)
- `BusinessException` (para violações de regras de negócio, ex.: conflito de horário)
- `GlobalExceptionHandler` (`@RestControllerAdvice`) que converta exceções em respostas HTTP padronizadas
- Classe de resposta de erro padronizada (ex.: `ErrorResponse { timestamp, status, message, path }`)

Atualmente os services usam `EntityNotFoundException` do Jakarta (não capturada pelo Spring MVC → retorna 500) e `ResponseStatusException` do Spring (misturados no mesmo service).

---

## 8. DTOs — AUSENTES (pacote inexistente)

Não existe pacote `dto/` no projeto. **Nenhum DTO foi criado.**

**O que falta criar:**
- `CadastrarAlunoRequest` / `AlunoResponse`
- `CadastrarAulaRequest` / `AulaResponse`
- `RegistrarProfessorRequest` / `ProfessorResponse`
- `CadastrarResponsavelRequest` / `ResponsavelResponse`
- `LoginRequest` / `LoginResponse` (com JWT)
- `AlterarStatusAulaRequest`
- `ReagendarAulaRequest`
- `IndicadoresFinanceirosResponse`

---

## 9. Flyway (Migrações) — AUSENTE

Flyway está declarado no `pom.xml`, mas **não existe a pasta `src/main/resources/db/migration/`** nem nenhum arquivo `.sql`.

Sem migrations, o Flyway falhará ao iniciar (sem a tabela `flyway_schema_history`). Atualmente `ddl-auto=update` no `application.properties` está servindo como substituto temporário, o que é inadequado para produção.

**O que falta criar:**
- `V1__create_professores.sql`
- `V2__create_responsaveis.sql`
- `V3__create_alunos.sql`
- `V4__create_aulas.sql`

---

## 10. application.properties — INCOMPLETO

**O que existe:** URL do PostgreSQL, usuário, senha, driver, `ddl-auto=update`, `format_sql=true`.

**Problemas:**
- Credenciais de banco (`username=postgres`, `password=12345678`) em texto puro — deveriam usar variáveis de ambiente (`${DB_USERNAME}`, `${DB_PASSWORD}`)
- Nenhum `spring.security.jwt.secret` ou propriedade de configuração JWT
- Sem `spring.flyway.*` explícito (localizações de migrations, baseline)
- Sem perfis separados (`application-dev.properties`, `application-prod.properties`)
- `spring.jpa.show-sql` ausente (útil para dev)

---

## 11. Testes (test/)

**O que existe:**
- `AgendaApiApplicationTests` — apenas `contextLoads()` com `@SpringBootTest`
- `TestcontainersConfiguration` — configura PostgreSQL via Testcontainers com `@ServiceConnection`
- `TestAgendaApiApplication` — runner alternativo com Testcontainers

**O que está completamente ausente:**
- Testes unitários de services (regras de negócio)
- Testes de integração de controllers
- Nenhum teste de segurança

---

## 12. Docker — AUSENTE

Não existe `Dockerfile`, `docker-compose.yml` nem qualquer arquivo de infraestrutura.

---

## Conclusões e Recomendações por Fase

### Fase 1 — Fundação (Exceções, Enums, Bean Validation, DTOs)
1. Criar enums `StatusAula`, `FormaPagamento`, `DiaSemana` no pacote `entities/enums/`
2. Criar exceções customizadas: `ResourceNotFoundException`, `BusinessException`, `GlobalExceptionHandler`, `ErrorResponseDTO`
3. Adicionar `@NotNull`/`@NotBlank`/`@Email`/`@Size` nas entidades e criar DTOs de request/response para todos os recursos
4. Adicionar `@Valid` nos controllers

### Fase 2 — Segurança JWT e Autenticação
1. Implementar `PasswordEncoder` (BCrypt) no `SecurityConfig`
2. Criar `JwtService` (geração e validação de tokens usando jjwt 0.12.6)
3. Criar `JwtAuthenticationFilter` (extrai `professorId` do JWT e popula `SecurityContext`)
4. Implementar `UserDetailsService` buscando professor por email
5. Criar endpoint `POST /auth/login` e `POST /auth/registro`
6. Configurar rotas protegidas no `SecurityConfig`
7. Mascarar credenciais com variáveis de ambiente no `application.properties`

### Fase 3 — Regras de Negócio nos Services
1. Isolamento multi-tenant: todos os `findAll()` filtram por `professorId` extraído do JWT
2. `AulaService.create()`: status inicial = AGENDADA, calcular `horarioFim` automaticamente
3. `AulaService`: verificação de conflito de horário (RN-005) com `intervaloMinimo` do professor
4. Lógica de remarcação: criar nova aula + marcar original como ADIADA (RN-008)
5. Endpoint e lógica de alteração de status com validações de transição (RN-004)
6. Indicadores financeiros: valor ganho, a receber, canceladas, adiadas (RN-010)
7. Queries customizadas nos repositories: `findByProfessorIdAndData`, `findByEmail`, verificação de sobreposição de horário

### Fase 4 — Banco de Dados e Migrações Flyway
1. Criar `src/main/resources/db/migration/` com scripts SQL V1–V4
2. Remover `ddl-auto=update` e usar `validate` em produção
3. Ajustar nomes de colunas (ex.: `date_pagamento` → `data_pagamento`)

### Fase 5 — Docker e Infraestrutura
1. `Dockerfile` multi-stage (build com Maven + runtime com JRE)
2. `docker-compose.yml` com serviços `api` e `postgres` com variáveis de ambiente

### Fase 6 — Qualidade (Testes e Documentação Swagger)
1. Testes unitários de services (Mockito) para regras de negócio críticas
2. Testes de integração de controllers com MockMvc + Testcontainers
3. Anotações `@Tag`, `@Operation`, `@ApiResponse` nos controllers para Swagger UI
