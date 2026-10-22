-- ============================================================
-- TAGOX FLOW
-- V8 - Agenda profissional e agendamentos
-- PostgreSQL + Flyway
-- ============================================================

-- Suporte às restrições de exclusão utilizadas para impedir
-- sobreposição de horários.
CREATE EXTENSION IF NOT EXISTS btree_gist;


-- ============================================================
-- 1. REFORÇO DA INTEGRIDADE MULTI-TENANT
-- ============================================================

-- Permite que as chaves estrangeiras compostas garantam
-- que o usuário referenciado pertença à clínica indicada.

ALTER TABLE usuario
    ADD CONSTRAINT uq_usuario_tenant_id
        UNIQUE (tenant_id, id);

ALTER TABLE paciente
    ADD CONSTRAINT uq_paciente_tenant_id
        UNIQUE (tenant_id, id);


-- ============================================================
-- 2. HORÁRIOS DE ATENDIMENTO DOS PROFISSIONAIS
-- ============================================================

CREATE TABLE agenda_profissional (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                     tenant_id UUID NOT NULL,
                                     profissional_id UUID NOT NULL,

    -- Segunda-feira = 1; domingo = 7.
                                     dia_semana SMALLINT NOT NULL,

                                     horario_inicio TIME NOT NULL,
                                     horario_fim TIME NOT NULL,

                                     ativo BOOLEAN NOT NULL DEFAULT TRUE,

                                     created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT fk_agenda_profissional_tenant
                                         FOREIGN KEY (tenant_id)
                                             REFERENCES tenant (id),

    -- Garante que o profissional pertença à mesma clínica.
                                     CONSTRAINT fk_agenda_profissional_usuario_tenant
                                         FOREIGN KEY (tenant_id, profissional_id)
                                             REFERENCES usuario (tenant_id, id),

                                     CONSTRAINT ck_agenda_profissional_dia_semana
                                         CHECK (dia_semana BETWEEN 1 AND 7),

                                     CONSTRAINT ck_agenda_profissional_intervalo
                                         CHECK (horario_inicio < horario_fim),

    -- Impede períodos de atendimento sobrepostos para
    -- o mesmo profissional no mesmo dia da semana.
                                     CONSTRAINT ex_agenda_profissional_intervalo
                                         EXCLUDE USING gist (
                                         tenant_id WITH =,
                                         profissional_id WITH =,
                                         dia_semana WITH =,
                                         tsrange(
                                                 TIMESTAMP '2000-01-01' + horario_inicio,
                                                 TIMESTAMP '2000-01-01' + horario_fim,
                                                 '[)'
                                         ) WITH &&
                                         )
                                         WHERE (ativo = TRUE)
);


CREATE INDEX idx_agenda_profissional_tenant_profissional
    ON agenda_profissional (tenant_id, profissional_id);

CREATE INDEX idx_agenda_profissional_dia_ativo
    ON agenda_profissional (
                            tenant_id,
                            profissional_id,
                            dia_semana
        )
    WHERE ativo = TRUE;


-- ============================================================
-- 3. AGENDAMENTOS
-- ============================================================

CREATE TABLE agendamento (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                             tenant_id UUID NOT NULL,
                             profissional_id UUID NOT NULL,
                             paciente_id UUID NOT NULL,

                             inicio TIMESTAMPTZ NOT NULL,
                             fim TIMESTAMPTZ NOT NULL,

                             status VARCHAR(20) NOT NULL DEFAULT 'SOLICITADO',

                             observacoes VARCHAR(1000),

                             created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_agendamento_tenant
                                 FOREIGN KEY (tenant_id)
                                     REFERENCES tenant (id),

    -- O profissional deve pertencer à clínica do agendamento.
                             CONSTRAINT fk_agendamento_profissional_tenant
                                 FOREIGN KEY (tenant_id, profissional_id)
                                     REFERENCES usuario (tenant_id, id),

    -- O paciente deve pertencer à clínica do agendamento.
                             CONSTRAINT fk_agendamento_paciente_tenant
                                 FOREIGN KEY (tenant_id, paciente_id)
                                     REFERENCES paciente (tenant_id, id),

                             CONSTRAINT ck_agendamento_intervalo
                                 CHECK (inicio < fim),

                             CONSTRAINT ck_agendamento_status
                                 CHECK (
                                     status IN (
                                                'SOLICITADO',
                                                'CONFIRMADO',
                                                'CANCELADO',
                                                'CONCLUIDO'
                                         )
                                     ),

    -- Impede que o mesmo profissional tenha agendamentos
    -- ativos com períodos sobrepostos.
    -- O intervalo [inicio, fim) permite consultas consecutivas.
                             CONSTRAINT ex_agendamento_profissional_intervalo
                                 EXCLUDE USING gist (
                                 tenant_id WITH =,
                                 profissional_id WITH =,
                                 tstzrange(inicio, fim, '[)') WITH &&
                                 )
                                 WHERE (
                                     status IN ('SOLICITADO', 'CONFIRMADO')
                                     )
);


CREATE INDEX idx_agendamento_tenant_profissional_inicio
    ON agendamento (tenant_id, profissional_id, inicio);

CREATE INDEX idx_agendamento_tenant_paciente_inicio
    ON agendamento (tenant_id, paciente_id, inicio);

CREATE INDEX idx_agendamento_tenant_status
    ON agendamento (tenant_id, status);