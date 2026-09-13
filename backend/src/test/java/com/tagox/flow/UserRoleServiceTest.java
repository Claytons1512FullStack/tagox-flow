package com.tagox.flow;

import com.tagox.flow.domain.role.Role;
import com.tagox.flow.domain.role.RoleRepository;
import com.tagox.flow.domain.role.RoleType;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.user.UserStatus;
import com.tagox.flow.domain.userrole.UserRole;
import com.tagox.flow.domain.userrole.UserRoleRepository;
import com.tagox.flow.service.UserRoleService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class UserRoleServiceTest {


    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserRoleRepository userRoleRepository;

    private UserRoleService service;


    @BeforeEach
    void setup() {

        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        userRoleRepository = mock(UserRoleRepository.class);


        service = new UserRoleService(
                userRepository,
                roleRepository,
                userRoleRepository
        );

    }



    @Test
    void deveAdicionarRoleAoUsuario() {


        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();



        Tenant tenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "123456789",
                "Empresa Teste",
                "Empresa Teste",
                "empresa-teste",
                TenantStatus.ATIVO
        );



        User user = new User(
                userId,
                tenant,
                "Clayton",
                "clayton@test.com",
                "hash",
                UserStatus.ATIVO
        );



        Role role = new Role(
                roleId,
                RoleType.ADMIN,
                "Administrador"
        );



        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));



        when(roleRepository.findByTipo(RoleType.ADMIN))
                .thenReturn(Optional.of(role));



        when(userRoleRepository.existsByIdUsuarioIdAndIdRoleId(
                userId,
                roleId
        ))
                .thenReturn(false);



        when(userRoleRepository.save(any(UserRole.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));



        UserRole resultado = service.adicionarRole(
                userId,
                RoleType.ADMIN
        );



        assertNotNull(resultado);

        assertEquals(user, resultado.getUsuario());

        assertEquals(role, resultado.getRole());



        verify(userRoleRepository)
                .save(any(UserRole.class));

    }

}