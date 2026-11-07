package com.tagox.flow.controller;

import com.tagox.flow.domain.agenda.Agendamento;
import com.tagox.flow.dto.agenda.AgendamentoResponse;
import com.tagox.flow.security.AuthenticatedUser;
import com.tagox.flow.service.AgendamentoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private static final ZoneId FUSO_CLINICA =
            ZoneId.of("America/Sao_Paulo");

    private final AgendamentoService agendamentoService;

    public AgendamentoController(
            AgendamentoService agendamentoService
    ) {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping("/profissionais/{profissionalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFISSIONAL')")
    public List<AgendamentoResponse> listarPorProfissional(
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado,
            Authentication authentication,
            @PathVariable UUID profissionalId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data
    ) {
        boolean administrador = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority())
                );

        if (!administrador
                && !usuarioAutenticado.getUserId().equals(profissionalId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você só pode consultar sua própria agenda."
            );
        }

        var inicio = data.atStartOfDay(FUSO_CLINICA).toInstant();
        var fim = data.plusDays(1).atStartOfDay(FUSO_CLINICA).toInstant();

        List<Agendamento> agendamentos =
                agendamentoService.listarPorProfissional(
                        usuarioAutenticado.getTenantId(),
                        profissionalId,
                        inicio,
                        fim
                );

        return agendamentos.stream()
                .map(AgendamentoResponse::from)
                .toList();
    }
}