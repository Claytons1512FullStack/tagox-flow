package com.tagox.flow;

import com.tagox.flow.domain.role.Role;
import com.tagox.flow.domain.role.RoleRepository;
import com.tagox.flow.domain.role.RoleType;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.userrole.UserRoleRepository;
import com.tagox.flow.dto.user.CreateUserRequest;
import com.tagox.flow.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
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
    private UserRoleRepository userRoleRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Tenant tenant;

    @BeforeEach
    void prepararBanco() {

        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        garantirPapeisPadrao();

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

    private void garantirPapeisPadrao() {

        List<Role> papeisPadrao = List.of(
                new Role(
                        UUID.randomUUID(),
                        RoleType.ADMIN,
                        "Administrador"
                ),
                new Role(
                        UUID.randomUUID(),
                        RoleType.PROFISSIONAL,
                        "Profissional"
                ),
                new Role(
                        UUID.randomUUID(),
                        RoleType.ASSISTENTE,
                        "Assistente"
                )
        );

        for (Role papel : papeisPadrao) {
            roleRepository.findByTipo(papel.getTipo())
                    .orElseGet(() -> roleRepository.save(papel));
        }
    }

    @Test
    void deveArmazenarSenhaComoHashBCrypt() {

        CreateUserRequest request = new CreateUserRequest();

        request.setNome("Usuario Service");
        request.setEmail("service@teste.com");
        request.setSenha("senha-secreta");
        request.setRole(RoleType.PROFISSIONAL);

        User salvo = userService.criarUsuario(
                request,
                tenant.getId()
        );

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

        request.setNome("Usuario Email");
        request.setEmail("USUARIO@TESTE.COM");
        request.setSenha("senha-secreta");
        request.setRole(RoleType.PROFISSIONAL);

        User salvo = userService.criarUsuario(
                request,
                tenant.getId()
        );

        assertThat(salvo.getEmail())
                .isEqualTo("usuario@teste.com");
    }
}