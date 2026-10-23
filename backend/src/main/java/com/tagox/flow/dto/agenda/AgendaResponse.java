package com.tagox.flow.dto.agenda;

import com.tagox.flow.domain.agenda.AgendaProfissional;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public class AgendaResponse {

    private UUID id;
    private UUID tenantId;
    private UUID profissionalId;
    private Short diaSemana;
    private LocalTime horarioInicio;
    private LocalTime horarioFim;
    private boolean ativo;
    private Instant createdAt;
    private Instant updatedAt;

    public AgendaResponse(
            UUID id,
            UUID tenantId,
            UUID profissionalId,
            Short diaSemana,
            LocalTime horarioInicio,
            LocalTime horarioFim,
            boolean ativo,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.profissionalId = profissionalId;
        this.diaSemana = diaSemana;
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
        this.ativo = ativo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AgendaResponse from(AgendaProfissional agenda) {
        return new AgendaResponse(
                agenda.getId(),
                agenda.getTenant().getId(),
                agenda.getProfissional().getId(),
                agenda.getDiaSemana(),
                agenda.getHorarioInicio(),
                agenda.getHorarioFim(),
                agenda.isAtivo(),
                agenda.getCreatedAt(),
                agenda.getUpdatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getProfissionalId() {
        return profissionalId;
    }

    public Short getDiaSemana() {
        return diaSemana;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public LocalTime getHorarioFim() {
        return horarioFim;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}