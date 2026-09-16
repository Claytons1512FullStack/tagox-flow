package com.tagox.flow;

import com.tagox.flow.domain.role.Role;
import com.tagox.flow.domain.role.RoleRepository;
import com.tagox.flow.domain.role.RoleType;
import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.user.UserStatus;
import com.tagox.flow.domain.userrole.UserRole;
import com.tagox.flow.domain.userrole.UserRoleId;
import com.tagox.flow.domain.userrole.UserRoleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserRolePersistenceTest {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant tenant;
    private User user;
    private Role role;

    @BeforeEach
    void prepararBanco() {

        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        tenantRepository.deleteAll();

        tenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "44444444000144",
                "Empresa UserRole Teste",
                "Empresa UserRole Teste",
                "empresa-userrole-teste",
                TenantStatus.TRIAL
        );

        tenantRepository.save(tenant);

        user = new User(
                UUID.randomUUID(),
                tenant,
                "Usuario UserRole",
                "userrole@teste.com",
                "hash-teste",
                UserStatus.ATIVO
        );

        userRepository.save(user);

        role = new Role(
                UUID.randomUUID(),
                RoleType.ADMIN,
                "Administrador"
        );

        roleRepository.save(role);
    }

    @Test
    void devePersistirUserRoleComChaveComposta() {

        UserRole userRole = new UserRole(
                user,
                role
        );

        UserRole salvo = userRoleRepository.save(userRole);

        assertThat(salvo.getId())
                .isNotNull();

        assertThat(salvo.getId().getUsuarioId())
                .isEqualTo(user.getId());

        assertThat(salvo.getId().getRoleId())
                .isEqualTo(role.getId());

        assertThat(salvo.getUsuario())
                .isNotNull();

        assertThat(salvo.getUsuario().getId())
                .isEqualTo(user.getId());

        assertThat(salvo.getRole())
                .isNotNull();

        assertThat(salvo.getRole().getId())
                .isEqualTo(role.getId());
    }

    @Test
    void deveEncontrarUserRolePeloUsuarioERole() {

        UserRole userRole = new UserRole(
                user,
                role
        );

        userRoleRepository.save(userRole);

        boolean existe =
                userRoleRepository.existsByIdUsuarioIdAndIdRoleId(
                        user.getId(),
                        role.getId()
                );

        assertThat(existe)
                .isTrue();
    }

    @Test
    void deveEncontrarUserRolePelaChaveComposta() {

        UserRole userRole = new UserRole(
                user,
                role
        );

        userRoleRepository.save(userRole);

        UserRoleId id = new UserRoleId(
                user.getId(),
                role.getId()
        );

        var encontrado = userRoleRepository.findById(id);

        assertThat(encontrado)
                .isPresent();

        assertThat(encontrado.get().getUsuario().getId())
                .isEqualTo(user.getId());

        assertThat(encontrado.get().getRole().getId())
                .isEqualTo(role.getId());
    }
}
