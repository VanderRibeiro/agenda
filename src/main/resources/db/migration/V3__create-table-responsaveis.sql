CREATE TABLE responsaveis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    professor_id UUID NOT NULL,
    nome VARCHAR(100) NOT NULL,
    telefone VARCHAR(30) NOT NULL,
    CONSTRAINT fk_responsaveis_professor
        FOREIGN KEY (professor_id) REFERENCES professores(id)
);

ALTER TABLE alunos
    ADD CONSTRAINT fk_alunos_professor
        FOREIGN KEY (professor_id) REFERENCES professores(id),
    ADD CONSTRAINT fk_alunos_responsavel
        FOREIGN KEY (responsavel_id) REFERENCES responsaveis(id);