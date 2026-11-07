package com.tagox.flow.controller;

import com.tagox.flow.domain.agenda.Agendamento;
import com.tagox.flow.dto.agenda.CriarAgendamentoPublicoRequest;
import com.tagox.flow.service.AgendamentoPublicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/publico/clinicas")
public class AgendamentoPublicoController {

    private final AgendamentoPublicoService agendamentoPublicoService;

    public AgendamentoPublicoController(
            AgendamentoPublicoService agendamentoPublicoService
    ) {
        this.agendamentoPublicoService = agendamentoPublicoService;
    }

    @PostMapping("/{slug}/profissionais/{profissionalId}/agendamentos")
    public ResponseEntity<Map<String, Object>> solicitarAgendamento(
            @PathVariable String slug,
            @PathVariable UUID profissionalId,
            @Valid @RequestBody CriarAgendamentoPublicoRequest request
    ) {
        Agendamento agendamento =
                agendamentoPublicoService.solicitarAgendamento(
                        slug,
                        profissionalId,
                        request
                );

        Map<String, Object> resposta = Map.of(
                "mensagem", "Solicitação de agendamento recebida com sucesso.",
                "agendamentoId", agendamento.getId(),
                "status", agendamento.getStatus(),
                "inicio", agendamento.getInicio(),
                "fim", agendamento.getFim()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }
}