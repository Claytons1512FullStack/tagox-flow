package com.tagox.flow.service;

import com.tagox.flow.domain.agenda.AgendaProfissional;
import com.tagox.flow.domain.agenda.AgendaProfissionalRepository;
import com.tagox.flow.domain.agenda.Agendamento;
import com.tagox.flow.domain.agenda.AgendamentoRepository;
import com.tagox.flow.domain.role.RoleType;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.user.UserStatus;
import com.tagox.flow.domain.userrole.UserRoleRepository;
import com.tagox.flow.dto.agenda.HorarioDisponivelResponse;
import com.tagox.flow.exception.BusinessException;
import com.tagox.flow.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DisponibilidadePublicaService {

    private static final long DURACAO_CONSULTA_MINUTOS = 50;

    private static final ZoneId FUSO_CLINICA =
            ZoneId.of("America/Sao_Paulo");

    private static final List<Agendamento.Status> STATUS_BLOQUEANTES =
            List.of(
                    Agendamento.Status.SOLICITADO,
                    Agendamento.Status.CONFIRMADO
            );

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AgendaProfissionalRepository agendaRepository;
    private final AgendamentoRepository agendamentoRepository;

    public DisponibilidadePublicaService(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            AgendaProfissionalRepository agendaRepository,
            AgendamentoRepository agendamentoRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.agendaRepository = agendaRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    @Transactional(readOnly = true)
    public List<HorarioDisponivelResponse> consultarDisponibilidade(
            String slug,
            UUID profissionalId,
            LocalDate data
    ) {
        if (slug == null || slug.isBlank()) {
            throw new BusinessException(
                    "O identificador público da clínica é obrigatório."
            );
        }

        if (profissionalId == null) {
            throw new BusinessException(
                    "O profissional é obrigatório."
            );
        }

        if (data == null) {
            throw new BusinessException(
                    "A data para consulta é obrigatória."
            );
        }

        Tenant tenant = tenantRepository.findBySlug(slug.trim())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Clínica não encontrada."
                        )
                );

        if (tenant.getStatus() != TenantStatus.TRIAL
                && tenant.getStatus() != TenantStatus.ATIVO) {
            throw new BusinessException(
                    "A agenda desta clínica não está disponível."
            );
        }

        User profissional = userRepository
                .findByIdAndTenantId(profissionalId, tenant.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Profissional não encontrado nesta clínica."
                        )
                );

        if (profissional.getStatus() != UserStatus.ATIVO) {
            throw new BusinessException(
                    "A agenda deste profissional não está disponível."
            );
        }

        List<RoleType> papeis =
                userRoleRepository.findRoleTypesByUsuarioId(profissionalId);

        if (!papeis.contains(RoleType.PROFISSIONAL)) {
            throw new ResourceNotFoundException(
                    "Profissional não encontrado nesta clínica."
            );
        }

        int intervaloMinutos =
                tenant.getIntervaloEntreConsultasMinutos();

        if (intervaloMinutos < 0) {
            throw new BusinessException(
                    "O intervalo entre consultas não pode ser negativo."
            );
        }

        long passoEntreConsultasMinutos =
                DURACAO_CONSULTA_MINUTOS + intervaloMinutos;

        long intervaloEmSegundos =
                intervaloMinutos * 60L;

        Instant agora = Instant.now();

        Instant inicioDoDia = data.atStartOfDay(FUSO_CLINICA).toInstant();

        Instant inicioDoProximoDia = data.plusDays(1)
                .atStartOfDay(FUSO_CLINICA)
                .toInstant();

        if (!inicioDoProximoDia.isAfter(agora)) {
            return List.of();
        }

        DayOfWeek diaSemana = data.getDayOfWeek();

        List<AgendaProfissional> horarios =
                agendaRepository
                        .findAllByTenant_IdAndProfissional_IdAndAtivoTrueOrderByDiaSemanaAscHorarioInicioAsc(
                                tenant.getId(),
                                profissionalId
                        );

        List<Agendamento> agendamentos =
                agendamentoRepository
                        .findAllByTenant_IdAndProfissional_IdAndInicioLessThanAndFimGreaterThanAndStatusInOrderByInicioAsc(
                                tenant.getId(),
                                profissionalId,
                                inicioDoProximoDia,
                                inicioDoDia,
                                STATUS_BLOQUEANTES
                        );

        List<HorarioDisponivelResponse> disponiveis =
                new ArrayList<>();

        for (AgendaProfissional horario : horarios) {

            if (horario.getDiaSemana() != diaSemana.getValue()) {
                continue;
            }

            LocalTime inicioSlot = horario.getHorarioInicio();

            while (true) {

                LocalTime fimSlot = inicioSlot.plusMinutes(
                        DURACAO_CONSULTA_MINUTOS
                );

                if (fimSlot.isAfter(horario.getHorarioFim())) {
                    break;
                }

                Instant inicioInstant = data.atTime(inicioSlot)
                        .atZone(FUSO_CLINICA)
                        .toInstant();

                Instant fimInstant = data.atTime(fimSlot)
                        .atZone(FUSO_CLINICA)
                        .toInstant();

                boolean futuro = inicioInstant.isAfter(agora);

                Instant limiteInicio = inicioInstant.minusSeconds(
                        intervaloEmSegundos
                );

                Instant limiteFim = fimInstant.plusSeconds(
                        intervaloEmSegundos
                );

                boolean ocupado = agendamentos.stream().anyMatch(
                        agendamento ->
                                agendamento.getInicio().isBefore(limiteFim)
                                        && agendamento.getFim()
                                        .isAfter(limiteInicio)
                );

                if (futuro && !ocupado) {
                    disponiveis.add(
                            new HorarioDisponivelResponse(
                                    inicioSlot,
                                    fimSlot
                            )
                    );
                }

                inicioSlot = inicioSlot.plusMinutes(
                        passoEntreConsultasMinutos
                );
            }
        }

        return disponiveis;
    }
}