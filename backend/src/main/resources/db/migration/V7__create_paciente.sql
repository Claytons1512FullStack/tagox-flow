CREATE TABLE paciente (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(11),
    data_nascimento DATE,
    telefone VARCHAR(20),
    email VARCHAR(150),
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_paciente_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_paciente_cpf
        CHECK (cpf IS NULL OR cpf ~ '^[0-9]{11}$')
);

CREATE UNIQUE INDEX uk_paciente_tenant_cpf
    ON paciente (tenant_id, cpf)
    WHERE cpf IS NOT NULL;

CREATE INDEX idx_paciente_tenant_nome
    ON paciente (tenant_id, nome);
