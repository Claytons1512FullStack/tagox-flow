        package com.tagox.flow.service;

import com.tagox.flow.domain.agenda.Agendamento;
import com.tagox.flow.domain.patient.Patient;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.dto.agenda.CriarAgendamentoPublicoRequest;
import com.tagox.flow.dto.agenda.HorarioDisponivelResponse;
import com.tagox.flow.dto.patient.CreatePatientRequest;
import com.tagox.flow.exception.BusinessException;
import com.tagox.flow.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class AgendamentoPublicoService {

    private static final ZoneId FUSO_CLINICA =
            ZoneId.of("America/Sao_Paulo");

    private final TenantRepository tenantRepository;
    private final PatientService patientService;
    private final AgendamentoService agendamentoService;
    private final DisponibilidadePublicaService disponibilidadeService;

    public AgendamentoPublicoService(
            TenantRepository tenantRepository,
            PatientService patientService,
            AgendamentoService agendamentoService,
            DisponibilidadePublicaService disponibilidadeService
    ) {
        this.tenantRepository = tenantRepository;
        this.patientService = patientService;
        this.agendamentoService = agendamentoService;
        this.disponibilidadeService = disponibilidadeService;
    }

    @Transactional
    public Agendamento solicitarAgendamento(
            String slug,
            UUID profissionalId,
            CriarAgendamentoPublicoRequest request
    ) {
        if (request == null
                || request.getData() == null
                || request.getHorarioInicio() == null) {
            throw new BusinessException(
                    "A data e o horário da consulta são obrigatórios."
            );
        }

        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Clínica não encontrada."
                        )
                );

        Instant inicio = request.getData()
                .atTime(request.getHorarioInicio())
                .atZone(FUSO_CLINICA)
                .toInstant();

        boolean horarioDisponivel = disponibilidadeService
                .consultarDisponibilidade(
                        slug,
                        profissionalId,
                        request.getData()
                )
                .stream()
                .anyMatch(horario ->
                                horario.inicio().equals(request.getHorarioInicio())
                        );

        if (!horarioDisponivel) {
            throw new BusinessException(
                    "O horário escolhido não está mais disponível. "
                            + "Consulte os horários novamente."
            );
        }

        CreatePatientRequest dadosPaciente = new CreatePatientRequest();
        dadosPaciente.setNome(request.getNome());
        dadosPaciente.setCpf(request.getCpf());
        dadosPaciente.setDataNascimento(request.getDataNascimento());
        dadosPaciente.setTelefone(request.getTelefone());
        dadosPaciente.setEmail(request.getEmail());

        Patient paciente = patientService.criarPaciente(
                dadosPaciente,
                tenant.getId()
        );

        return agendamentoService.criarAgendamento(
                tenant.getId(),
                profissionalId,
                paciente.getId(),
                inicio,
                request.getObservacoes()
        );
    }
}
