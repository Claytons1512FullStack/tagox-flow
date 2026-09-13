package com.tagox.flow;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.tenant.TipoPessoa;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TenantPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void devePersistirERecuperarTenant() {

        UUID id = UUID.randomUUID();
        Instant agora = Instant.now();

        Tenant tenant = new Tenant(
                id,
                TipoPessoa.JURIDICA,
                "12345678000199",
                "TAGOX Tecnologia LTDA",
                "TAGOX Tech",
                "tagox-tech",
                TenantStatus.TRIAL,
                agora,
                agora
        );

        entityManager.persist(tenant);
        entityManager.flush();
        entityManager.clear();

        Tenant recuperado = entityManager.find(Tenant.class, id);

        assertThat(recuperado).isNotNull();
        assertThat(recuperado.getId()).isEqualTo(id);
        assertThat(recuperado.getTipoPessoa()).isEqualTo(TipoPessoa.JURIDICA);
        assertThat(recuperado.getDocumento()).isEqualTo("12345678000199");
        assertThat(recuperado.getNome()).isEqualTo("TAGOX Tecnologia LTDA");
        assertThat(recuperado.getNomeFantasia()).isEqualTo("TAGOX Tech");
        assertThat(recuperado.getSlug()).isEqualTo("tagox-tech");
        assertThat(recuperado.getStatus()).isEqualTo(TenantStatus.TRIAL);

        assertThat(recuperado.getCriadoEm())
                .isCloseTo(agora, within(1, ChronoUnit.MICROS));

        assertThat(recuperado.getAtualizadoEm())
                .isCloseTo(agora, within(1, ChronoUnit.MICROS));
    }
}
