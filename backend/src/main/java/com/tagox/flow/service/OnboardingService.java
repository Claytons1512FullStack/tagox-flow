package com.tagox.flow.service;

import com.tagox.flow.domain.role.RoleType;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantService;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.dto.onboarding.OnboardingRequest;
import com.tagox.flow.dto.onboarding.OnboardingResponse;
import com.tagox.flow.dto.tenant.TenantResponseDTO;
import com.tagox.flow.dto.user.CreateUserRequest;
import com.tagox.flow.dto.user.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingService {

    private final TenantService tenantService;
    private final UserService userService;
    private final UserRoleService userRoleService;

    public OnboardingService(
            TenantService tenantService,
            UserService userService,
            UserRoleService userRoleService
    ) {
        this.tenantService = tenantService;
        this.userService = userService;
        this.userRoleService = userRoleService;
    }

    @Transactional
    public OnboardingResponse cadastrar(OnboardingRequest request) {

        var dadosTenant = request.tenant();

        Tenant tenant = tenantService.criar(
                dadosTenant.tipoPessoa(),
                dadosTenant.documento(),
                dadosTenant.nome(),
                dadosTenant.nomeFantasia(),
                dadosTenant.slug()
        );

        var dadosAdministrador = request.administrador();

        CreateUserRequest administradorRequest = new CreateUserRequest();
        administradorRequest.setNome(dadosAdministrador.nome());
        administradorRequest.setEmail(dadosAdministrador.email());
        administradorRequest.setSenha(dadosAdministrador.senha());

        User administrador = userService.criarUsuario(
                administradorRequest,
                tenant.getId()
        );

        userRoleService.adicionarRole(
                administrador.getId(),
                RoleType.ADMIN
        );

        TenantResponseDTO tenantResponse = new TenantResponseDTO(
                tenant.getId(),
                tenant.getNome(),
                tenant.getNomeFantasia(),
                tenant.getSlug(),
                tenant.getStatus().name()
        );

        UserResponse administradorResponse = UserResponse.from(administrador);

        return new OnboardingResponse(
                tenantResponse,
                administradorResponse
        );
    }

}
