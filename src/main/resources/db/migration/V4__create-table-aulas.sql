CREATE TABLE aulas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    professor_id UUID NOT NULL,
    aluno_id UUID NOT NULL,
    aula_original_id UUID,
    data DATE NOT NULL,
    horario_inicio TIME NOT NULL,
    horario_fim TIME NOT NULL,
    duracao_minutos INTEGER NOT NULL,
    valor NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    pago BOOLEAN NOT NULL,
    date_pagamento DATE,
    CONSTRAINT fk_aulas_professor
        FOREIGN KEY (professor_id) REFERENCES professores(id),
    CONSTRAINT fk_aulas_aluno
        FOREIGN KEY (aluno_id) REFERENCES alunos(id),
    CONSTRAINT fk_aulas_original
        FOREIGN KEY (aula_original_id) REFERENCES aulas(id)
);