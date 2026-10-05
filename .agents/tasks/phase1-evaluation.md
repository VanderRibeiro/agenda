# Avaliação da Fase 1 — agenda-api

## Resumo Executivo

A Fase 1 foi **amplamente implementada**, mas com **dois problemas críticos** que comprometem o design pretendido:

1. **Os controllers ainda recebem e retornam Entidades diretamente**, não os DTOs criados. Os DTOs existem mas não são usados.
2. **Dois `@RestControllerAdvice` registrados simultaneamente** tratam as mesmas exceções (`ResourceNotFoundException`, `BusinessException`), o que causa comportamento ambíguo no Spring.

Os demais itens (enums, exceções, `ErrorResponseDTO`, validações nas entidades) estão corretos.

---

## Avaliação por Tarefa

### ✅ Tarefa 1 — Enums (`StatusAula`, `FormaPagamento`, `DiaSemana`)

**Status: Feito corretamente.**

Todos os três enums estão em `entities/enums/` com os valores corretos:

| Enum | Valores |
|------|---------|
| `StatusAula` | AGENDADA, REALIZADA, CANCELADA, ADIADA |
| `FormaPagamento` | DIARIO, MENSAL |
| `DiaSemana` | SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO, DOMINGO |

Nenhum problema encontrado.

---

### ✅ Tarefa 2 — Exceções (`ResourceNotFoundException` e `BusinessException`)

**Status: Feito corretamente, com arquitetura bem estruturada.**

Há uma hierarquia limpa:
- `ApplicationException` (base) — armazena `HttpStatus`, estende `RuntimeException`
- `ResourceNotFoundException` — estende `ApplicationException` com `HttpStatus.NOT_FOUND`
- `BusinessException` — estende `ApplicationException` com `HttpStatus.BAD_REQUEST`

Ambas no pacote `com.brunoribeiro.exception`. A arquitetura com classe base é boa prática.

---

### ✅ Tarefa 3 — `ErrorResponseDTO` (DTO padronizado de erro)

**Status: Feito corretamente.**

Arquivo: `exception/dto/ErrorResponseDTO.java`

Contém todos os campos esperados:
- `Instant timestamp` ✅
- `int status` ✅
- `String message` ✅
- `String path` ✅

Tem construtor completo, construtor vazio e getters/setters. Correto.

---

### ⚠️ Tarefa 4 — `GlobalExceptionHandler` com `@RestControllerAdvice`

**Status: Feito, mas com problema de duplicidade.**

**Problema crítico:** existem **dois** `@RestControllerAdvice` no mesmo pacote:

#### `exception/handler/GlobalExceptionHandler.java`
Trata: `MethodArgumentNotValidException`, `ConstraintViolationException`, `HttpMessageNotReadableException`, `ApplicationException` (base), `Exception` (genérico).

#### `exception/handler/ApiExceptionHandler.java`
Trata: `ResourceNotFoundException`, `BusinessException` — que já são subclasses de `ApplicationException`, já tratadas pelo `GlobalExceptionHandler`.

**Por que isso é um problema:**
- O Spring não garante qual handler vai capturar `ResourceNotFoundException` ou `BusinessException` — depende da ordem de carregamento de beans.
- `ApiExceptionHandler` usa `HttpServletRequest` para extrair o path; `GlobalExceptionHandler` usa `WebRequest`. Podem retornar formatos diferentes para o mesmo tipo de erro.
- Há redundância total: `ApiExceptionHandler` não acrescenta nada além do que `GlobalExceptionHandler` já faz via polimorfismo (`ApplicationException` captura ambas as subclasses).

**Recomendação:** remover `ApiExceptionHandler.java` inteiramente. O `GlobalExceptionHandler` já cobre todos os casos pela hierarquia de exceções.

**Observação menor:** o comentário no `GlobalExceptionHandler` diz *"Controller ainda não possui `@Valid` e `@RequestBody`"*, mas isso já foi implementado — o comentário está desatualizado.

---

### ✅ Tarefa 5 — Bean Validation nas entidades

**Status: Feito corretamente.**

| Entidade | Validações presentes |
|----------|---------------------|
| `Aluno` | `@NotNull` (professorId), `@NotBlank` + `@Size` (nome, telefone), `@Email` + `@Size` (email), `@Size` (endereco) |
| `Aula` | `@NotNull` em todos os campos obrigatórios (professorId, alunoId, data, horarioInicio, horarioFim, duracaoMinutos, valor, status, pago) |
| `Professor` | `@NotBlank` + `@Size` (nome, email, senhaHash), `@Email` (email), `@NotNull` (intervaloMinimo) |
| `Responsavel` | `@NotBlank` + `@Size` (nome, telefone), `@NotNull` (professorId) |

**Observação sobre `Aluno.email`:** o campo tem `@Email` mas não tem `@NotBlank`. Se o email for obrigatório, falta o `@NotBlank`. No `AlunoRequestDTO` o email tem `@NotBlank`, mas na entidade não. Inconsistência entre entidade e DTO.

---

### ⚠️ Tarefa 6 — Pacote `dto/` com Request/Response DTOs

**Status: Feito, mas com lacunas nos RequestDTOs.**

A estrutura está correta: `dto/request/` e `dto/response/`, todos como Java Records.

#### ResponseDTOs — Corretos ✅

| DTO | Campos |
|-----|--------|
| `AlunoResponseDTO` | id, professorId, responsavelId, nome, email, telefone, endereco |
| `AulaResponseDTO` | id, professorId, alunoId, aulaOriginalId, data, horarioInicio, horarioFim, duracaoMinutos, valor, status, pago, datePagamento |
| `ProfessorResponseDTO` | id, nome, email, intervaloMinimo |
| `ResponsavelResponseDTO` | id, professorId, nome, telefone |

Os ResponseDTOs refletem adequadamente os campos das entidades.

#### RequestDTOs — Incompletos ⚠️

**`AlunoRequestDTO`:** faltam os campos de configuração de aula padrão que existem na entidade:
- `formaPagamento` (FormaPagamento) — ausente
- `valorMensal` (BigDecimal) — ausente
- `diaSemanaPadrao` (DiaSemana) — ausente
- `horarioPadrao` (LocalTime) — ausente
- `duracaoPadraoMin` (Integer) — ausente
- `valorPadrao` (BigDecimal) — ausente

Com o DTO atual, é impossível criar/atualizar um aluno com forma de pagamento ou configurações de aula padrão via API.

**`AulaRequestDTO`:** completo, cobre todos os campos da entidade `Aula`. ✅

**`ProfessorRequestDTO`:** completo, cobre todos os campos da entidade `Professor`. ✅

**`ResponsavelRequestDTO`:** completo, cobre todos os campos da entidade `Responsavel`. ✅

---

### ❌ Tarefa 7 — `@Valid` nos `@RequestBody` dos controllers

**Status: Parcialmente feito — `@Valid` está presente, mas os controllers ainda usam as Entidades, não os DTOs.**

O `@Valid` foi adicionado a todos os `@RequestBody` em todos os controllers:

| Controller | Métodos com @Valid |
|------------|--------------------|
| `AulaController` | `create`, `update` ✅ |
| `AlunoController` | `create`, `update` ✅ |
| `ProfessorController` | `create`, `update` ✅ |
| `ResponsavelController` | `create`, `update` ✅ |

**Problema crítico:** os controllers recebem e retornam as **entidades JPA diretamente**, não os DTOs:

```java
// AulaController.java — usando entidade, não DTO
public Aula create(@RequestBody @Valid Aula aula) { ... }
public Aula update(@PathVariable UUID id, @RequestBody @Valid Aula aula) { ... }

// Deveria ser:
public AulaResponseDTO create(@RequestBody @Valid AulaRequestDTO dto) { ... }
public AulaResponseDTO update(@PathVariable UUID id, @RequestBody @Valid AulaRequestDTO dto) { ... }
```

Isso vale para todos os quatro controllers. Os DTOs foram criados mas nunca conectados aos controllers. Consequências:
- A API expõe campos internos da entidade que não devem ser recebidos pela API (ex: o cliente pode enviar o `id` no corpo do POST).
- As validações dos DTOs (ex: `@NotBlank` em `AlunoRequestDTO.email`) nunca são disparadas — o `@Valid` valida a entidade, não o DTO.
- Os métodos de leitura (`findAll`, `findById`) também retornam entidades em vez de ResponseDTOs.

---

## Conclusão e Recomendações

### O que está pronto e correto
- Enums com os valores corretos
- Hierarquia de exceções bem estruturada
- `ErrorResponseDTO` com todos os campos necessários
- Bean Validation nas entidades (com ressalva menor no `Aluno.email`)
- `@Valid` adicionado nos controllers

### O que precisa ser corrigido

**Prioridade alta:**

1. **Conectar DTOs aos controllers** — substituir `Aula`/`Aluno`/`Professor`/`Responsavel` por `XxxRequestDTO`/`XxxResponseDTO` em todos os controllers, e mapear entre DTO ↔ entidade nas services.

2. **Remover `ApiExceptionHandler.java`** — é redundante e conflita com `GlobalExceptionHandler`. Manter apenas `GlobalExceptionHandler`.

**Prioridade média:**

3. **Adicionar campos ausentes em `AlunoRequestDTO`** — incluir `formaPagamento`, `valorMensal`, `diaSemanaPadrao`, `horarioPadrao`, `duracaoPadraoMin`, `valorPadrao`.

4. **Adicionar `@NotBlank` em `Aluno.email`** — para consistência com o `AlunoRequestDTO` e para garantir que a validação seja acionada na entidade também.

**Prioridade baixa:**

5. **Atualizar comentário desatualizado** em `GlobalExceptionHandler` que diz que controllers ainda não têm `@Valid`.

6. **Considerar retornar `ResponseEntity<XxxResponseDTO>`** nos métodos dos controllers para ter controle explícito do status HTTP (201 Created no POST, etc.).
