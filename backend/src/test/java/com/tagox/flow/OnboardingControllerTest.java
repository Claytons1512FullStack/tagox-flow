package com.tagox.flow;

import com.tagox.flow.domain.role.Role;
import com.tagox.flow.domain.role.RoleRepository;
import com.tagox.flow.domain.role.RoleType;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.userrole.UserRoleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class OnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @BeforeEach
    void prepararBanco() {

        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.saveAll(
                List.of(
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
                )
        );
    }

    @Test
    void deveCadastrarEmpresaAdministradorEAssociarRoleAdmin()
            throws Exception {

        String json = """
                {
                    "tenant": {
                        "tipoPessoa": "JURIDICA",
                        "documento": "11222333000181",
                        "nome": "TAGOX Flow Tecnologia LTDA",
                        "nomeFantasia": "TAGOX Flow",
                        "slug": "tagox-flow-onboarding"
                    },
                    "administrador": {
                        "nome": "Administrador TAGOX",
                        "email": "ADMIN@TAGOX.COM.BR",
                        "senha": "SenhaSegura123!"
                    }
                }
                """;

        var resultado = mockMvc.perform(
                        post("/api/onboarding")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenant.id").exists())
                .andExpect(jsonPath("$.tenant.nome")
                        .value("TAGOX Flow Tecnologia LTDA"))
                .andExpect(jsonPath("$.tenant.status")
                        .value("TRIAL"))
                .andExpect(jsonPath("$.administrador.id").exists())
                .andExpect(jsonPath("$.administrador.nome")
                        .value("Administrador TAGOX"))
                .andExpect(jsonPath("$.administrador.email")
                        .value("admin@tagox.com.br"))
                .andExpect(jsonPath("$.administrador.status")
                        .value("ATIVO"))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.administrador.senha").doesNotExist())
                .andExpect(jsonPath("$.administrador.senhaHash").doesNotExist())
                .andReturn();

        String resposta = resultado.getResponse().getContentAsString();

        UUID administradorId = UUID.fromString(
                com.jayway.jsonpath.JsonPath.read(
                        resposta,
                        "$.administrador.id"
                )
        );

        Role roleAdmin = roleRepository.findByTipo(RoleType.ADMIN)
                .orElseThrow();

        assertThat(tenantRepository.count()).isEqualTo(1);
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(userRoleRepository.count()).isEqualTo(1);

        assertThat(
                userRoleRepository.existsByIdUsuarioIdAndIdRoleId(
                        administradorId,
                        roleAdmin.getId()
                )
        ).isTrue();
    }

    @Test
    void deveRejeitarEmailInvalidoSemCriarRegistros() throws Exception {

        String json = """
                {
                    "tenant": {
                        "tipoPessoa": "JURIDICA",
                        "documento": "11222333000181",
                        "nome": "TAGOX Flow Tecnologia LTDA",
                        "nomeFantasia": "TAGOX Flow",
                        "slug": "tagox-flow-email-invalido"
                    },
                    "administrador": {
                        "nome": "Administrador TAGOX",
                        "email": "email-invalido",
                        "senha": "SenhaSegura123!"
                    }
                }
                """;

        mockMvc.perform(
                        post("/api/onboarding")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());

        assertThat(tenantRepository.count()).isZero();
        assertThat(userRepository.count()).isZero();
        assertThat(userRoleRepository.count()).isZero();
    }

    @Test
    void deveReverterCadastroQuandoRoleAdminNaoExiste()
            throws Exception {

        roleRepository.deleteAll();

        String json = """
                {
                    "tenant": {
                        "tipoPessoa": "JURIDICA",
                        "documento": "11222333000181",
                        "nome": "TAGOX Flow Tecnologia LTDA",
                        "nomeFantasia": "TAGOX Flow",
                        "slug": "tagox-flow-rollback"
                    },
                    "administrador": {
                        "nome": "Administrador TAGOX",
                        "email": "admin@tagox.com.br",
                        "senha": "SenhaSegura123!"
                    }
                }
                """;

        mockMvc.perform(
                        post("/api/onboarding")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound());

        assertThat(tenantRepository.count()).isZero();
        assertThat(userRepository.count()).isZero();
        assertThat(userRoleRepository.count()).isZero();
    }
}