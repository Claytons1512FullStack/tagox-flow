package com.tagox.flow.dto.agenda;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public class CriarAgendaRequest {

    @NotNull(message = "O dia da semana é obrigatório.")
    @Positive(message = "O dia da semana deve ser positivo.")
    private Short diaSemana;

    @NotNull(message = "O horário de início é obrigatório.")
    private LocalTime horarioInicio;

    @NotNull(message = "O horário de término é obrigatório.")
    private LocalTime horarioFim;

    public CriarAgendaRequest() {
    }

    public Short getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(Short diaSemana) {
        this.diaSemana = diaSemana;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public LocalTime getHorarioFim() {
        return horarioFim;
    }

    public void setHorarioFim(LocalTime horarioFim) {
        this.horarioFim = horarioFim;
    }
}