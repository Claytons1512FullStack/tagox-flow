package com.tagox.flow.service;

import com.tagox.flow.domain.agenda.AgendaProfissional;
import com.tagox.flow.domain.agenda.AgendaProfissionalRepository;
import com.tagox.flow.domain.role.RoleType;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.userrole.UserRoleRepository;
import com.tagox.flow.exception.BusinessException;
import com.tagox.flow.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class AgendaProfissionalService {

    private final AgendaProfissionalRepository agendaRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final TenantRepository tenantRepository;

    public AgendaProfissionalService(
            AgendaProfissionalRepository agendaRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            TenantRepository tenantRepository
    ) {
        this.agendaRepository = agendaRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public AgendaProfissional cadastrarHorario(
            UUID tenantId,
            UUID profissionalId,
            Short diaSemana,
            LocalTime horarioInicio,
            LocalTime horarioFim
    ) {
        validarHorario(diaSemana, horarioInicio, horarioFim);

        Tenant tenant = buscarTenant(tenantId);
        User profissional = buscarProfissional(profissionalId, tenantId);

        AgendaProfissional agenda = new AgendaProfissional(
                tenant,
                profissional,
                diaSemana,
                horarioInicio,
                horarioFim
        );

        return salvarComTratamentoDeConflito(agenda);
    }

    @Transactional(readOnly = true)
    public List<AgendaProfissional> listarHorarios(
            UUID tenantId,
            UUID profissionalId
    ) {
        buscarProfissional(profissionalId, tenantId);

        return agendaRepository
                .findAllByTenant_IdAndProfissional_IdOrderByDiaSemanaAscHorarioInicioAsc(
                        tenantId,
                        profissionalId
                );
    }

    @Transactional(readOnly = true)
    public List<AgendaProfissional> listarHorariosAtivos(
            UUID tenantId,
            UUID profissionalId
    ) {
        buscarProfissional(profissionalId, tenantId);

        return agendaRepository
                .findAllByTenant_IdAndProfissional_IdAndAtivoTrueOrderByDiaSemanaAscHorarioInicioAsc(
                        tenantId,
                        profissionalId
                );
    }

    @Transactional
    public AgendaProfissional atualizarHorario(
            UUID tenantId,
            UUID agendaId,
            Short diaSemana,
            LocalTime horarioInicio,
            LocalTime horarioFim
    ) {
        validarHorario(diaSemana, horarioInicio, horarioFim);

        AgendaProfissional agenda = buscarAgenda(agendaId, tenantId);

        agenda.atualizarHorario(
                diaSemana,
                horarioInicio,
                horarioFim
        );

        return salvarComTratamentoDeConflito(agenda);
    }

    @Transactional
    public AgendaProfissional ativarHorario(
            UUID tenantId,
            UUID agendaId
    ) {
        AgendaProfissional agenda = buscarAgenda(agendaId, tenantId);
        agenda.ativar();

        return salvarComTratamentoDeConflito(agenda);
    }

    @Transactional
    public AgendaProfissional desativarHorario(
            UUID tenantId,
            UUID agendaId
    ) {
        AgendaProfissional agenda = buscarAgenda(agendaId, tenantId);
        agenda.desativar();

        return agendaRepository.save(agenda);
    }

    private Tenant buscarTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Clínica não encontrada."
                        )
                );
    }

    private User buscarProfissional(
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

        List<RoleType> roles =
                userRoleRepository.findRoleTypesByUsuarioId(profissionalId);

        if (!roles.contains(RoleType.PROFISSIONAL)) {
            throw new BusinessException(
                    "O usuário informado não possui o papel de profissional."
            );
        }

        return profissional;
    }

    private AgendaProfissional buscarAgenda(
            UUID agendaId,
            UUID tenantId
    ) {
        return agendaRepository.findByIdAndTenant_Id(agendaId, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Horário de agenda não encontrado nesta clínica."
                        )
                );
    }

    private void validarHorario(
            Short diaSemana,
            LocalTime horarioInicio,
            LocalTime horarioFim
    ) {
        if (diaSemana == null || diaSemana < 1 || diaSemana > 7) {
            throw new BusinessException(
                    "O dia da semana deve estar entre 1 e 7."
            );
        }

        if (horarioInicio == null || horarioFim == null) {
            throw new BusinessException(
                    "Os horários de início e término são obrigatórios."
            );
        }

        if (!horarioInicio.isBefore(horarioFim)) {
            throw new BusinessException(
                    "O horário de início deve ser anterior ao horário de término."
            );
        }
    }

    private AgendaProfissional salvarComTratamentoDeConflito(
            AgendaProfissional agenda
    ) {
        try {
            return agendaRepository.saveAndFlush(agenda);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    "Não foi possível salvar o horário. Verifique se existe sobreposição com outro horário ativo."
            );
        }
    }
}