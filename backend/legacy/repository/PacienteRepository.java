package com.tagox.flow.repository;

import com.tagox.flow.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
    Optional<Paciente> findByCpf(String cpf);
}
