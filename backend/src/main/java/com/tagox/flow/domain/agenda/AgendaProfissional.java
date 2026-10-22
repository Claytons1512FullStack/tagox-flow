package com.tagox.flow.domain.agenda;

import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
        name = "agenda_profissional",
        indexes = {
                @Index(
                        name = "idx_agenda_profissional_tenant_profissional",
                        columnList = "tenant_id, profissional_id"
                ),
                @Index(
                        name = "idx_agenda_profissional_dia_ativo",
                        columnList = "tenant_id, profissional_id, dia_semana"
                )
        }
)
public class AgendaProfissional {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private User profissional;

    @Column(name = "dia_semana", nullable = false)
    private Short diaSemana;

    @Column(name = "horario_inicio", nullable = false)
    private LocalTime horarioInicio;

    @Column(name = "horario_fim", nullable = false)
    private LocalTime horarioFim;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AgendaProfissional() {
    }

    public AgendaProfissional(
            Tenant tenant,
            User profissional,
            Short diaSemana,
            LocalTime horarioInicio,
            LocalTime horarioFim
    ) {
        this.tenant = tenant;
        this.profissional = profissional;
        this.diaSemana = diaSemana;
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
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

    public Short getDiaSemana() {
        return diaSemana;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public LocalTime getHorarioFim() {
        return horarioFim;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void atualizarHorario(
            Short diaSemana,
            LocalTime horarioInicio,
            LocalTime horarioFim
    ) {
        this.diaSemana = diaSemana;
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }
}