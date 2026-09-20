package com.tagox.flow;

import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        token = jwtService.gerarToken(
                UUID.randomUUID(),
                tenant.getId()
        );
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

        String tokenTenantInexistente = jwtService.gerarToken(
                UUID.randomUUID(),
                tenantInexistente
        );

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
}
