package com.tagox.flow;

import com.tagox.flow.domain.agenda.AgendaProfissionalRepository;
import com.tagox.flow.domain.role.Role;
import com.tagox.flow.domain.role.RoleRepository;
import com.tagox.flow.domain.role.RoleType;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.tenant.TipoPessoa;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AgendaProfissionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AgendaProfissionalRepository agendaRepository;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserRoleRepository userRoleRepository;

    private Tenant tenant;
    private User admin;
    private User profissional;
    private String tokenAdmin;
    private String tokenProfissional;

    @BeforeEach
    void prepararDados() {
        agendaRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        garantirPapeisPadrao();

        String identificador = UUID.randomUUID()
                .toString()
                .replace("-", "");

        tenant = tenantRepository.save(
                new Tenant(
                        UUID.randomUUID(),
                        TipoPessoa.JURIDICA,
                        "66" + identificador.substring(0, 12),
                        "Clínica Agenda HTTP",
                        "Clínica Agenda HTTP",
                        "clinica-agenda-http-" + identificador,
                        TenantStatus.TRIAL
                )
        );

        admin = criarUsuario(
                UUID.randomUUID(),
                tenant,
                "admin-" + identificador + "@teste.com"
        );

        profissional = criarUsuario(
                UUID.randomUUID(),
                tenant,
                "profissional-" + identificador + "@teste.com"
        );

        tokenAdmin = jwtService.gerarToken(
                admin.getId(),
                tenant.getId()
        );

        tokenProfissional = jwtService.gerarToken(
                profissional.getId(),
                tenant.getId()
        );

        when(userRoleRepository.findRoleTypesByUsuarioId(admin.getId()))
                .thenReturn(List.of(RoleType.ADMIN));

        when(userRoleRepository.findRoleTypesByUsuarioId(profissional.getId()))
                .thenReturn(List.of(RoleType.PROFISSIONAL));
    }

    private void garantirPapeisPadrao() {
        List<Role> papeis = List.of(
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

        for (Role papel : papeis) {
            roleRepository.findByTipo(papel.getTipo())
                    .orElseGet(() -> roleRepository.save(papel));
        }
    }

    private User criarUsuario(
            UUID id,
            Tenant tenantUsuario,
            String email
    ) {
        return userRepository.save(
                new User(
                        id,
                        tenantUsuario,
                        "Usuário de Teste",
                        email,
                        "senha-hash-teste",
                        UserStatus.ATIVO
                )
        );
    }

    @Test
    void deveCadastrarHorarioPelaApi() throws Exception {
        String json = """
                {
                    "diaSemana": 1,
                    "horarioInicio": "09:00:00",
                    "horarioFim": "12:00:00"
                }
                """;

        mockMvc.perform(
                        post("/api/agenda-profissional/profissionais/"
                                + profissional.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenAdmin
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.profissionalId")
                        .value(profissional.getId().toString()))
                .andExpect(jsonPath("$.diaSemana").value(1))
                .andExpect(jsonPath("$.horarioInicio")
                        .value("09:00:00"))
                .andExpect(jsonPath("$.horarioFim")
                        .value("12:00:00"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void deveListarHorariosPelaApi() throws Exception {
        String json = """
                {
                    "diaSemana": 2,
                    "horarioInicio": "10:00:00",
                    "horarioFim": "16:00:00"
                }
                """;

        mockMvc.perform(
                        post("/api/agenda-profissional/profissionais/"
                                + profissional.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenAdmin
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/agenda-profissional/profissionais/"
                                + profissional.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenAdmin
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].diaSemana").value(2));
    }

    @Test
    void deveNegarCadastroDeHorarioAoProfissional() throws Exception {
        String json = """
                {
                    "diaSemana": 1,
                    "horarioInicio": "09:00:00",
                    "horarioFim": "12:00:00"
                }
                """;

        mockMvc.perform(
                        post("/api/agenda-profissional/profissionais/"
                                + profissional.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenProfissional
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deveNegarAlteracaoDeHorarioAoProfissional() throws Exception {
        String json = """
                {
                    "diaSemana": 1,
                    "horarioInicio": "09:00:00",
                    "horarioFim": "12:00:00"
                }
                """;

        String resposta = mockMvc.perform(
                        post("/api/agenda-profissional/profissionais/"
                                + profissional.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenAdmin
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String agendaId = com.fasterxml.jackson.databind.json.JsonMapper
                .builder()
                .build()
                .readTree(resposta)
                .get("id")
                .asText();

        mockMvc.perform(
                        patch("/api/agenda-profissional/"
                                + agendaId + "/desativar")
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenProfissional
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void naoDevePermitirAcessoAAgendaDeOutraClinica() throws Exception {
        String identificador = UUID.randomUUID()
                .toString()
                .replace("-", "");

        Tenant outroTenant = tenantRepository.save(
                new Tenant(
                        UUID.randomUUID(),
                        TipoPessoa.JURIDICA,
                        "77" + identificador.substring(0, 12),
                        "Outra Clínica",
                        "Outra Clínica",
                        "outra-clinica-" + identificador,
                        TenantStatus.ATIVO
                )
        );

        User outroProfissional = criarUsuario(
                UUID.randomUUID(),
                outroTenant,
                "outro-profissional-" + identificador + "@teste.com"
        );

        mockMvc.perform(
                        get("/api/agenda-profissional/profissionais/"
                                + outroProfissional.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenAdmin
                                )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRejeitarCadastroComIntervaloInvalido() throws Exception {
        String json = """
                {
                    "diaSemana": 1,
                    "horarioInicio": "12:00:00",
                    "horarioFim": "09:00:00"
                }
                """;

        mockMvc.perform(
                        post("/api/agenda-profissional/profissionais/"
                                + profissional.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenAdmin
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());
    }
}