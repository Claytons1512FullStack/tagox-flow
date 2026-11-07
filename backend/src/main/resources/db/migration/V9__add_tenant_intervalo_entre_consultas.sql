ALTER TABLE tenant
    ADD COLUMN intervalo_entre_consultas_minutos INTEGER NOT NULL DEFAULT 10;

ALTER TABLE tenant
    ADD CONSTRAINT ck_tenant_intervalo_consultas
    CHECK (intervalo_entre_consultas_minutos >= 0);
