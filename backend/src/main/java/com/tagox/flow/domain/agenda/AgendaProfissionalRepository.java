package com.tagox.flow.domain.agenda;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgendaProfissionalRepository
        extends JpaRepository<AgendaProfissional, UUID> {

    List<AgendaProfissional>
    findAllByTenant_IdAndProfissional_IdOrderByDiaSemanaAscHorarioInicioAsc(
            UUID tenantId,
            UUID profissionalId
    );

    List<AgendaProfissional>
    findAllByTenant_IdAndProfissional_IdAndAtivoTrueOrderByDiaSemanaAscHorarioInicioAsc(
            UUID tenantId,
            UUID profissionalId
    );

    Optional<AgendaProfissional> findByIdAndTenant_Id(
            UUID id,
            UUID tenantId
    );
}