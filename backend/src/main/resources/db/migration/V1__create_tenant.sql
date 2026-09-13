CREATE TABLE tenant (
    id UUID PRIMARY KEY,
    tipo_pessoa VARCHAR(20) NOT NULL,
    documento VARCHAR(20) NOT NULL,
    nome VARCHAR(150) NOT NULL,
    nome_fantasia VARCHAR(150),
    slug VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP NOT NULL,

    CONSTRAINT uk_tenant_documento UNIQUE (documento),
    CONSTRAINT uk_tenant_slug UNIQUE (slug),

    CONSTRAINT ck_tenant_tipo_pessoa
        CHECK (tipo_pessoa IN ('FISICA', 'JURIDICA')),

    CONSTRAINT ck_tenant_status
        CHECK (status IN ('TRIAL', 'ATIVO', 'SUSPENSO', 'CANCELADO'))
);