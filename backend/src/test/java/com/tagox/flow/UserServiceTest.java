package com.tagox.flow;

import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.dto.user.CreateUserRequest;
import com.tagox.flow.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Tenant tenant;

    @BeforeEach
    void prepararBanco() {

        userRepository.deleteAll();
        tenantRepository.deleteAll();

        tenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "44444444000144",
                "Empresa Service Teste",
                "Empresa Service Teste",
                "empresa-service-teste",
                TenantStatus.TRIAL
        );

        tenantRepository.save(tenant);
    }

    @Test
    void deveArmazenarSenhaComoHashBCrypt() {

        CreateUserRequest request = new CreateUserRequest();

        request.setTenantId(tenant.getId());
        request.setNome("Usuario Service");
        request.setEmail("service@teste.com");
        request.setSenha("senha-secreta");

        User salvo = userService.criarUsuario(request);

        assertThat(salvo.getSenhaHash())
                .isNotNull()
                .isNotEqualTo("senha-secreta");

        assertThat(passwordEncoder.matches(
                "senha-secreta",
                salvo.getSenhaHash()
        )).isTrue();
    }

    @Test
    void deveNormalizarEmailParaMinusculo() {

        CreateUserRequest request = new CreateUserRequest();

        request.setTenantId(tenant.getId());
        request.setNome("Usuario Email");
        request.setEmail("USUARIO@TESTE.COM");
        request.setSenha("senha-secreta");

        User salvo = userService.criarUsuario(request);

        assertThat(salvo.getEmail())
                .isEqualTo("usuario@teste.com");
    }
}
