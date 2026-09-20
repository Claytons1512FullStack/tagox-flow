package com.tagox.flow;

import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.dto.auth.LoginRequest;
import com.tagox.flow.dto.user.CreateUserRequest;
import com.tagox.flow.exception.ResourceNotFoundException;
import com.tagox.flow.security.JwtService;
import com.tagox.flow.service.AuthService;
import com.tagox.flow.service.UserService;

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
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private JwtService jwtService;

    private Tenant tenant;

    @BeforeEach
    void prepararBanco() {

        userRepository.deleteAll();
        tenantRepository.deleteAll();

        tenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "55555555000155",
                "Empresa Auth Teste",
                "Empresa Auth Teste",
                "empresa-auth-teste",
                TenantStatus.ATIVO
        );

        tenantRepository.save(tenant);
    }

    @Test
    void deveAutenticarUsuarioComCredenciaisValidas() {

        User usuario = criarUsuario(
                "Usuario Auth",
                "auth@teste.com",
                "senha-secreta"
        );

        LoginRequest request = new LoginRequest();

        request.setTenantSlug("empresa-auth-teste");
        request.setEmail("auth@teste.com");
        request.setSenha("senha-secreta");

        String token = authService.autenticar(request);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();

        assertThat(jwtService.extrairUserId(token))
                .isEqualTo(usuario.getId());

        assertThat(jwtService.extrairTenantId(token))
                .isEqualTo(tenant.getId());
    }

    @Test
    void deveRejeitarSenhaIncorreta() {

        criarUsuario(
                "Usuario Auth",
                "auth@teste.com",
                "senha-secreta"
        );

        LoginRequest request = new LoginRequest();

        request.setTenantSlug("empresa-auth-teste");
        request.setEmail("auth@teste.com");
        request.setSenha("senha-errada");

        assertThatThrownBy(() ->
                authService.autenticar(request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    void deveRejeitarUsuarioInexistente() {

        LoginRequest request = new LoginRequest();

        request.setTenantSlug("empresa-auth-teste");
        request.setEmail("inexistente@teste.com");
        request.setSenha("senha-secreta");

        assertThatThrownBy(() ->
                authService.autenticar(request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuário não encontrado");
    }

    @Test
    void deveRejeitarTenantInexistente() {

        LoginRequest request = new LoginRequest();

        request.setTenantSlug("tenant-inexistente");
        request.setEmail("auth@teste.com");
        request.setSenha("senha-secreta");

        assertThatThrownBy(() ->
                authService.autenticar(request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tenant não encontrado");
    }

    @Test
    void deveRejeitarTenantNaoAtivo() {

        tenantRepository.deleteAll();

        tenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "66666666000166",
                "Empresa Suspensa",
                "Empresa Suspensa",
                "empresa-suspensa",
                TenantStatus.SUSPENSO
        );

        tenantRepository.save(tenant);

        LoginRequest request = new LoginRequest();

        request.setTenantSlug("empresa-suspensa");
        request.setEmail("auth@teste.com");
        request.setSenha("senha-secreta");

        assertThatThrownBy(() ->
                authService.autenticar(request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tenant não está disponível para autenticação");
    }

    @Test
    void deveManterIsolamentoEntreTenants() {

        User usuarioA = criarUsuario(
                "Usuario Empresa A",
                "mesmo@email.com",
                "senha-a"
        );

        Tenant outroTenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "77777777000177",
                "Empresa B",
                "Empresa B",
                "empresa-b",
                TenantStatus.ATIVO
        );

        tenantRepository.save(outroTenant);

        CreateUserRequest requestUsuarioB = new CreateUserRequest();

        requestUsuarioB.setNome("Usuario Empresa B");
        requestUsuarioB.setEmail("mesmo@email.com");
        requestUsuarioB.setSenha("senha-b");

        User usuarioB = userService.criarUsuario(
                requestUsuarioB,
                outroTenant.getId()
        );

        LoginRequest loginEmpresaA = new LoginRequest();

        loginEmpresaA.setTenantSlug("empresa-auth-teste");
        loginEmpresaA.setEmail("mesmo@email.com");
        loginEmpresaA.setSenha("senha-a");

        LoginRequest loginEmpresaB = new LoginRequest();

        loginEmpresaB.setTenantSlug("empresa-b");
        loginEmpresaB.setEmail("mesmo@email.com");
        loginEmpresaB.setSenha("senha-b");

        String tokenA = authService.autenticar(loginEmpresaA);
        String tokenB = authService.autenticar(loginEmpresaB);

        assertThat(jwtService.extrairTenantId(tokenA))
                .isEqualTo(tenant.getId());

        assertThat(jwtService.extrairTenantId(tokenB))
                .isEqualTo(outroTenant.getId());

        assertThat(jwtService.extrairUserId(tokenA))
                .isEqualTo(usuarioA.getId());

        assertThat(jwtService.extrairUserId(tokenB))
                .isEqualTo(usuarioB.getId());

        assertThat(tokenA)
                .isNotEqualTo(tokenB);
    }

    private User criarUsuario(
            String nome,
            String email,
            String senha
    ) {

        CreateUserRequest request = new CreateUserRequest();

        request.setNome(nome);
        request.setEmail(email);
        request.setSenha(senha);

        return userService.criarUsuario(
                request,
                tenant.getId()
        );
    }
}
