package com.tagox.flow;

import com.tagox.flow.domain.agenda.AgendaProfissional;
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
import com.tagox.flow.domain.userrole.UserRoleRepository;
import com.tagox.flow.dto.user.CreateUserRequest;
import com.tagox.flow.exception.BusinessException;
import com.tagox.flow.service.AgendaProfissionalService;
import com.tagox.flow.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AgendaProfissionalServiceTest {

    @Autowired
    private AgendaProfissionalService agendaService;

    @Autowired
    private AgendaProfissionalRepository agendaRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Tenant tenant;
    private User profissional;

    @BeforeEach
    void prepararDados() {
        garantirPapeisPadrao();

        String identificador = UUID.randomUUID()
                .toString()
                .replace("-", "");

        String documento = "55" + identificador.substring(0, 12);

        tenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                documento,
                "Clinica Agenda Teste",
                "Clinica Agenda Teste",
                "clinica-agenda-" + identificador,
                TenantStatus.TRIAL
        );

        tenant = tenantRepository.save(tenant);

        CreateUserRequest request = new CreateUserRequest();
        request.setNome("Profissional Agenda");
        request.setEmail("profissional-" + identificador + "@teste.com");
        request.setSenha("senha-segura");
        request.setRole(RoleType.PROFISSIONAL);

        profissional = userService.criarUsuario(
                request,
                tenant.getId()
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

    @Test
    void deveCadastrarEListarHorarioProfissional() {
        AgendaProfissional salvo = agendaService.cadastrarHorario(
                tenant.getId(),
                profissional.getId(),
                (short) 1,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        List<AgendaProfissional> horarios =
                agendaService.listarHorarios(
                        tenant.getId(),
                        profissional.getId()
                );

        assertThat(salvo.getId()).isNotNull();
        assertThat(horarios)
                .hasSize(1)
                .first()
                .satisfies(horario -> {
                    assertThat(horario.getDiaSemana()).isEqualTo((short) 1);
                    assertThat(horario.getHorarioInicio())
                            .isEqualTo(LocalTime.of(9, 0));
                    assertThat(horario.getHorarioFim())
                            .isEqualTo(LocalTime.of(12, 0));
                    assertThat(horario.isAtivo()).isTrue();
                });
    }

    @Test
    void naoDeveCadastrarHorarioComIntervaloInvalido() {
        assertThatThrownBy(() ->
                agendaService.cadastrarHorario(
                        tenant.getId(),
                        profissional.getId(),
                        (short) 1,
                        LocalTime.of(12, 0),
                        LocalTime.of(9, 0)
                )
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("anterior");
    }

    @Test
    void naoDevePermitirSobreposicaoDeHorariosAtivos() {
        agendaService.cadastrarHorario(
                tenant.getId(),
                profissional.getId(),
                (short) 1,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        assertThatThrownBy(() ->
                agendaService.cadastrarHorario(
                        tenant.getId(),
                        profissional.getId(),
                        (short) 1,
                        LocalTime.of(11, 0),
                        LocalTime.of(13, 0)
                )
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("sobreposição");
    }

    @Test
    void deveDesativarHorarioSemExcluirRegistro() {
        AgendaProfissional salvo = agendaService.cadastrarHorario(
                tenant.getId(),
                profissional.getId(),
                (short) 2,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );

        agendaService.desativarHorario(
                tenant.getId(),
                salvo.getId()
        );

        AgendaProfissional desativado = agendaRepository
                .findByIdAndTenant_Id(salvo.getId(), tenant.getId())
                .orElseThrow();

        assertThat(desativado.isAtivo()).isFalse();

        assertThat(
                agendaService.listarHorarios(
                        tenant.getId(),
                        profissional.getId()
                )
        ).hasSize(1);

        assertThat(
                agendaService.listarHorariosAtivos(
                        tenant.getId(),
                        profissional.getId()
                )
        ).isEmpty();
    }
}