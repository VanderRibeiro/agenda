CREATE TABLE alunos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    professor_id UUID NOT NULL,
    responsavel_id UUID,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150),
    telefone VARCHAR(30) NOT NULL,
    endereco VARCHAR(255),
    forma_pagamento VARCHAR(20),
    valor_mensal NUMERIC(19, 2),
    dia_semana_padrao VARCHAR(20),
    horario_padrao TIME,
    duracao_padrao_min INTEGER,
    valor_padrao NUMERIC(19, 2)
);