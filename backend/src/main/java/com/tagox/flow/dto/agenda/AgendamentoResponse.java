package com.tagox.flow.dto.agenda;

import com.tagox.flow.domain.agenda.Agendamento;

import java.time.Instant;
import java.util.UUID;

public record AgendamentoResponse(
        UUID id,
        UUID profissionalId,
        UUID pacienteId,
        Instant inicio,
        Instant fim,
        Agendamento.Status status,
        String observacoes
) {
    public static AgendamentoResponse from(Agendamento agendamento) {
        return new AgendamentoResponse(
                agendamento.getId(),
                agendamento.getProfissional().getId(),
                agendamento.getPaciente().getId(),
                agendamento.getInicio(),
                agendamento.getFim(),
                agendamento.getStatus(),
                agendamento.getObservacoes()
        );
    }
}
