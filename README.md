## Agenda de Aulas Particulares — API

API REST para professores particulares gerenciarem alunos, aulas agendadas e controle financeiro mensal.

## Objetivo

O projeto tem como objetivo substituir o controle manual realizado através de planilhas ou cadernos, permitindo que professores particulares acompanhem sua agenda de aulas e sua situação financeira mensal.

O sistema permite acompanhar três indicadores principais:

<br>**A receber:** valor referente às aulas realizadas ou previstas que ainda não foram pagas.
<br>**Recebido:** valor já pago pelos alunos.
<br>**Perdido:** valor referente a aulas que não aconteceram e que não geraram receita.

## Funcionalidades

<br>Cadastro e gerenciamento de alunos
<br>Agendamento e gerenciamento de aulas
<br>Controle do status das aulas
<br>Registro de pagamentos
<br>Controle financeiro mensal
<br>Consulta de valores a receber
<br>Consulta de valores recebidos
<br>Consulta de valores perdidos

## Tecnologias
<br>Java
<br>Spring Boot
<br>Spring Web
<br>Spring Data JPA
<br>PostgreSQL
<br>Flyway
<br>Maven
<br>Bean Validation
<br>JUnit
<br>Estrutura

# Arquitetura

O projeto segue uma arquitetura em camadas, separando responsabilidades entre:

<br>Controllers
<br>Services
<br>Repositories
<br>Entities
<br>DTOs
<br>Exceptions
<br>API

```mermaid
---
config:
  theme: forest
  fontFamily: '''Open Sans Variable'', sans-serif'
  layout: dagre
  themeVariables:
    fontFamily: '''Open Sans Variable'', sans-serif'
    lineColor: '#ffffff'
---
flowchart LR
    cliente["Cliente"] -- Requisição HTTP com JWT --> filtro["Security Filter (JWT)<br>Valida o token e extrai o tenant"]
    filtro -- Token válido + tenant extraído --> controller["Controller (REST)<br>Recebe e valida o DTO de entrada"]
    controller --> service["Service<br>Aplica as regras de negócio (RNs)"]
    service --> repository["Repository<br>Spring Data JPA"]
    service -- Erro de regra de negócio --> handler["Exception Handler<br>(@RestControllerAdvice)"]
    repository --> postgres[("PostgreSQL<br>Schema versionado via Flyway")]
    controller -- Resposta de erro HTTP --> handler
    filtro -- Extrai professor_id --> contexto["Contexto multi-tenant<br>professor_id extraído do JWT"]
    contexto -- Disponibiliza tenant --> service
    repository -- Violação de constraint --> handler

    cliente:::client
    filtro:::security
    controller:::api
    service:::business
    repository:::data
    handler:::error
    postgres:::data
    contexto:::business

    classDef client fill:#eef2ff,stroke:#818cf8,color:#1e1b4b
    classDef security fill:#fef2f2,stroke:#f87171,color:#7f1d1d
    classDef api fill:#ecfeff,stroke:#22d3ee,color:#164e63
    classDef error fill:#fdf4ff,stroke:#e879f9,color:#701a75
    classDef business fill:#f0fdf4,stroke:#4ade80,color:#14532d
    classDef data fill:#fff7ed,stroke:#fb923c,color:#7c2d12

    linkStyle default stroke:#ffffff,color:#ffffff
```
A API será disponibilizada através de endpoints REST para gerenciamento de:

<br>Alunos
<br>Aulas
<br>Pagamentos
<br>Controle financeiro

## Status

Setup do projeto 
