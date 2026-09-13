package com.tagox.flow;

import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantService;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.tenant.TipoPessoa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TenantService.class)
class TenantServiceTest {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    void deveCriarTenantComStatusTrial() {

        Tenant tenant = tenantService.criar(
                TipoPessoa.JURIDICA,
                "98765432000188",
                "TAGOX Flow Tecnologia LTDA",
                "TAGOX Flow",
                "tagox-flow"
        );

        assertThat(tenant.getId()).isNotNull();
        assertThat(tenant.getTipoPessoa()).isEqualTo(TipoPessoa.JURIDICA);
        assertThat(tenant.getDocumento()).isEqualTo("98765432000188");
        assertThat(tenant.getNome()).isEqualTo("TAGOX Flow Tecnologia LTDA");
        assertThat(tenant.getNomeFantasia()).isEqualTo("TAGOX Flow");
        assertThat(tenant.getSlug()).isEqualTo("tagox-flow");
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.TRIAL);
        assertThat(tenant.getCriadoEm()).isNotNull();
        assertThat(tenant.getAtualizadoEm()).isNotNull();

        assertThat(tenantRepository.findById(tenant.getId()))
                .isPresent();
    }

    @Test
    void naoDevePermitirDocumentoDuplicado() {

        tenantService.criar(
                TipoPessoa.JURIDICA,
                "11111111000111",
                "Primeiro Tenant",
                "Primeiro",
                "primeiro-tenant"
        );

        assertThatThrownBy(() ->
                tenantService.criar(
                        TipoPessoa.JURIDICA,
                        "11111111000111",
                        "Segundo Tenant",
                        "Segundo",
                        "segundo-tenant"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Documento já cadastrado.");
    }

    @Test
    void naoDevePermitirSlugDuplicado() {

        tenantService.criar(
                TipoPessoa.JURIDICA,
                "22222222000122",
                "Primeiro Tenant",
                "Primeiro",
                "slug-existente"
        );

        assertThatThrownBy(() ->
                tenantService.criar(
                        TipoPessoa.JURIDICA,
                        "33333333000133",
                        "Segundo Tenant",
                        "Segundo",
                        "slug-existente"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Slug já cadastrado.");
    }
}