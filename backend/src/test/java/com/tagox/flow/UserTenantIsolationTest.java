package com.tagox.flow;

import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.user.UserStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class UserTenantIsolationTest {


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private TenantRepository tenantRepository;

    @BeforeEach
    void prepararBanco() {
        userRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    void devePermitirMesmoEmailEmTenantsDiferentes() {


        Tenant tenantA = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "11111111111111",
                "Empresa A",
                "Empresa A",
                "empresa-a",
                TenantStatus.ATIVO
        );


        Tenant tenantB = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "22222222222222",
                "Empresa B",
                "Empresa B",
                "empresa-b",
                TenantStatus.ATIVO
        );


        tenantRepository.save(tenantA);
        tenantRepository.save(tenantB);



        User userA = new User(
                UUID.randomUUID(),
                tenantA,
                "Usuario Empresa A",
                "usuario@email.com",
                "hash123",
                UserStatus.ATIVO
        );


        User userB = new User(
                UUID.randomUUID(),
                tenantB,
                "Usuario Empresa B",
                "usuario@email.com",
                "hash123",
                UserStatus.ATIVO
        );


        userRepository.save(userA);
        userRepository.save(userB);



        boolean existeTenantA =
                userRepository.existsByTenantIdAndEmail(
                        tenantA.getId(),
                        "usuario@email.com"
                );


        boolean existeTenantB =
                userRepository.existsByTenantIdAndEmail(
                        tenantB.getId(),
                        "usuario@email.com"
                );



        assertThat(existeTenantA).isTrue();

        assertThat(existeTenantB).isTrue();


        assertThat(userA.getTenant().getId())
                .isEqualTo(tenantA.getId());


        assertThat(userB.getTenant().getId())
                .isEqualTo(tenantB.getId());

    }



    @Test
    void deveBloquearEmailDuplicadoDentroDoMesmoTenant() {


        Tenant tenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "33333333333333",
                "Empresa C",
                "Empresa C",
                "empresa-c",
                TenantStatus.ATIVO
        );


        tenantRepository.save(tenant);



        User primeiroUsuario = new User(
                UUID.randomUUID(),
                tenant,
                "Primeiro Usuario",
                "duplicado@email.com",
                "hash123",
                UserStatus.ATIVO
        );


        userRepository.save(primeiroUsuario);



        boolean existe =
                userRepository.existsByTenantIdAndEmail(
                        tenant.getId(),
                        "duplicado@email.com"
                );


        assertThat(existe).isTrue();

    }

}