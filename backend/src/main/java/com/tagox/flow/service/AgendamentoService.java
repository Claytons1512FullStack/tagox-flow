package com.tagox.flow.service;

import com.tagox.flow.domain.agenda.AgendaProfissional;
import com.tagox.flow.domain.agenda.AgendaProfissionalRepository;
import com.tagox.flow.domain.agenda.Agendamento;
import com.tagox.flow.domain.agenda.AgendamentoRepository;
import com.tagox.flow.domain.patient.Patient;
import com.tagox.flow.domain.patient.PatientRepository;
import com.tagox.flow.domain.role.RoleType;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.user.UserStatus;
import com.tagox.flow.domain.userrole.UserRoleRepository;
import com.tagox.flow.exception.BusinessException;
import com.tagox.flow.exception.ConflitoAgendamentoException;
import com.tagox.flow.exception.ResourceNotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AgendamentoService {

    private static final long DURACAO_CONSULTA_MINUTOS = 50;

    private static final String SQLSTATE_EXCLUSAO_VIOLADA = "23P01";

    private static final ZoneId FUSO_CLINICA =
            ZoneId.of("America/Sao_Paulo");

    private static final List<Agendamento.Status> STATUS_BLOQUEANTES =
            List.of(
                    Agendamento.Status.SOLICITADO,
                    Agendamento.Status.CONFIRMADO
            );

    private final AgendamentoRepository agendamentoRepository;
    private final AgendaProfissionalRepository agendaRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PatientRepository patientRepository;
    private final TenantRepository tenantRepository;

    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            AgendaProfissionalRepository agendaRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            PatientRepository patientRepository,
            TenantRepository tenantRepository
    ) {
        this.agendamentoRepository = agendamentoRepository;
        this.agendaRepository = agendaRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.patientRepository = patientRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public Agendamento criarAgendamento(
            UUID tenantId,
            UUID profissionalId,
            UUID pacienteId,
            Instant inicio,
            String observacoes
    ) {
        validarInicio(inicio);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Clínica não encontrada."
                        )
                );

        /*
         * Bloqueia o registro do profissional durante a transação.
         * As demais criações que utilizarem o mesmo mecanismo
         * precisarão aguardar a liberação desse bloqueio.
         */
        User profissional = buscarProfissionalAtivoParaAgendamento(
                profissionalId,
                tenantId
        );

        Patient paciente = patientRepository
                .findByIdAndTenantId(pacienteId, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Paciente não encontrado nesta clínica."
                        )
                );

        Instant fim = inicio.plusSeconds(
                DURACAO_CONSULTA_MINUTOS * 60
        );

        int intervaloEntreConsultas =
                tenant.getIntervaloEntreConsultasMinutos();

        validarDisponibilidade(
                tenantId,
                profissionalId,
                inicio,
                fim
        );

        validarAusenciaDeConflito(
                tenantId,
                profissionalId,
                inicio,
                fim,
                intervaloEntreConsultas
        );

        Agendamento agendamento = new Agendamento(
                tenant,
                profissional,
                paciente,
                inicio,
                fim,
                normalizarObservacoes(observacoes)
        );

        try {
            return agendamentoRepository.saveAndFlush(agendamento);
        } catch (DataIntegrityViolationException exception) {
            if (ehConflitoDeHorario(exception)) {
                throw new ConflitoAgendamentoException(
                        "Este horário acabou de ser reservado. "
                                + "Consulte a disponibilidade e escolha "
                                + "outro horário."
                );
            }

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<Agendamento> listarPorProfissional(
            UUID tenantId,
            UUID profissionalId,
            Instant inicio,
            Instant fim
    ) {
        if (inicio == null || fim == null || !inicio.isBefore(fim)) {
            throw new BusinessException(
                    "O período informado para consulta é inválido."
            );
        }

        buscarProfissionalAtivo(profissionalId, tenantId);

        return agendamentoRepository
                .findAllByTenant_IdAndProfissional_IdAndInicioGreaterThanEqualAndInicioLessThanOrderByInicioAsc(
                        tenantId,
                        profissionalId,
                        inicio,
                        fim
                );
    }

    @Transactional(readOnly = true)
    public List<Agendamento> listarPorPaciente(
            UUID tenantId,
            UUID pacienteId
    ) {
        patientRepository.findByIdAndTenantId(pacienteId, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Paciente não encontrado nesta clínica."
                        )
                );

        return agendamentoRepository
                .findAllByTenant_IdAndPaciente_IdOrderByInicioDesc(
                        tenantId,
                        pacienteId
                );
    }

    @Transactional
    public Agendamento confirmar(
            UUID tenantId,
            UUID agendamentoId
    ) {
        Agendamento agendamento = buscarAgendamento(
                agendamentoId,
                tenantId
        );

        agendamento.confirmar();

        return agendamentoRepository.save(agendamento);
    }

    @Transactional
    public Agendamento cancelar(
            UUID tenantId,
            UUID agendamentoId
    ) {
        Agendamento agendamento = buscarAgendamento(
                agendamentoId,
                tenantId
        );

        agendamento.cancelar();

        return agendamentoRepository.save(agendamento);
    }

    @Transactional
    public Agendamento concluir(
            UUID tenantId,
            UUID agendamentoId
    ) {
        Agendamento agendamento = buscarAgendamento(
                agendamentoId,
                tenantId
        );

        agendamento.concluir();

        return agendamentoRepository.save(agendamento);
    }

    private User buscarProfissionalAtivoParaAgendamento(
            UUID profissionalId,
            UUID tenantId
    ) {
        User profissional = userRepository
                .findByIdAndTenantIdForUpdate(profissionalId, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Profissional não encontrado nesta clínica."
                        )
                );

        validarProfissionalAtivo(profissional, profissionalId);

        return profissional;
    }

    private User buscarProfissionalAtivo(
            UUID profissionalId,
            UUID tenantId
    ) {
        User profissional = userRepository
                .findByIdAndTenantId(profissionalId, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Profissional não encontrado nesta clínica."
                        )
                );

        validarProfissionalAtivo(profissional, profissionalId);

        return profissional;
    }

    private void validarProfissionalAtivo(
            User profissional,
            UUID profissionalId
    ) {
        if (profissional.getStatus() != UserStatus.ATIVO) {
            throw new BusinessException(
                    "O profissional não está ativo."
            );
        }

        List<RoleType> roles =
                userRoleRepository.findRoleTypesByUsuarioId(profissionalId);

        if (!roles.contains(RoleType.PROFISSIONAL)) {
            throw new BusinessException(
                    "O usuário informado não possui o papel de profissional."
            );
        }
    }

    private void validarInicio(Instant inicio) {
        if (inicio == null) {
            throw new BusinessException(
                    "O horário de início é obrigatório."
            );
        }

        if (!inicio.isAfter(Instant.now())) {
            throw new BusinessException(
                    "O agendamento deve começar em um horário futuro."
            );
        }
    }

    private void validarDisponibilidade(
            UUID tenantId,
            UUID profissionalId,
            Instant inicio,
            Instant fim
    ) {
        ZonedDateTime inicioLocal = inicio.atZone(FUSO_CLINICA);
        ZonedDateTime fimLocal = fim.atZone(FUSO_CLINICA);

        DayOfWeek dia = inicioLocal.getDayOfWeek();
        LocalTime horaInicio = inicioLocal.toLocalTime();
        LocalTime horaFim = fimLocal.toLocalTime();

        if (!inicioLocal.toLocalDate().equals(fimLocal.toLocalDate())) {
            throw new BusinessException(
                    "A consulta deve terminar no mesmo dia em que começa."
            );
        }

        List<AgendaProfissional> horarios =
                agendaRepository
                        .findAllByTenant_IdAndProfissional_IdAndAtivoTrueOrderByDiaSemanaAscHorarioInicioAsc(
                                tenantId,
                                profissionalId
                        );

        boolean disponivel = horarios.stream().anyMatch(horario ->
                horario.getDiaSemana() == dia.getValue()
                        && !horaInicio.isBefore(horario.getHorarioInicio())
                        && !horaFim.isAfter(horario.getHorarioFim())
        );

        if (!disponivel) {
            throw new BusinessException(
                    "O horário solicitado está fora da disponibilidade "
                            + "cadastrada para o profissional."
            );
        }
    }

    private void validarAusenciaDeConflito(
            UUID tenantId,
            UUID profissionalId,
            Instant inicio,
            Instant fim,
            int intervaloEntreConsultasMinutos
    ) {
        if (intervaloEntreConsultasMinutos < 0) {
            throw new BusinessException(
                    "O intervalo entre consultas da clínica é inválido."
            );
        }

        long intervaloEmSegundos =
                intervaloEntreConsultasMinutos * 60L;

        /*
         * Expandimos a janela de pesquisa nos dois sentidos.
         *
         * Isso identifica:
         * - consultas que terminam muito próximas do novo início;
         * - consultas que começam muito próximas do novo fim;
         * - sobreposições propriamente ditas.
         *
         * Os limites são exclusivos: quando a distância é exatamente
         * igual ao intervalo configurado, o horário é permitido.
         */
        Instant limiteInicio = inicio.minusSeconds(intervaloEmSegundos);
        Instant limiteFim = fim.plusSeconds(intervaloEmSegundos);

        boolean existeConflito =
                agendamentoRepository
                        .existsByTenant_IdAndProfissional_IdAndInicioLessThanAndFimGreaterThanAndStatusIn(
                                tenantId,
                                profissionalId,
                                limiteFim,
                                limiteInicio,
                                STATUS_BLOQUEANTES
                        );

        if (existeConflito) {
            throw new ConflitoAgendamentoException(
                    "Já existe um agendamento nesse intervalo. "
                            + "Consulte a disponibilidade e escolha outro horário."
            );
        }
    }

    private boolean ehConflitoDeHorario(Throwable exception) {
        Throwable causa = exception;

        while (causa != null) {
            if (causa instanceof SQLException sqlException
                    && SQLSTATE_EXCLUSAO_VIOLADA.equals(
                    sqlException.getSQLState()
            )) {
                return true;
            }

            causa = causa.getCause();
        }

        return false;
    }

    private Agendamento buscarAgendamento(
            UUID agendamentoId,
            UUID tenantId
    ) {
        return agendamentoRepository
                .findByIdAndTenant_Id(agendamentoId, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Agendamento não encontrado nesta clínica."
                        )
                );
    }

    private String normalizarObservacoes(String observacoes) {
        if (observacoes == null || observacoes.isBlank()) {
            return null;
        }

        String valor = observacoes.trim();

        if (valor.length() > 1000) {
            throw new BusinessException(
                    "As observações não podem ultrapassar 1000 caracteres."
            );
        }

        return valor;
    }
}