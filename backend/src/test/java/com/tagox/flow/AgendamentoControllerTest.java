package com.tagox.flow;

import com.tagox.flow.domain.agenda.AgendamentoRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private AgendaProfissionalRepository agendaRepository;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserRoleRepository userRoleRepository;

    private Tenant tenant;
    private User admin;
    private User profissional;
    private User outroProfissional;
    private User assistente;

    private String tokenAdmin;
    private String tokenProfissional;
    private String tokenAssistente;

    @BeforeEach
    void prepararDados() {
        agendamentoRepository.deleteAll();
        agendaRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        garantirPapeisPadrao();

        String identificador = UUID.randomUUID()
                .toString()
                .replace("-", "");

        tenant = criarTenant(
                identificador,
                "66",
                "Clinica Agendamento",
                "clinica-agendamento-"
        );

        admin = criarUsuario(
                tenant,
                "admin-" + identificador + "@teste.com"
        );

        profissional = criarUsuario(
                tenant,
                "profissional-" + identificador + "@teste.com"
        );

        outroProfissional = criarUsuario(
                tenant,
                "outro-profissional-" + identificador + "@teste.com"
        );

        assistente = criarUsuario(
                tenant,
                "assistente-" + identificador + "@teste.com"
        );

        tokenAdmin = jwtService.gerarToken(
                admin.getId(),
                tenant.getId()
        );

        tokenProfissional = jwtService.gerarToken(
                profissional.getId(),
                tenant.getId()
        );

        tokenAssistente = jwtService.gerarToken(
                assistente.getId(),
                tenant.getId()
        );

        when(userRoleRepository.findRoleTypesByUsuarioId(admin.getId()))
                .thenReturn(List.of(RoleType.ADMIN));

        when(userRoleRepository.findRoleTypesByUsuarioId(profissional.getId()))
                .thenReturn(List.of(RoleType.PROFISSIONAL));

        when(userRoleRepository.findRoleTypesByUsuarioId(
                outroProfissional.getId()
        )).thenReturn(List.of(RoleType.PROFISSIONAL));

        when(userRoleRepository.findRoleTypesByUsuarioId(assistente.getId()))
                .thenReturn(List.of(RoleType.ASSISTENTE));
    }

    @Test
    void administradorDeveConsultarAgendaDeProfissionalDaMesmaClinica()
            throws Exception {

        mockMvc.perform(
                        get("/api/agendamentos/profissionais/"
                                + outroProfissional.getId())
                                .param("data", dataDeConsulta())
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenAdmin
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void profissionalDeveConsultarSuaPropriaAgenda()
            throws Exception {

        mockMvc.perform(
                        get("/api/agendamentos/profissionais/"
                                + profissional.getId())
                                .param("data", dataDeConsulta())
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenProfissional
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void profissionalNaoDeveConsultarAgendaDeOutroProfissional()
            throws Exception {

        mockMvc.perform(
                        get("/api/agendamentos/profissionais/"
                                + outroProfissional.getId())
                                .param("data", dataDeConsulta())
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenProfissional
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void assistenteNaoDeveConsultarAgendaPeloEndpointRestrito()
            throws Exception {

        mockMvc.perform(
                        get("/api/agendamentos/profissionais/"
                                + profissional.getId())
                                .param("data", dataDeConsulta())
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenAssistente
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorNaoDeveConsultarAgendaDeOutraClinica()
            throws Exception {

        String identificador = UUID.randomUUID()
                .toString()
                .replace("-", "");

        Tenant outraClinica = criarTenant(
                identificador,
                "77",
                "Outra Clinica",
                "outra-clinica-"
        );

        User profissionalDeOutraClinica = criarUsuario(
                outraClinica,
                "profissional-externo-" + identificador + "@teste.com"
        );

        mockMvc.perform(
                        get("/api/agendamentos/profissionais/"
                                + profissionalDeOutraClinica.getId())
                                .param("data", dataDeConsulta())
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenAdmin
                                )
                )
                .andExpect(status().isNotFound());
    }

    private String dataDeConsulta() {
        return LocalDate.now()
                .plusDays(7)
                .toString();
    }

    private Tenant criarTenant(
            String identificador,
            String prefixoDocumento,
            String nome,
            String prefixoSlug
    ) {
        return tenantRepository.save(
                new Tenant(
                        UUID.randomUUID(),
                        TipoPessoa.JURIDICA,
                        prefixoDocumento
                                + identificador.substring(0, 12),
                        nome,
                        nome,
                        prefixoSlug + identificador,
                        TenantStatus.TRIAL
                )
        );
    }

    private User criarUsuario(
            Tenant tenantUsuario,
            String email
    ) {
        return userRepository.save(
                new User(
                        UUID.randomUUID(),
                        tenantUsuario,
                        "Usuário de Teste",
                        email,
                        "senha-hash-teste",
                        UserStatus.ATIVO
                )
        );
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
}