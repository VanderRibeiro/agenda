# Avaliação Final — Fase 1 (Leitura completa e fresca do código)

> Data da avaliação: leitura direta de todos os arquivos relevantes.  
> Nenhum cache ou relatório anterior foi reutilizado.

---

## Resumo Executivo

| # | Tópico | Status |
|---|--------|--------|
| 1 | Enums | ✅ Correto |
| 2 | Exceções | ✅ Correto |
| 3 | GlobalExceptionHandler | ⚠️ Parcial — handler duplicado em nível lógico |
| 4 | Bean Validation nas Entidades | ✅ Correto |
| 5 | DTOs (request/response) | ⚠️ Parcial — AulaController bypassa validação de email no update; AulaRequestDTO recebe enum diretamente sem safe-parsing |
| 6 | Controllers | ⚠️ Parcial — PUT retorna entidade crua sem ResponseEntity e sem status explícito |
| 7 | Services | ⚠️ Parcial — `AlunoService.create()` não chama `validarEmailObrigatorioSemResponsavel` antes de salvar (análise detalhada abaixo) |

Resultado geral: **a Fase 1 está em boa forma, com 4 itens aprovados e 3 itens com problemas pontuais e corrigíveis**.

---

## 1. Enums — ✅ Correto

**Arquivos:** `entities/enums/StatusAula.java`, `FormaPagamento.java`, `DiaSemana.java`

Todos os valores exigidos estão presentes:

```java
// StatusAula.java
public enum StatusAula { AGENDADA, REALIZADA, CANCELADA, ADIADA }

// FormaPagamento.java
public enum FormaPagamento { DIARIO, MENSAL }

// DiaSemana.java
public enum DiaSemana { SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO, DOMINGO }
```

Nenhum problema encontrado.

---

## 2. Exceções — ✅ Correto

**Arquivos:** `exception/ApplicationException.java`, `exception/BusinessException.java`, `exception/ResourceNotFoundException.java`, `exception/dto/ErrorResponseDTO.java`

- `ApplicationException` é a base, estende `RuntimeException`, carrega `HttpStatus`.
- `BusinessException` → `ApplicationException(HttpStatus.BAD_REQUEST)`.
- `ResourceNotFoundException` → `ApplicationException(HttpStatus.NOT_FOUND)`.
- Ambas as subclasses oferecem construtores com e sem `Throwable cause`.
- `ErrorResponseDTO` possui os 4 campos esperados: `timestamp`, `status`, `message`, `path`, com construtores e getters/setters completos.

Hierarquia correta, sem problemas.

---

## 3. GlobalExceptionHandler — ⚠️ Parcial

**Arquivo:** `exception/handler/GlobalExceptionHandler.java`

A classe possui `@RestControllerAdvice` ✅ e trata as exceções esperadas, porém há um **problema de precedência e redundância**:

### Problema: handlers duplicados em nível lógico para `ResourceNotFoundException` e `BusinessException`

O handler específico para `ResourceNotFoundException`:
```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
}
```

E o handler genérico para `ApplicationException`:
```java
@ExceptionHandler(ApplicationException.class)
public ResponseEntity<ErrorResponseDTO> handleApplicationException(ApplicationException ex, WebRequest request) { ... }
```

Como `ResourceNotFoundException` e `BusinessException` estendem `ApplicationException`, **os handlers específicos nunca são alcançados em situações onde o Spring escolhe o mais específico** — na prática o Spring SÍ escolhe o mais específico (subclasse), então os handlers específicos funcionam corretamente. O risco real é:

1. **Inconsistência de parâmetro**: os handlers específicos recebem `HttpServletRequest` enquanto os demais recebem `WebRequest`, levando a dois overloads do método privado `buildResponse`. Isso não é bug, mas é inconsistente e pode causar confusão.
2. **Duplicação desnecessária**: o `handleApplicationException` já cobre `ResourceNotFoundException` e `BusinessException` através da hierarquia, com status dinâmico via `ex.getStatus()`. Os handlers específicos sobrepõem essa lógica usando status hardcoded (`NOT_FOUND`, `BAD_REQUEST`), o que é correto mas redundante.

### Recomendação
Remover os handlers específicos de `ResourceNotFoundException` e `BusinessException`, deixando apenas o `handleApplicationException` (que resolve o status via `ex.getStatus()`). Isso elimina a inconsistência e a duplicação sem alterar o comportamento.

---

## 4. Bean Validation nas Entidades — ✅ Correto

**Arquivo:** `entities/Aluno.java`

O campo `email` na entidade `Aluno`:
```java
@Email(message = "Informe um email válido")
@Size(max = 150, message = "O email do aluno deve ter no máximo 150 caracteres")
@Column(name = "email", length = 150)
private String email;
```

**Não há `@NotBlank` no email da entidade** ✅ — a obrigatoriedade condicional é tratada no DTO e no service.

As demais entidades (`Professor`, `Aula`, `Responsavel`) têm validações adequadas para seus campos obrigatórios.

---

## 5. DTOs — ⚠️ Parcial

### AlunoRequestDTO — ✅ Correto

**Arquivo:** `dto/request/AlunoRequestDTO.java`

- ✅ Todos os 6 campos de agenda presentes: `formaPagamento`, `valorMensal`, `diaSemanaPadrao`, `horarioPadrao`, `duracaoPadraoMin`, `valorPadrao`.
- ✅ Sem `@NotBlank` no email.
- ✅ Cross-field `@AssertTrue` implementado:
  ```java
  @AssertTrue(message = "O email do aluno é obrigatório quando não há responsável")
  public boolean isEmailObrigatorioQuandoSemResponsavel() {
      return responsavelId != null || (email != null && !email.isBlank());
  }
  ```
- ✅ `toEntity()` com safe enum parsing via `try/catch` em `valueOf` para `FormaPagamento` e `DiaSemana`, lançando `BusinessException` com mensagem descritiva.

### AlunoResponseDTO — ✅ Correto

**Arquivo:** `dto/response/AlunoResponseDTO.java`

Todos os 6 campos de agenda estão presentes: `formaPagamento`, `valorMensal`, `diaSemanaPadrao`, `horarioPadrao`, `duracaoPadraoMin`, `valorPadrao`. Factory method `fromEntity` completo.

### AulaRequestDTO — ⚠️ Parcial

**Arquivo:** `dto/request/AulaRequestDTO.java`

O campo `status` é declarado diretamente como `StatusAula`:
```java
@NotNull(message = "O status da aula é obrigatório")
StatusAula status,
```

Isso delega o parse do enum ao desserializador Jackson. Se o JSON contiver um valor inválido (ex: `"INVALIDO"`), Jackson lança `HttpMessageNotReadableException` — que é capturada pelo `GlobalExceptionHandler` com a mensagem genérica "JSON inválido ou campo com formato incorreto". **Tecnicamente funciona**, mas ao contrário do `AlunoRequestDTO` (que usa `String` + `try/catch`), a mensagem de erro é menos informativa. Não há `toEntity()` personalizado além do mapeamento direto.

### ProfessorRequestDTO — ✅ Correto

**Arquivo:** `dto/request/ProfessorRequestDTO.java`

Todos os campos obrigatórios com validações corretas. `toEntity()` presente e completo.

### ResponsavelRequestDTO — ✅ Correto

**Arquivo:** `dto/request/ResponsavelRequestDTO.java`

Campos e validações corretos. `toEntity()` presente.

### ProfessorResponseDTO — ✅ Correto (com observação)

**Arquivo:** `dto/response/ProfessorResponseDTO.java`

Retorna `id`, `nome`, `email`, `intervaloMinimo`. **Não expõe `senhaHash`** ✅ (correto por segurança).

### AulaResponseDTO — ✅ Correto

**Arquivo:** `dto/response/AulaResponseDTO.java`

Todos os campos da entidade `Aula` mapeados.

### ResponsavelResponseDTO — ✅ Correto

**Arquivo:** `dto/response/ResponsavelResponseDTO.java`

Campos corretos.

---

## 6. Controllers — ⚠️ Parcial

**Arquivos:** `AlunoController.java`, `AulaController.java`, `ProfessorController.java`, `ResponsavelController.java`

### POST retorna 201 ✅

Todos os controllers usam `ResponseEntity.status(HttpStatus.CREATED)` no POST. Exemplo:
```java
// AlunoController.java
@PostMapping
public ResponseEntity<AlunoResponseDTO> create(@RequestBody @Valid AlunoRequestDTO alunoRequestDTO) {
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(AlunoResponseDTO.fromEntity(alunoService.create(alunoRequestDTO.toEntity())));
}
```

### `@Valid` presente ✅

Todos os `@RequestBody` têm `@Valid`.

### Problema: PUT não usa `ResponseEntity` ⚠️

Em todos os 4 controllers, o método `update` retorna o DTO diretamente, sem `ResponseEntity`:
```java
// AlunoController.java
@PutMapping("/{id}")
public AlunoResponseDTO update(@PathVariable UUID id, @RequestBody @Valid AlunoRequestDTO alunoRequestDTO) {
    return AlunoResponseDTO.fromEntity(alunoService.update(id, alunoRequestDTO.toEntity()));
}
```

O Spring implicitamente retorna HTTP 200, então **funcionalmente não é um erro**, mas a inconsistência com o POST (que usa `ResponseEntity`) é um desvio de boa prática e dificulta customizações futuras (headers, status diferente).

### Problema: AlunoController.update bypassa o @AssertTrue ⚠️

```java
// AlunoController.java
@PutMapping("/{id}")
public AlunoResponseDTO update(@PathVariable UUID id, @RequestBody @Valid AlunoRequestDTO alunoRequestDTO) {
    return AlunoResponseDTO.fromEntity(alunoService.update(id, alunoRequestDTO.toEntity()));
}
```

O `@Valid` está presente, então o `@AssertTrue` do DTO **é validado** antes de chamar `toEntity()`. Isso é correto ✅.

---

## 7. Services — ⚠️ Parcial

**Arquivos:** `service/AlunoService.java`, `AulaService.java`, `ProfessorService.java`, `ResponsavelService.java`

### AlunoService — Análise Detalhada

```java
// AlunoService.java
public Aluno create(Aluno aluno) {
    if (aluno == null) {
        throw new BusinessException("Dados do aluno não informados.");
    }
    validarEmailObrigatorioSemResponsavel(aluno);  // ✅ CHAMADO ANTES DE SALVAR
    return alunoRepository.save(aluno);
}
```

✅ `create` chama `validarEmailObrigatorioSemResponsavel` — **correto**.

```java
public Aluno update(UUID id, Aluno obj) {
    if (obj == null) {
        throw new BusinessException("Dados do aluno não informados.");
    }
    Aluno entity = alunoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado, ID: " + id));
    updateAluno(entity, obj);
    validarEmailObrigatorioSemResponsavel(entity);  // ✅ CHAMADO APÓS MERGE
    return alunoRepository.save(entity);
}
```

✅ `update` também chama a validação após o merge — **correto**.

```java
public void updateAluno(Aluno aluno, Aluno obj) {
    aluno.setProfessorId(obj.getProfessorId());
    aluno.setResponsavelId(obj.getResponsavelId());
    aluno.setNome(obj.getNome());
    aluno.setEmail(obj.getEmail());        // ✅ email é copiado
    aluno.setTelefone(obj.getTelefone());
    aluno.setEndereco(obj.getEndereco());
    aluno.setFormaPagamento(obj.getFormaPagamento());
    aluno.setValorMensal(obj.getValorMensal());
    aluno.setDiaSemanaPadrao(obj.getDiaSemanaPadrao());
    aluno.setHorarioPadrao(obj.getHorarioPadrao());
    aluno.setDuracaoPadraoMin(obj.getDuracaoPadraoMin());
    aluno.setValorPadrao(obj.getValorPadrao());
}
```

✅ `updateAluno` copia o campo `email` — **correto**.

### Problema em AlunoService — guard de null input no update ⚠️

O `obj == null` guard funciona, mas ele é **checado no nível da entidade**, não no nível do DTO. Na prática, `alunoRequestDTO.toEntity()` nunca retorna `null` (é um `new Aluno()`), então este guard nunca disparará para a rota PUT. O guard faz mais sentido se o service recebesse o DTO diretamente, mas como está, é tecnicamente seguro porém ineficaz.

### ProfessorService — ✅ Correto

- `ResourceNotFoundException` no `findById`.
- `BusinessException` em `create` e `update` quando `obj == null`.
- `updateProfessor` copia todos os campos.

### ResponsavelService — ✅ Correto

- `ResourceNotFoundException` no `findById`.
- `BusinessException` em `create` e `update` quando `obj == null`.
- `updateResponsavel` copia todos os campos.

### AulaService — ✅ Correto

- `ResourceNotFoundException` no `findById`.
- `BusinessException` em `create` e `update` quando `obj == null`.
- `updateAula` copia todos os campos.

---

## Conclusões e Recomendações

### Problemas que devem ser corrigidos

| Prioridade | Arquivo | Problema | Correção |
|------------|---------|----------|----------|
| Alta | `GlobalExceptionHandler.java` | Handlers para `ResourceNotFoundException` e `BusinessException` são redundantes com `handleApplicationException`; inconsistência no parâmetro (`HttpServletRequest` vs `WebRequest`) | Remover os handlers específicos das subclasses e deixar apenas `handleApplicationException` para resolver status dinamicamente via `ex.getStatus()` |
| Média | `AulaController.java`, `AlunoController.java`, `ProfessorController.java`, `ResponsavelController.java` | Método `update` retorna DTO diretamente em vez de `ResponseEntity<XxxResponseDTO>` | Converter para `ResponseEntity.ok(...)` para consistência com o padrão do POST |
| Baixa | `AulaRequestDTO.java` | Campo `status` recebe `StatusAula` diretamente; erro de valor inválido retorna mensagem genérica | Mudar para `String` e aplicar `try/catch` em `StatusAula.valueOf()` no `toEntity()`, igual ao padrão de `FormaPagamento` e `DiaSemana` no `AlunoRequestDTO` |

### Itens aprovados sem ressalvas

- ✅ Todos os 3 enums com os valores corretos
- ✅ Hierarquia de exceções (`ApplicationException` → `BusinessException`, `ResourceNotFoundException`)
- ✅ `ErrorResponseDTO` completo
- ✅ `Aluno.email` sem `@NotBlank` na entidade
- ✅ `AlunoRequestDTO`: sem `@NotBlank` no email, `@AssertTrue` cross-field, safe enum parsing, todos os 6 campos de agenda
- ✅ `AlunoResponseDTO`: todos os 6 campos de agenda
- ✅ `AlunoService`: `validarEmailObrigatorioSemResponsavel` chamado em `create` E em `update`, `email` copiado no `updateAluno`
- ✅ Todos os controllers: `@RequestBody @Valid` nas operações de escrita, POST retorna 201
- ✅ `ProfessorResponseDTO` não expõe `senhaHash`
