package com.tagox.flow;

import com.tagox.flow.domain.role.RoleType;
import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.user.UserStatus;
import com.tagox.flow.domain.userrole.UserRoleRepository;
import com.tagox.flow.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private UserRoleRepository userRoleRepository;

    private Tenant tenant;

    private UUID userId;

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

        userId = UUID.randomUUID();

        criarUsuarioAutenticavel(
                userId,
                tenant,
                "admin@teste.com"
        );

        token = jwtService.gerarToken(
                userId,
                tenant.getId()
        );

        when(userRoleRepository.findRoleTypesByUsuarioId(userId))
                .thenReturn(List.of(RoleType.ADMIN));
    }

    private User criarUsuarioAutenticavel(
            UUID id,
            Tenant tenantUsuario,
            String email
    ) {

        User usuario = new User(
                id,
                tenantUsuario,
                "Usuario de Teste",
                email,
                "senha-hash-teste",
                UserStatus.ATIVO
        );

        return userRepository.save(usuario);
    }

    @Test
    void deveCriarUsuarioVinculadoAoTenant() throws Exception {

        String json = """
                {
                    "nome": "Clayton Usuario",
                    "email": "clayton@teste.com",
                    "senha": "senha-teste",
                    "role": "PROFISSIONAL"
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
                    "senha": "senha-teste",
                    "role": "PROFISSIONAL"
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
    void deveRejeitarTokenDeTenantInexistente() throws Exception {

        UUID tenantInexistente = UUID.randomUUID();
        UUID usuarioInexistente = UUID.randomUUID();

        String tokenTenantInexistente = jwtService.gerarToken(
                usuarioInexistente,
                tenantInexistente
        );

        String json = """
                {
                    "nome": "Usuario Invalido",
                    "email": "usuario@teste.com",
                    "senha": "senha-teste",
                    "role": "PROFISSIONAL"
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
                .andExpect(status().isForbidden());
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

        criarUsuarioAutenticavel(
                userIdA,
                tenantA,
                "admin@empresa-a.com"
        );

        criarUsuarioAutenticavel(
                userIdB,
                tenantB,
                "admin@empresa-b.com"
        );

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
                    "senha": "senha-teste",
                    "role": "PROFISSIONAL"
                }
                """;

        String jsonB = """
                {
                    "nome": "Usuario Empresa B",
                    "email": "mesmo@email.com",
                    "senha": "senha-teste",
                    "role": "PROFISSIONAL"
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

        UUID profissionalId = UUID.randomUUID();

        criarUsuarioAutenticavel(
                profissionalId,
                tenant,
                "profissional@teste.com"
        );

        String profissionalToken = jwtService.gerarToken(
                profissionalId,
                tenant.getId()
        );

        when(userRoleRepository.findRoleTypesByUsuarioId(profissionalId))
                .thenReturn(List.of(RoleType.PROFISSIONAL));

        String json = """
                {
                    "nome": "Usuario Profissional",
                    "email": "novo-profissional@teste.com",
                    "senha": "senha-teste",
                    "role": "PROFISSIONAL"
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

        UUID assistenteId = UUID.randomUUID();

        criarUsuarioAutenticavel(
                assistenteId,
                tenant,
                "assistente@teste.com"
        );

        String assistenteToken = jwtService.gerarToken(
                assistenteId,
                tenant.getId()
        );

        when(userRoleRepository.findRoleTypesByUsuarioId(assistenteId))
                .thenReturn(List.of(RoleType.ASSISTENTE));

        String json = """
                {
                    "nome": "Usuario Assistente",
                    "email": "novo-assistente@teste.com",
                    "senha": "senha-teste",
                    "role": "PROFISSIONAL"
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

    @Test
    void adminNaoPodeCriarOutroAdminPeloCadastroComum() throws Exception {

        String json = """
                {
                    "nome": "Outro Administrador",
                    "email": "outro-admin@teste.com",
                    "senha": "senha-teste",
                    "role": "ADMIN"
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(
                                "O cadastro comum permite apenas os papéis PROFISSIONAL ou ASSISTENTE"
                        ));

        assertThat(userRepository.existsByTenantIdAndEmail(
                tenant.getId(),
                "outro-admin@teste.com"
        )).isFalse();
    }

    @Test
    void deveBloquearTokenQuandoUsuarioForBloqueado() throws Exception {

        jdbcTemplate.update(
                "UPDATE usuario SET status = ? WHERE id = ?",
                UserStatus.BLOQUEADO.name(),
                userId
        );

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "nome": "Novo Usuario",
                                            "email": "novo@teste.com",
                                            "senha": "senha-teste",
                                            "role": "PROFISSIONAL"
                                        }
                                        """)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBloquearTokenQuandoUsuarioForInativado() throws Exception {

        jdbcTemplate.update(
                "UPDATE usuario SET status = ? WHERE id = ?",
                UserStatus.INATIVO.name(),
                userId
        );

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "nome": "Novo Usuario",
                                            "email": "novo@teste.com",
                                            "senha": "senha-teste",
                                            "role": "PROFISSIONAL"
                                        }
                                        """)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBloquearTokenQuandoTenantForSuspenso() throws Exception {

        jdbcTemplate.update(
                "UPDATE tenant SET status = ? WHERE id = ?",
                TenantStatus.SUSPENSO.name(),
                tenant.getId()
        );

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "nome": "Novo Usuario",
                                            "email": "novo@teste.com",
                                            "senha": "senha-teste",
                                            "role": "PROFISSIONAL"
                                        }
                                        """)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBloquearTokenQuandoTenantForCancelado() throws Exception {

        jdbcTemplate.update(
                "UPDATE tenant SET status = ? WHERE id = ?",
                TenantStatus.CANCELADO.name(),
                tenant.getId()
        );

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "nome": "Novo Usuario",
                                            "email": "novo@teste.com",
                                            "senha": "senha-teste",
                                            "role": "PROFISSIONAL"
                                        }
                                        """)
                )
                .andExpect(status().isForbidden());
    }
}