package com.tagox.flow;

import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantService;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.exception.DuplicateResourceException;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Import(TenantService.class)
class TenantServiceTest {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    void deveConsiderarDocumentoFormatadoEnaoFormatadoComoDuplicado() {

        tenantService.criar(
                TipoPessoa.JURIDICA,
                "11.222.333/0001-81",
                "Primeiro Tenant",
                "Primeiro",
                "documento-formatado"
        );

        assertThatThrownBy(() ->
                tenantService.criar(
                        TipoPessoa.JURIDICA,
                        "11222333000181",
                        "Segundo Tenant",
                        "Segundo",
                        "documento-sem-formatacao"
                )
        )
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Documento já cadastrado.");
    }

    @Test
    void deveCriarTenantComStatusTrial() {

        Tenant tenant = tenantService.criar(
                TipoPessoa.JURIDICA,
                "12345678000195",
                "TAGOX Flow Tecnologia LTDA",
                "TAGOX Flow",
                "tagox-flow"
        );

        assertThat(tenant.getId())
                .isNotNull();

        assertThat(tenant.getTipoPessoa())
                .isEqualTo(TipoPessoa.JURIDICA);

        assertThat(tenant.getDocumento())
                .isEqualTo("12345678000195");

        assertThat(tenant.getNome())
                .isEqualTo("TAGOX Flow Tecnologia LTDA");

        assertThat(tenant.getNomeFantasia())
                .isEqualTo("TAGOX Flow");

        assertThat(tenant.getSlug())
                .isEqualTo("tagox-flow");

        assertThat(tenant.getStatus())
                .isEqualTo(TenantStatus.TRIAL);

        assertThat(tenant.getCriadoEm())
                .isNotNull();

        assertThat(tenant.getAtualizadoEm())
                .isNotNull();

        assertThat(
                tenantRepository.findById(tenant.getId())
        )
                .isPresent();
    }

    @Test
    void naoDevePermitirDocumentoDuplicado() {

        tenantService.criar(
                TipoPessoa.JURIDICA,
                "11222333000181",
                "Primeiro Tenant",
                "Primeiro",
                "primeiro-tenant"
        );

        assertThatThrownBy(() ->
                tenantService.criar(
                        TipoPessoa.JURIDICA,
                        "11222333000181",
                        "Segundo Tenant",
                        "Segundo",
                        "segundo-tenant"
                )
        )
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Documento já cadastrado.");
    }

    @Test
    void naoDevePermitirSlugDuplicado() {

        tenantService.criar(
                TipoPessoa.JURIDICA,
                "04252011000110",
                "Primeiro Tenant",
                "Primeiro",
                "slug-existente"
        );

        assertThatThrownBy(() ->
                tenantService.criar(
                        TipoPessoa.JURIDICA,
                        "04252011000209",
                        "Segundo Tenant",
                        "Segundo",
                        "slug-existente"
                )
        )
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Slug já cadastrado.");
    }
}