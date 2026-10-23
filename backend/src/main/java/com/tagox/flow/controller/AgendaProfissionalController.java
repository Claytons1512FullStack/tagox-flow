package com.tagox.flow.controller;

import com.tagox.flow.domain.agenda.AgendaProfissional;
import com.tagox.flow.dto.agenda.AgendaResponse;
import com.tagox.flow.dto.agenda.CriarAgendaRequest;
import com.tagox.flow.security.AuthenticatedUser;
import com.tagox.flow.service.AgendaProfissionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agenda-profissional")
public class AgendaProfissionalController {

    private final AgendaProfissionalService agendaService;

    public AgendaProfissionalController(
            AgendaProfissionalService agendaService
    ) {
        this.agendaService = agendaService;
    }

    @PostMapping("/profissionais/{profissionalId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AgendaResponse cadastrarHorario(
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado,
            @PathVariable UUID profissionalId,
            @Valid @RequestBody CriarAgendaRequest request
    ) {
        AgendaProfissional agenda = agendaService.cadastrarHorario(
                usuarioAutenticado.getTenantId(),
                profissionalId,
                request.getDiaSemana(),
                request.getHorarioInicio(),
                request.getHorarioFim()
        );

        return AgendaResponse.from(agenda);
    }

    @GetMapping("/profissionais/{profissionalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFISSIONAL')")
    public List<AgendaResponse> listarHorarios(
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado,
            @PathVariable UUID profissionalId
    ) {
        return agendaService.listarHorarios(
                        usuarioAutenticado.getTenantId(),
                        profissionalId
                )
                .stream()
                .map(AgendaResponse::from)
                .toList();
    }

    @GetMapping("/profissionais/{profissionalId}/ativos")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFISSIONAL')")
    public List<AgendaResponse> listarHorariosAtivos(
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado,
            @PathVariable UUID profissionalId
    ) {
        return agendaService.listarHorariosAtivos(
                        usuarioAutenticado.getTenantId(),
                        profissionalId
                )
                .stream()
                .map(AgendaResponse::from)
                .toList();
    }

    @PutMapping("/{agendaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public AgendaResponse atualizarHorario(
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado,
            @PathVariable UUID agendaId,
            @Valid @RequestBody CriarAgendaRequest request
    ) {
        AgendaProfissional agenda = agendaService.atualizarHorario(
                usuarioAutenticado.getTenantId(),
                agendaId,
                request.getDiaSemana(),
                request.getHorarioInicio(),
                request.getHorarioFim()
        );

        return AgendaResponse.from(agenda);
    }

    @PatchMapping("/{agendaId}/ativar")
    @PreAuthorize("hasRole('ADMIN')")
    public AgendaResponse ativarHorario(
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado,
            @PathVariable UUID agendaId
    ) {
        AgendaProfissional agenda = agendaService.ativarHorario(
                usuarioAutenticado.getTenantId(),
                agendaId
        );

        return AgendaResponse.from(agenda);
    }

    @PatchMapping("/{agendaId}/desativar")
    @PreAuthorize("hasRole('ADMIN')")
    public AgendaResponse desativarHorario(
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado,
            @PathVariable UUID agendaId
    ) {
        AgendaProfissional agenda = agendaService.desativarHorario(
                usuarioAutenticado.getTenantId(),
                agendaId
        );

        return AgendaResponse.from(agenda);
    }
}