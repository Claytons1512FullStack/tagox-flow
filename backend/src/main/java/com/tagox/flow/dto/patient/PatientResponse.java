package com.tagox.flow.dto.patient;

import com.tagox.flow.domain.patient.Patient;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class PatientResponse {

    private UUID id;
    private UUID tenantId;
    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private String telefone;
    private String email;
    private Instant criadoEm;
    private Instant atualizadoEm;

    public PatientResponse(
            UUID id,
            UUID tenantId,
            String nome,
            String cpf,
            LocalDate dataNascimento,
            String telefone,
            String email,
            Instant criadoEm,
            Instant atualizadoEm
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.email = email;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getTenant().getId(),
                patient.getNome(),
                patient.getCpf(),
                patient.getDataNascimento(),
                patient.getTelefone(),
                patient.getEmail(),
                patient.getCriadoEm(),
                patient.getAtualizadoEm()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
