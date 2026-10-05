# Re-avaliação: Uso de DTOs nos Controllers — agenda-api

**Data:** pós-correção aplicada  
**Escopo:** Controllers (Aluno, Aula, Professor, Responsavel), Services correspondentes, e pacote `dto/`

---

## Resumo Executivo

**Os controllers foram corrigidos corretamente.** Todos os quatro controllers agora utilizam RequestDTOs no recebimento e ResponseDTOs na devolução, com `@Valid` devidamente posicionado. A conversão DTO ↔ entity está sendo feita nos controllers (via `toEntity()` / `fromEntity()`), e os services continuam operando exclusivamente com entidades JPA — o que é aceitável para a arquitetura atual.

Existem **dois problemas residuais** que não comprometem o funcionamento básico mas representam inconsistências de design:

1. **`AlunoService.updateAluno` e `AlunoRequestDTO.toEntity()` estão dessincronizados** — o service atualiza campos que o DTO não expõe (campos de agenda: `formaPagamento`, `valorMensal`, `diaSemanaPadrao`, `horarioPadrao`, `duracaoPadraoMin`, `valorPadrao`), o que causa sua zeragem silenciosa em todo PUT.
2. **Validações de Bean Validation ainda existem nas entidades JPA**, o que é redundante e pode causar double-validation inesperada.

---

## Evidências por Controller

---

### 1. AlunoController

**Arquivo:** `src/main/java/com/brunoribeiro/controller/AlunoController.java`

#### Request bodies usando RequestDTO? ✅
- `POST /alunos` → `@RequestBody @Valid AlunoRequestDTO alunoRequestDTO`
- `PUT /alunos/{id}` → `@RequestBody @Valid AlunoRequestDTO alunoRequestDTO`

#### Responses usando ResponseDTO? ✅
- `GET /alunos` → `List<AlunoResponseDTO>`
- `GET /alunos/{id}` → `AlunoResponseDTO`
- `POST /alunos` → `AlunoResponseDTO`
- `PUT /alunos/{id}` → `AlunoResponseDTO`
- `DELETE /alunos/{id}` → `ResponseEntity<Void>` ✅ (correto para deleção)

#### `@Valid` presente? ✅
Presente em POST e PUT corretamente: `@RequestBody @Valid AlunoRequestDTO`.

#### Mapeamento DTO ↔ entity? ✅ (com ressalva)
O controller converte corretamente:
```java
// POST
return AlunoResponseDTO.fromEntity(alunoService.create(alunoRequestDTO.toEntity()));

// PUT
return AlunoResponseDTO.fromEntity(alunoService.update(id, alunoRequestDTO.toEntity()));
```

#### ⚠️ Problema residual: dessincronização entre DTO e service no PUT

`AlunoRequestDTO.toEntity()` popula apenas: `professorId`, `responsavelId`, `nome`, `email`, `telefone`, `endereco`.

`AlunoService.updateAluno()` tenta copiar do objeto recebido também: `formaPagamento`, `valorMensal`, `diaSemanaPadrao`, `horarioPadrao`, `duracaoPadraoMin`, `valorPadrao`.

```java
// AlunoService.java — updateAluno()
aluno.setFormaPagamento(obj.getFormaPagamento());   // será null (vem do DTO)
aluno.setValorMensal(obj.getValorMensal());          // será null
aluno.setDiaSemanaPadrao(obj.getDiaSemanaPadrao());  // será null
aluno.setHorarioPadrao(obj.getHorarioPadrao());      // será null
aluno.setDuracaoPadraoMin(obj.getDuracaoPadraoMin()); // será null
aluno.setValorPadrao(obj.getValorPadrao());          // será null
```

Como `AlunoRequestDTO.toEntity()` não preenche esses campos, cada chamada `PUT /alunos/{id}` **zera silenciosamente os campos de agenda do aluno** que já estavam persistidos. Isso é um bug funcional.

---

### 2. AulaController

**Arquivo:** `src/main/java/com/brunoribeiro/controller/AulaController.java`

#### Request bodies usando RequestDTO? ✅
- `POST /aulas` → `@RequestBody @Valid AulaRequestDTO aulaRequestDTO`
- `PUT /aulas/{id}` → `@RequestBody @Valid AulaRequestDTO aulaRequestDTO`

#### Responses usando ResponseDTO? ✅
- `GET /aulas` → `List<AulaResponseDTO>`
- `GET /aulas/{id}` → `AulaResponseDTO`
- `POST /aulas` → `AulaResponseDTO`
- `PUT /aulas/{id}` → `AulaResponseDTO`
- `DELETE /aulas/{id}` → `ResponseEntity<Void>` ✅

#### `@Valid` presente? ✅

#### Mapeamento DTO ↔ entity? ✅
```java
return AulaResponseDTO.fromEntity(aulaService.create(aulaRequestDTO.toEntity()));
return AulaResponseDTO.fromEntity(aulaService.update(id, aulaRequestDTO.toEntity()));
```

`AulaRequestDTO.toEntity()` cobre todos os campos que `AulaService.updateAula()` escreve. Nenhuma dessincronização encontrada.

#### Problemas residuais: nenhum. ✅

---

### 3. ProfessorController

**Arquivo:** `src/main/java/com/brunoribeiro/controller/ProfessorController.java`

#### Request bodies usando RequestDTO? ✅
- `POST /professores` → `@RequestBody @Valid ProfessorRequestDTO professorRequestDTO`
- `PUT /professores/{id}` → `@RequestBody @Valid ProfessorRequestDTO professorRequestDTO`

#### Responses usando ResponseDTO? ✅
- `GET /professores` → `List<ProfessorResponseDTO>`
- `GET /professores/{id}` → `ProfessorResponseDTO`
- `POST /professores` → `ProfessorResponseDTO`
- `PUT /professores/{id}` → `ProfessorResponseDTO`
- `DELETE /professores/{id}` → `ResponseEntity<Void>` ✅

#### `@Valid` presente? ✅

#### Mapeamento DTO ↔ entity? ✅

#### ⚠️ Observação: `senhaHash` recebido mas não exposto na resposta

`ProfessorRequestDTO` recebe `senhaHash` (campo obrigatório) e o repassa à entidade. `ProfessorResponseDTO` não inclui `senhaHash` — correto por segurança. Porém, o campo `senhaHash` no DTO recebe a senha em texto puro (plaintext). Não há nenhum hash sendo aplicado antes de persistir:

```java
// ProfessorRequestDTO.toEntity()
professor.setSenhaHash(this.senhaHash);  // persiste o valor tal como recebido
```

O campo se chama `senhaHash` na entidade, mas está sendo armazenado sem hashing. Isso não é um problema de DTO vs. entity, mas é um risco de segurança que deve ser tratado no service.

---

### 4. ResponsavelController

**Arquivo:** `src/main/java/com/brunoribeiro/controller/ResponsavelController.java`

#### Request bodies usando RequestDTO? ✅
- `POST /responsaveis` → `@RequestBody @Valid ResponsavelRequestDTO responsavelRequestDTO`
- `PUT /responsaveis/{id}` → `@RequestBody @Valid ResponsavelRequestDTO responsavelRequestDTO`

#### Responses usando ResponseDTO? ✅
- `GET /responsaveis` → `List<ResponsavelResponseDTO>`
- `GET /responsaveis/{id}` → `ResponsavelResponseDTO`
- `POST /responsaveis` → `ResponsavelResponseDTO`
- `PUT /responsaveis/{id}` → `ResponsavelResponseDTO`
- `DELETE /responsaveis/{id}` → `ResponseEntity<Void>` ✅

#### `@Valid` presente? ✅

#### Mapeamento DTO ↔ entity? ✅
Todos os campos de `ResponsavelRequestDTO` estão cobertos em `ResponsavelService.updateResponsavel()`.

#### Problemas residuais: nenhum. ✅

---

## Tabela Resumo

| Controller       | RequestDTO | ResponseDTO | @Valid | Mapeamento | Problemas |
|------------------|-----------|------------|--------|------------|-----------|
| AlunoController  | ✅        | ✅          | ✅     | ✅         | ⚠️ PUT zera campos de agenda |
| AulaController   | ✅        | ✅          | ✅     | ✅         | ✅ Nenhum |
| ProfessorController | ✅     | ✅          | ✅     | ✅         | ⚠️ senha sem hash no service |
| ResponsavelController | ✅   | ✅          | ✅     | ✅         | ✅ Nenhum |

---

## Conclusões e Recomendações

### Problema 1 (funcional — deve ser corrigido): PUT /alunos zera campos de agenda

**Causa:** `AlunoRequestDTO` foi simplificado para expor apenas os campos de identificação/contato do aluno, mas `AlunoService.updateAluno()` ainda tenta copiar os 6 campos de agenda (`formaPagamento`, `valorMensal`, `diaSemanaPadrao`, `horarioPadrao`, `duracaoPadraoMin`, `valorPadrao`) a partir da entidade recebida — que vem do DTO e portanto tem esses campos como `null`.

**Recomendações (escolha uma):**

**Opção A — Adicionar campos ao `AlunoRequestDTO`** (recomendada se o endpoint `/alunos` for responsável por gerenciar agenda):
```java
// Adicionar ao AlunoRequestDTO:
FormaPagamento formaPagamento,
BigDecimal valorMensal,
DiaSemana diaSemanaPadrao,
LocalTime horarioPadrao,
Integer duracaoPadraoMin,
BigDecimal valorPadrao
```
E mapear no `toEntity()`.

**Opção B — Remover os campos de agenda do `updateAluno()`** (recomendada se a agenda for gerenciada por outro endpoint):
```java
// Remover de AlunoService.updateAluno():
// aluno.setFormaPagamento(obj.getFormaPagamento());  -- remover
// aluno.setValorMensal(obj.getValorMensal());         -- remover
// ...
```

**Opção C — Criar dois endpoints separados** (mais RESTful): `PUT /alunos/{id}` para dados cadastrais e `PUT /alunos/{id}/agenda` para dados de agenda, com DTOs distintos.

---

### Problema 2 (segurança): senha em plaintext no `ProfessorService`

O `senhaHash` está sendo persistido sem hashing. Recomenda-se aplicar BCrypt no service antes de salvar:

```java
// ProfessorService.create() e update() — adicionar:
professor.setSenhaHash(passwordEncoder.encode(professor.getSenhaHash()));
```

---

### Observação de design (baixa prioridade): Bean Validation duplicada

As entidades JPA (ex: `Aluno.java`) ainda carregam anotações `@NotBlank`, `@NotNull`, `@Size` etc. Com DTOs validados via `@Valid` no controller, a validação nas entidades é redundante. Não causa erro, mas pode gerar mensagens de erro duplicadas em alguns cenários. A boa prática é remover as anotações de validação das entidades e mantê-las apenas nos DTOs.
