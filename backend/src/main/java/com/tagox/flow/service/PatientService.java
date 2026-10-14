package com.tagox.flow.service;

import com.tagox.flow.domain.patient.Patient;
import com.tagox.flow.domain.patient.PatientRepository;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.dto.patient.CreatePatientRequest;
import com.tagox.flow.exception.BusinessException;
import com.tagox.flow.exception.DuplicateResourceException;
import com.tagox.flow.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final TenantRepository tenantRepository;

    public PatientService(
            PatientRepository patientRepository,
            TenantRepository tenantRepository
    ) {
        this.patientRepository = patientRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public Patient criarPaciente(
            CreatePatientRequest request,
            UUID tenantId
    ) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tenant não encontrado")
                );

        String nome = normalizarNome(request.getNome());
        String cpf = normalizarCpf(request.getCpf());

        validarCpf(cpf);
        validarCpfDuplicado(tenantId, cpf);

        Patient patient = new Patient(
                UUID.randomUUID(),
                tenant,
                nome,
                cpf,
                request.getDataNascimento(),
                normalizarTextoOpcional(request.getTelefone()),
                normalizarEmail(request.getEmail())
        );

        return patientRepository.save(patient);
    }

    @Transactional(readOnly = true)
    public List<Patient> listarPacientes(UUID tenantId) {
        return patientRepository.findAllByTenantIdOrderByNomeAsc(tenantId);
    }

    @Transactional(readOnly = true)
    public Patient buscarPaciente(UUID pacienteId, UUID tenantId) {
        return patientRepository.findByIdAndTenantId(pacienteId, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Paciente não encontrado")
                );
    }

    @Transactional
    public Patient atualizarPaciente(
            UUID pacienteId,
            CreatePatientRequest request,
            UUID tenantId
    ) {
        Patient patient = buscarPaciente(pacienteId, tenantId);

        String nome = normalizarNome(request.getNome());
        String cpf = normalizarCpf(request.getCpf());

        validarCpf(cpf);

        if (cpf != null
                && patientRepository.existsByTenantIdAndCpfAndIdNot(
                        tenantId,
                        cpf,
                        pacienteId
                )) {
            throw new DuplicateResourceException(
                    "Já existe um paciente com este CPF neste tenant"
            );
        }

        patient.atualizarDados(
                nome,
                cpf,
                request.getDataNascimento(),
                normalizarTextoOpcional(request.getTelefone()),
                normalizarEmail(request.getEmail())
        );

        return patientRepository.save(patient);
    }

    private void validarCpfDuplicado(UUID tenantId, String cpf) {
        if (cpf != null
                && patientRepository.existsByTenantIdAndCpf(tenantId, cpf)) {
            throw new DuplicateResourceException(
                    "Já existe um paciente com este CPF neste tenant"
            );
        }
    }

    private String normalizarNome(String nome) {
        return nome.trim().replaceAll("\\s+", " ");
    }

    private String normalizarTextoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    private String normalizarEmail(String email) {
        String normalizado = normalizarTextoOpcional(email);

        return normalizado == null
                ? null
                : normalizado.toLowerCase(Locale.ROOT);
    }

    private String normalizarCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return null;
        }

        return cpf.replaceAll("[^0-9]", "");
    }

    private void validarCpf(String cpf) {
        if (cpf == null) {
            return;
        }

        if (cpf.length() != 11
                || cpf.chars().distinct().count() == 1
                || !digitosCpfValidos(cpf)) {
            throw new BusinessException("O CPF informado é inválido");
        }
    }

    private boolean digitosCpfValidos(String cpf) {
        int soma = 0;

        for (int i = 0; i < 9; i++) {
            soma += (cpf.charAt(i) - '0') * (10 - i);
        }

        int primeiroDigito = (soma * 10) % 11;

        if (primeiroDigito == 10) {
            primeiroDigito = 0;
        }

        if (primeiroDigito != cpf.charAt(9) - '0') {
            return false;
        }

        soma = 0;

        for (int i = 0; i < 10; i++) {
            soma += (cpf.charAt(i) - '0') * (11 - i);
        }

        int segundoDigito = (soma * 10) % 11;

        if (segundoDigito == 10) {
            segundoDigito = 0;
        }

        return segundoDigito == cpf.charAt(10) - '0';
    }
}
