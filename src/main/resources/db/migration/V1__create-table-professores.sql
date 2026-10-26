CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE professores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    intervalo_minimo INTEGER NOT NULL
);