package com.tagox.flow.domain.tenant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public Tenant criar(
            TipoPessoa tipoPessoa,
            String documento,
            String nome,
            String nomeFantasia,
            String slug
    ) {
        if (tenantRepository.existsByDocumento(documento)) {
            throw new IllegalArgumentException("Documento já cadastrado.");
        }

        if (tenantRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Slug já cadastrado.");
        }

        Instant agora = Instant.now();

        Tenant tenant = new Tenant(
                UUID.randomUUID(),
                tipoPessoa,
                documento,
                nome,
                nomeFantasia,
                slug,
                TenantStatus.TRIAL,
                agora,
                agora
        );

        return tenantRepository.save(tenant);
    }
}