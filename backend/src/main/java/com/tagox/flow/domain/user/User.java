package com.tagox.flow.domain.user;

import com.tagox.flow.domain.tenant.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuario")
public class User {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;


    protected User() {
        // Construtor protegido exigido pelo Hibernate
    }


    public User(
            UUID id,
            Tenant tenant,
            String nome,
            String email,
            String senhaHash,
            UserStatus status
    ) {
        this.id = id;
        this.tenant = tenant;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.status = status;
    }


    @PrePersist
    private void antesDePersistir() {

        Instant agora = Instant.now();

        if (this.id == null) {
            this.id = UUID.randomUUID();
        }

        if (this.criadoEm == null) {
            this.criadoEm = agora;
        }

        if (this.atualizadoEm == null) {
            this.atualizadoEm = agora;
        }
    }


    @PreUpdate
    private void antesDeAtualizar() {

        this.atualizadoEm = Instant.now();

    }


    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}