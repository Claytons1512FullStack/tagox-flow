package com.tagox.flow;

import com.tagox.flow.domain.role.RoleType;
import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.userrole.UserRoleRepository;
import com.tagox.flow.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserRoleRepository userRoleRepository;

    private Tenant tenant;

    private String token;

    @BeforeEach
    void prepararBanco() {

        userRepository.deleteAll();

        tenantRepository.deleteAll();

        tenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "22222222000122",
                "Empresa Teste",
                "Empresa Teste",
                "empresa-teste",
                TenantStatus.TRIAL
        );

        tenantRepository.save(tenant);

        UUID userId = UUID.randomUUID();

        token = jwtService.gerarToken(
                userId,
                tenant.getId()
        );

        when(userRoleRepository.findRoleTypesByUsuarioId(userId))
                .thenReturn(List.of(RoleType.ADMIN));
    }

    @Test
    void deveCriarUsuarioVinculadoAoTenant() throws Exception {

        String json = """
                {
                    "nome": "Clayton Usuario",
                    "email": "clayton@teste.com",
                    "senha": "senha-teste"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome")
                        .value("Clayton Usuario"))
                .andExpect(jsonPath("$.email")
                        .value("clayton@teste.com"))
                .andExpect(jsonPath("$.status")
                        .value("ATIVO"))
                .andExpect(jsonPath("$.tenantId")
                        .value(tenant.getId().toString()));
    }

    @Test
    void naoDevePermitirEmailDuplicadoNoMesmoTenant() throws Exception {

        String json = """
                {
                    "nome": "Primeiro Usuario",
                    "email": "usuario@teste.com",
                    "senha": "senha-teste"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Já existe um usuário com este email neste tenant"
                        ));
    }

    @Test
    void deveFalharQuandoTenantNaoExiste() throws Exception {

        UUID tenantInexistente = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String tokenTenantInexistente = jwtService.gerarToken(
                userId,
                tenantInexistente
        );

        when(userRoleRepository.findRoleTypesByUsuarioId(userId))
                .thenReturn(List.of(RoleType.ADMIN));

        String json = """
                {
                    "nome": "Usuario Invalido",
                    "email": "usuario@teste.com",
                    "senha": "senha-teste"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenTenantInexistente
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Tenant não encontrado"));
    }

    @Test
    void deveIsolarUsuariosEntreTenantsViaHttp() throws Exception {

        Tenant tenantA = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "44444444000144",
                "Empresa A",
                "Empresa A",
                "empresa-a",
                TenantStatus.ATIVO
        );

        Tenant tenantB = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "55555555000155",
                "Empresa B",
                "Empresa B",
                "empresa-b",
                TenantStatus.ATIVO
        );

        tenantRepository.save(tenantA);
        tenantRepository.save(tenantB);

        UUID userIdA = UUID.randomUUID();
        UUID userIdB = UUID.randomUUID();

        String tokenA = jwtService.gerarToken(
                userIdA,
                tenantA.getId()
        );

        String tokenB = jwtService.gerarToken(
                userIdB,
                tenantB.getId()
        );

        when(userRoleRepository.findRoleTypesByUsuarioId(userIdA))
                .thenReturn(List.of(RoleType.ADMIN));

        when(userRoleRepository.findRoleTypesByUsuarioId(userIdB))
                .thenReturn(List.of(RoleType.ADMIN));

        String jsonA = """
                {
                    "nome": "Usuario Empresa A",
                    "email": "mesmo@email.com",
                    "senha": "senha-teste"
                }
                """;

        String jsonB = """
                {
                    "nome": "Usuario Empresa B",
                    "email": "mesmo@email.com",
                    "senha": "senha-teste"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenA
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonA)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId")
                        .value(tenantA.getId().toString()));

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenB
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonB)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId")
                        .value(tenantB.getId().toString()));

        assertThat(userRepository.existsByTenantIdAndEmail(
                tenantA.getId(),
                "mesmo@email.com"
        )).isTrue();

        assertThat(userRepository.existsByTenantIdAndEmail(
                tenantB.getId(),
                "mesmo@email.com"
        )).isTrue();
    }

    @Test
    void profissionalNaoPodeCriarUsuario() throws Exception {

        UUID userId = UUID.randomUUID();

        String profissionalToken = jwtService.gerarToken(
                userId,
                tenant.getId()
        );

        when(userRoleRepository.findRoleTypesByUsuarioId(userId))
                .thenReturn(List.of(RoleType.PROFISSIONAL));

        String json = """
                {
                    "nome": "Usuario Profissional",
                    "email": "profissional@teste.com",
                    "senha": "senha-teste"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + profissionalToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void assistenteNaoPodeCriarUsuario() throws Exception {

        UUID userId = UUID.randomUUID();

        String assistenteToken = jwtService.gerarToken(
                userId,
                tenant.getId()
        );

        when(userRoleRepository.findRoleTypesByUsuarioId(userId))
                .thenReturn(List.of(RoleType.ASSISTENTE));

        String json = """
                {
                    "nome": "Usuario Assistente",
                    "email": "assistente@teste.com",
                    "senha": "senha-teste"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + assistenteToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isForbidden());
    }
}