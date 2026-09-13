package com.tagox.flow;


import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.domain.tenant.TenantStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


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
                "Empresa Teste LTDA",
                "Empresa Teste",
                "12345678000199",
                "empresa-teste",
                TenantStatus.TRIAL,
                null,
                null
        );


        Tenant salvo = tenantRepository.saveAndFlush(tenant);


        assertThat(salvo.getId())
                .isNotNull();


        assertThat(
                tenantRepository.findById(salvo.getId())
        )
                .isPresent();
    }
}