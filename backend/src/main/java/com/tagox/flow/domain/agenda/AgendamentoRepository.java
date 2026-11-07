package com.tagox.flow.domain.agenda;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgendamentoRepository
        extends JpaRepository<Agendamento, UUID> {

    List<Agendamento>
    findAllByTenant_IdAndProfissional_IdAndInicioGreaterThanEqualAndInicioLessThanOrderByInicioAsc(
            UUID tenantId,
            UUID profissionalId,
            Instant inicio,
            Instant fim
    );

    List<Agendamento>
    findAllByTenant_IdAndPaciente_IdOrderByInicioDesc(
            UUID tenantId,
            UUID pacienteId
    );

    Optional<Agendamento> findByIdAndTenant_Id(
            UUID agendamentoId,
            UUID tenantId
    );

    boolean existsByTenant_IdAndProfissional_IdAndInicioLessThanAndFimGreaterThanAndStatusIn(
            UUID tenantId,
            UUID profissionalId,
            Instant fim,
            Instant inicio,
            List<Agendamento.Status> status
    );

    List<Agendamento>
    findAllByTenant_IdAndProfissional_IdAndInicioLessThanAndFimGreaterThanAndStatusInOrderByInicioAsc(
            UUID tenantId,
            UUID profissionalId,
            Instant fim,
            Instant inicio,
            List<Agendamento.Status> status
    );
}