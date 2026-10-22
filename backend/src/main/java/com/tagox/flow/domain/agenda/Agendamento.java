package com.tagox.flow.domain.agenda;

import com.tagox.flow.domain.patient.Patient;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agendamento")
public class Agendamento {

    public enum Status {
        SOLICITADO,
        CONFIRMADO,
        CANCELADO,
        CONCLUIDO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private User profissional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Patient paciente;

    @Column(name = "inicio", nullable = false)
    private Instant inicio;

    @Column(name = "fim", nullable = false)
    private Instant fim;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.SOLICITADO;

    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Agendamento() {
    }

    public Agendamento(
            Tenant tenant,
            User profissional,
            Patient paciente,
            Instant inicio,
            Instant fim,
            String observacoes
    ) {
        this.tenant = tenant;
        this.profissional = profissional;
        this.paciente = paciente;
        this.inicio = inicio;
        this.fim = fim;
        this.observacoes = observacoes;
    }

    @PrePersist
    protected void prePersist() {
        Instant agora = Instant.now();
        this.createdAt = agora;
        this.updatedAt = agora;
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public User getProfissional() {
        return profissional;
    }

    public Patient getPaciente() {
        return paciente;
    }

    public Instant getInicio() {
        return inicio;
    }

    public Instant getFim() {
        return fim;
    }

    public Status getStatus() {
        return status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void confirmar() {
        if (this.status != Status.SOLICITADO) {
            throw new IllegalStateException(
                    "Somente agendamentos solicitados podem ser confirmados."
            );
        }

        this.status = Status.CONFIRMADO;
    }

    public void cancelar() {
        if (this.status == Status.CANCELADO
                || this.status == Status.CONCLUIDO) {
            throw new IllegalStateException(
                    "Este agendamento não pode ser cancelado."
            );
        }

        this.status = Status.CANCELADO;
    }

    public void concluir() {
        if (this.status != Status.CONFIRMADO) {
            throw new IllegalStateException(
                    "Somente agendamentos confirmados podem ser concluídos."
            );
        }

        this.status = Status.CONCLUIDO;
    }
}