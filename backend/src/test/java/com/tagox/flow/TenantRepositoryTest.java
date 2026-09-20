package com.tagox.flow;

import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.userrole.UserRoleRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@Transactional
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TenantRepositoryTest {


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
    void deveVerificarExistenciaPorDocumentoESlug() {

        Tenant tenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "99999999000199",
                "TAGOX Tecnologia LTDA",
                "TAGOX Tech",
                "tagox-tech",
                TenantStatus.TRIAL,
                Instant.now(),
                Instant.now()
        );


        tenantRepository.saveAndFlush(tenant);


        assertThat(
                tenantRepository.existsByDocumento("99999999000199")
        )
                .isTrue();


        assertThat(
                tenantRepository.existsBySlug("tagox-tech")
        )
                .isTrue();


        assertThat(
                tenantRepository.existsByDocumento("99999999999999")
        )
                .isFalse();


        assertThat(
                tenantRepository.existsBySlug("tenant-inexistente")
        )
                .isFalse();
    }
}
