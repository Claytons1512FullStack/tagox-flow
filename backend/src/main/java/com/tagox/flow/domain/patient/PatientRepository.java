package com.tagox.flow.domain.patient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    List<Patient> findAllByTenantIdOrderByNomeAsc(UUID tenantId);

    Optional<Patient> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndCpf(UUID tenantId, String cpf);

    boolean existsByTenantIdAndCpfAndIdNot(
            UUID tenantId,
            String cpf,
            UUID id
    );
}
