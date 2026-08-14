# Regras de Negócio

## RN-001 - Professor e Permissões

- Todo usuário do sistema representa um professor.
- Cada professor pode cadastrar e gerenciar seus próprios alunos e aulas.
- Um professor não pode visualizar, editar ou excluir informações pertencentes a outro professor.

## RN-002 - Cadastro de Alunos

O professor pode cadastrar um aluno com:

- Nome;
- Telefone;
- Endereço;
- Forma de pagamento;
- Matéria da aula;
- Responsável (opcional).

### Responsável

- O responsável é opcional.
- Caso informado, nome e telefone são obrigatórios.

### Configuração de Aula Recorrente

O aluno pode possuir uma configuração padrão contendo:

- Dia da semana;
- Horário;
- Duração;
- Valor.

## RN-003 - Aulas

Toda aula deve possuir:

- Professor;
- Aluno;
- Data;
- Horário;
- Duração;
- Valor;
- Status.

### Regras

- O horário final é calculado automaticamente.
- A configuração recorrente serve como base para geração das aulas futuras.
- Uma aula específica pode ser remarcada.

## RN-004 - Status das Aulas

Status permitidos:

- AGENDADA
- REALIZADA
- CANCELADA
- ADIADA

### Regras

- Toda nova aula inicia como AGENDADA.
- Apenas aulas REALIZADAS são consideradas concluídas.
- Apenas aulas REALIZADAS geram receita.
- Aulas CANCELADAS não geram receita.
- Aulas ADIADAS não geram receita até serem realizadas.
- Uma aula ADIADA passa a gerar receita quando for marcada como REALIZADA.

## RN-005 - Conflito de Horários

- O professor não pode possuir aulas conflitantes.
- O sistema deve considerar a duração da aula e o tempo mínimo de intervalo configurado.
- O intervalo mínimo é configurado em Configurações > Horários.

### Validação

Quando houver conflito:

"O horário solicitado está próximo demais de outro horário já existente. Escolha outro horário ou acesse Configurações > Horários > Tempo de intervalo."

## RN-006 - Agenda

### Visão Mensal

- A agenda principal deve utilizar visualização mensal.
- Dias com aulas devem ser destacados.

### Visão Diária

- Ao selecionar um dia, todas as aulas daquele dia devem ser exibidas.
- As aulas devem ser ordenadas cronologicamente em pilha.
- As informações devem ser exibidas em formato de card.

## RN-007 - Ações da Aula

Cada aula pode disponibilizar:

### WhatsApp

Abre conversa com o telefone do aluno ou responsável.

### Uber/POP

Utiliza o endereço do aluno como destino da viagem.

### Remarcar

Permite alterar data e horário respeitando as regras de conflito.

### Alterar Status

Permite marcar a aula como:

- REALIZADA;
- CANCELADA;
- ADIADA.

## RN-008 - Remarcação

- Toda remarcação deve validar conflitos e intervalo mínimo.
- A aula original deve permanecer registrada como ADIADA.
- A nova data deve gerar ou atualizar o compromisso correspondente.
- Quando realizada, a aula remarcada passa a ser contabilizada normalmente.

## RN-009 - Regras Financeiras

### Pagamento Diário

- O aluno paga por aula.
- Apenas aulas REALIZADAS são contabilizadas.
- Aulas CANCELADAS e ADIADAS não geram receita.

### Pagamento Mensal

- O aluno possui um valor mensal configurado.
- Apenas aulas REALIZADAS são consideradas para cálculo efetivo de receita.
- Aulas CANCELADAS e ADIADAS não entram no cálculo enquanto não forem realizadas.

## RN-010 - Indicadores

O sistema deve apresentar:

- Valor ganho;
- Valor a receber;
- Quantidade de aulas realizadas;
- Quantidade de aulas canceladas;
- Quantidade de aulas adiadas.

### Regras

- O valor ganho considera apenas aulas realizadas.
- O usuário pode ocultar valores financeiros.
- Todos os indicadores respeitam o período selecionado.

## RN-012 - Configurações

O professor pode configurar:

- Tempo mínimo de intervalo entre aulas;
- Regras relacionadas ao funcionamento da agenda.
