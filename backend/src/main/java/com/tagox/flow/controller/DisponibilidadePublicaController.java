package com.tagox.flow.controller;

import com.tagox.flow.dto.agenda.HorarioDisponivelResponse;
import com.tagox.flow.service.DisponibilidadePublicaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/publico/clinicas")
public class DisponibilidadePublicaController {

    private final DisponibilidadePublicaService disponibilidadeService;

    public DisponibilidadePublicaController(
            DisponibilidadePublicaService disponibilidadeService
    ) {
        this.disponibilidadeService = disponibilidadeService;
    }

    @GetMapping("/{slug}/profissionais/{profissionalId}/disponibilidade")
    public List<HorarioDisponivelResponse> consultarDisponibilidade(
            @PathVariable String slug,
            @PathVariable UUID profissionalId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data
    ) {
        return disponibilidadeService.consultarDisponibilidade(
                slug,
                profissionalId,
                data
        );
    }
}