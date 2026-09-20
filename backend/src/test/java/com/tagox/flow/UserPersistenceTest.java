package com.tagox.flow;

import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.user.UserStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class UserPersistenceTest {


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private TenantRepository tenantRepository;


    private Tenant tenant;


    @BeforeEach
    void prepararBanco() {

        userRepository.deleteAll();
        tenantRepository.deleteAll();


        tenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "33333333000133",
                "Empresa Persistencia Teste",
                "Empresa Persistencia Teste",
                "empresa-persistencia-teste",
                TenantStatus.TRIAL
        );


        tenantRepository.save(tenant);
    }


    @Test
    void devePersistirUsuarioVinculadoAoTenant() {


        User user = new User(
                UUID.randomUUID(),
                tenant,
                "Usuario Persistencia",
                "persistencia@teste.com",
                "hash-teste",
                UserStatus.ATIVO
        );


        User salvo = userRepository.save(user);


        assertThat(salvo.getId())
                .isNotNull();


        assertThat(salvo.getNome())
                .isEqualTo("Usuario Persistencia");


        assertThat(salvo.getEmail())
                .isEqualTo("persistencia@teste.com");


        assertThat(salvo.getStatus())
                .isEqualTo(UserStatus.ATIVO);


        assertThat(salvo.getTenant())
                .isNotNull();


        assertThat(salvo.getTenant().getId())
                .isEqualTo(tenant.getId());


        assertThat(salvo.getCriadoEm())
                .isNotNull();


        assertThat(salvo.getAtualizadoEm())
                .isNotNull();
    }


    @Test
    void deveEncontrarUsuarioPorTenantEEmail() {


        User user = new User(
                UUID.randomUUID(),
                tenant,
                "Usuario Busca",
                "busca@teste.com",
                "hash-teste",
                UserStatus.ATIVO
        );


        userRepository.save(user);


        boolean existe =
                userRepository.existsByTenantIdAndEmail(
                        tenant.getId(),
                        "busca@teste.com"
                );


        assertThat(existe)
                .isTrue();
    }
}
