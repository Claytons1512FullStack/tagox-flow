package com.tagox.flow;


import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.tenant.TipoPessoa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("test")
@SpringBootTest
class TenantPersistenceTest {


    @Autowired
    private TenantRepository tenantRepository;


    @BeforeEach
    void limparBanco() {

        tenantRepository.deleteAll();

    }


    @Test
    void devePersistirTenantNoBanco() {


        Tenant tenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "12345678000199",
                "Empresa Teste LTDA",
                "Empresa Teste",
                "empresa-teste",
                TenantStatus.TRIAL,
                null,
                null
        );


        Tenant salvo = tenantRepository.saveAndFlush(tenant);


        assertThat(salvo.getId())
                .isNotNull();


        assertThat(salvo.getDocumento())
                .isEqualTo("12345678000199");


        assertThat(salvo.getNome())
                .isEqualTo("Empresa Teste LTDA");


        assertThat(salvo.getNomeFantasia())
                .isEqualTo("Empresa Teste");


        assertThat(salvo.getSlug())
                .isEqualTo("empresa-teste");


        assertThat(salvo.getStatus())
                .isEqualTo(TenantStatus.TRIAL);


        assertThat(
                tenantRepository.findById(salvo.getId())
        )
                .isPresent();

    }
}

