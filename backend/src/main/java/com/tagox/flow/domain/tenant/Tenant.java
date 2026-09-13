package com.tagox.flow.domain.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant")
public class Tenant {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false)
    private TipoPessoa tipoPessoa;

    @Column(nullable = false, unique = true, length = 20)
    private String documento;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "nome_fantasia", nullable = false, length = 150)
    private String nomeFantasia;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;


    protected Tenant() {
        // Construtor protegido exigido pelo Hibernate
    }


    public Tenant(
            UUID id,
            TipoPessoa tipoPessoa,
            String documento,
            String nome,
            String nomeFantasia,
            String slug,
            TenantStatus status
    ) {
        this.id = id;
        this.tipoPessoa = tipoPessoa;
        this.documento = documento;
        this.nome = nome;
        this.nomeFantasia = nomeFantasia;
        this.slug = slug;
        this.status = status;
    }


    public Tenant(
            UUID id,
            TipoPessoa tipoPessoa,
            String documento,
            String nome,
            String nomeFantasia,
            String slug,
            TenantStatus status,
            Instant criadoEm,
            Instant atualizadoEm
    ) {
        this.id = id;
        this.tipoPessoa = tipoPessoa;
        this.documento = documento;
        this.nome = nome;
        this.nomeFantasia = nomeFantasia;
        this.slug = slug;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
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

    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }

    public String getDocumento() {
        return documento;
    }

    public String getNome() {
        return nome;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public String getSlug() {
        return slug;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}