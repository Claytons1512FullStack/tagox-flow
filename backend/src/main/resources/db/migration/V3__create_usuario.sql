CREATE TABLE usuario (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    nome VARCHAR(150) NOT NULL,

    email VARCHAR(150) NOT NULL,

    senha_hash VARCHAR(255) NOT NULL,

    status VARCHAR(20) NOT NULL,

    criado_em TIMESTAMPTZ NOT NULL,

    atualizado_em TIMESTAMPTZ NOT NULL,


    CONSTRAINT fk_usuario_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id)
        ON DELETE RESTRICT,


    CONSTRAINT uk_usuario_tenant_email
        UNIQUE (tenant_id, email),


    CONSTRAINT ck_usuario_status
        CHECK (status IN ('ATIVO', 'INATIVO', 'BLOQUEADO'))
);