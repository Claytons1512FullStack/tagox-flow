package com.tagox.flow;

import com.tagox.flow.domain.agenda.Agendamento;
import com.tagox.flow.domain.agenda.AgendamentoRepository;
import com.tagox.flow.domain.patient.Patient;
import com.tagox.flow.domain.patient.PatientRepository;
import com.tagox.flow.domain.role.Role;
import com.tagox.flow.domain.role.RoleRepository;
import com.tagox.flow.domain.role.RoleType;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.dto.user.CreateUserRequest;
import com.tagox.flow.exception.BusinessException;
import com.tagox.flow.exception.ResourceNotFoundException;
import com.tagox.flow.service.AgendaProfissionalService;
import com.tagox.flow.service.AgendamentoService;
import com.tagox.flow.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AgendamentoServiceTest {

    private static final ZoneId FUSO_CLINICA =
            ZoneId.of("America/Sao_Paulo");

    private static final LocalTime HORARIO_INICIO =
            LocalTime.of(9, 0);

    private static final LocalTime HORARIO_FIM =
            LocalTime.of(12, 0);

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private AgendaProfissionalService agendaService;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Tenant tenant;
    private User profissional;
    private Patient paciente;
    private Instant inicioConsulta;

    @BeforeEach
    void prepararDados() {
        garantirPapeisPadrao();

        tenant = criarTenant("agendamento");

        profissional = criarProfissional(
                tenant,
                "profissional"
        );

        paciente = criarPaciente(
                tenant,
                "paciente"
        );

        inicioConsulta = proximoHorarioDisponivel();

        cadastrarHorarioProfissional();
    }

    @Test
    void deveCriarAgendamentoComDuracaoDeCinquentaMinutos() {
        Agendamento agendamento =
                agendamentoService.criarAgendamento(
                        tenant.getId(),
                        profissional.getId(),
                        paciente.getId(),
                        inicioConsulta,
                        "Primeira consulta"
                );

        assertThat(agendamento.getId()).isNotNull();

        assertThat(agendamento.getTenant().getId())
                .isEqualTo(tenant.getId());

        assertThat(agendamento.getProfissional().getId())
                .isEqualTo(profissional.getId());

        assertThat(agendamento.getPaciente().getId())
                .isEqualTo(paciente.getId());

        assertThat(agendamento.getInicio())
                .isEqualTo(inicioConsulta);

        assertThat(agendamento.getFim())
                .isEqualTo(
                        inicioConsulta.plusSeconds(50 * 60)
                );

        assertThat(agendamento.getStatus())
                .isEqualTo(Agendamento.Status.SOLICITADO);

        assertThat(agendamento.getObservacoes())
                .isEqualTo("Primeira consulta");
    }

    @Test
    void naoDeveCriarAgendamentoForaDaAgendaProfissional() {
        Instant horarioIndisponivel = inicioConsulta
                .atZone(FUSO_CLINICA)
                .withHour(13)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .toInstant();

        assertThatThrownBy(() ->
                agendamentoService.criarAgendamento(
                        tenant.getId(),
                        profissional.getId(),
                        paciente.getId(),
                        horarioIndisponivel,
                        null
                )
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    void naoDevePermitirAgendamentosComHorariosSobrepostos() {
        agendamentoService.criarAgendamento(
                tenant.getId(),
                profissional.getId(),
                paciente.getId(),
                inicioConsulta,
                null
        );

        Instant horarioSobreposto =
                inicioConsulta.plusSeconds(30 * 60);

        assertThatThrownBy(() ->
                agendamentoService.criarAgendamento(
                        tenant.getId(),
                        profissional.getId(),
                        paciente.getId(),
                        horarioSobreposto,
                        null
                )
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    void devePermitirConsultasConsecutivas() {
        agendamentoService.criarAgendamento(
                tenant.getId(),
                profissional.getId(),
                paciente.getId(),
                inicioConsulta,
                null
        );

        Instant proximaConsulta =
                inicioConsulta.plusSeconds(50 * 60);

        Agendamento segundoAgendamento =
                agendamentoService.criarAgendamento(
                        tenant.getId(),
                        profissional.getId(),
                        paciente.getId(),
                        proximaConsulta,
                        null
                );

        assertThat(segundoAgendamento.getInicio())
                .isEqualTo(proximaConsulta);
    }

    @Test
    void naoDevePermitirPacienteDeOutroTenant() {
        Tenant outroTenant = criarTenant("outro-tenant");

        Patient pacienteDeOutraClinica =
                criarPaciente(
                        outroTenant,
                        "paciente-externo"
                );

        assertThatThrownBy(() ->
                agendamentoService.criarAgendamento(
                        tenant.getId(),
                        profissional.getId(),
                        pacienteDeOutraClinica.getId(),
                        inicioConsulta,
                        null
                )
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveConfirmarCancelarEConcluirAgendamentos() {
        // Primeiro agendamento: criar e confirmar.
        Agendamento primeiro =
                agendamentoService.criarAgendamento(
                        tenant.getId(),
                        profissional.getId(),
                        paciente.getId(),
                        inicioConsulta,
                        null
                );

        Agendamento confirmado =
                agendamentoService.confirmar(
                        tenant.getId(),
                        primeiro.getId()
                );

        assertThat(confirmado.getStatus())
                .isEqualTo(Agendamento.Status.CONFIRMADO);

        // Segundo agendamento: criar e cancelar.
        Agendamento segundo =
                agendamentoService.criarAgendamento(
                        tenant.getId(),
                        profissional.getId(),
                        paciente.getId(),
                        inicioConsulta.plusSeconds(50 * 60),
                        null
                );

        Agendamento cancelado =
                agendamentoService.cancelar(
                        tenant.getId(),
                        segundo.getId()
                );

        assertThat(cancelado.getStatus())
                .isEqualTo(Agendamento.Status.CANCELADO);

        // Terceiro agendamento: criar, confirmar e concluir.
        // Começa 60 minutos após o início da primeira consulta.
        // Assim, termina às 11h50, dentro da agenda das 9h às 12h.
        Agendamento terceiro =
                agendamentoService.criarAgendamento(
                        tenant.getId(),
                        profissional.getId(),
                        paciente.getId(),
                        inicioConsulta.plusSeconds(60 * 60),
                        null
                );

        agendamentoService.confirmar(
                tenant.getId(),
                terceiro.getId()
        );

        Agendamento concluido =
                agendamentoService.concluir(
                        tenant.getId(),
                        terceiro.getId()
                );

        assertThat(concluido.getStatus())
                .isEqualTo(Agendamento.Status.CONCLUIDO);
    }

    @Test
    void naoDeveCriarAgendamentoNoPassado() {
        Instant passado = Instant.now().minusSeconds(3600);

        assertThatThrownBy(() ->
                agendamentoService.criarAgendamento(
                        tenant.getId(),
                        profissional.getId(),
                        paciente.getId(),
                        passado,
                        null
                )
        ).isInstanceOf(BusinessException.class);
    }

    private void cadastrarHorarioProfissional() {
        short diaSemana = (short) inicioConsulta
                .atZone(FUSO_CLINICA)
                .getDayOfWeek()
                .getValue();

        agendaService.cadastrarHorario(
                tenant.getId(),
                profissional.getId(),
                diaSemana,
                HORARIO_INICIO,
                HORARIO_FIM
        );
    }

    private Instant proximoHorarioDisponivel() {
        LocalDate data = LocalDate.now(FUSO_CLINICA)
                .plusDays(7);

        return data.atTime(LocalTime.of(10, 0))
                .atZone(FUSO_CLINICA)
                .toInstant();
    }

    private Tenant criarTenant(String prefixo) {
        String identificador = UUID.randomUUID()
                .toString()
                .replace("-", "");

        String documento = "55"
                + identificador.substring(0, 12);

        Tenant novoTenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                documento,
                "Clinica " + prefixo,
                "Clinica " + prefixo,
                prefixo + "-" + identificador,
                TenantStatus.TRIAL
        );

        return tenantRepository.save(novoTenant);
    }

    private User criarProfissional(
            Tenant tenant,
            String prefixo
    ) {
        String identificador = UUID.randomUUID()
                .toString()
                .replace("-", "");

        CreateUserRequest request = new CreateUserRequest();

        request.setNome("Profissional " + prefixo);

        request.setEmail(
                prefixo + "-" + identificador + "@teste.com"
        );

        request.setSenha("senha-segura");

        request.setRole(RoleType.PROFISSIONAL);

        return userService.criarUsuario(
                request,
                tenant.getId()
        );
    }

    private Patient criarPaciente(
            Tenant tenant,
            String prefixo
    ) {
        Patient novoPaciente = new Patient(
                UUID.randomUUID(),
                tenant,
                "Paciente " + prefixo,
                null,
                null,
                null,
                null
        );

        return patientRepository.save(novoPaciente);
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