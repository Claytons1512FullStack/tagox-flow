package com.tagox.flow;

import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.userrole.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
class TenantPersistenceTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @BeforeEach
    void limparBanco() {
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    void devePersistirTenantNoBanco() {

        Tenant tenant = criarTenant();

        Tenant salvo = tenantRepository.saveAndFlush(tenant);

        assertThat(salvo.getId()).isNotNull();

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

        assertThat(salvo.getIntervaloEntreConsultasMinutos())
                .isEqualTo(10);

        assertThat(tenantRepository.findById(salvo.getId()))
                .isPresent();
    }

    @Test
    void devePersistirIntervaloPersonalizado() {

        Tenant tenant = criarTenant();

        tenant.atualizarIntervaloEntreConsultasMinutos(15);

        Tenant salvo = tenantRepository.saveAndFlush(tenant);

        assertThat(salvo.getIntervaloEntreConsultasMinutos())
                .isEqualTo(15);
    }

    @Test
    void devePermitirIntervaloZero() {

        Tenant tenant = criarTenant();

        tenant.atualizarIntervaloEntreConsultasMinutos(0);

        Tenant salvo = tenantRepository.saveAndFlush(tenant);

        assertThat(salvo.getIntervaloEntreConsultasMinutos())
                .isZero();
    }

    @Test
    void deveRejeitarIntervaloNegativo() {

        Tenant tenant = criarTenant();

        assertThatThrownBy(
                () -> tenant.atualizarIntervaloEntreConsultasMinutos(-1)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "O intervalo entre consultas não pode ser negativo."
                );
    }

    private Tenant criarTenant() {

        return new Tenant(
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
    }
}