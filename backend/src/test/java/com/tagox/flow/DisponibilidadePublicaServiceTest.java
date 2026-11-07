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
import com.tagox.flow.dto.agenda.HorarioDisponivelResponse;
import com.tagox.flow.dto.user.CreateUserRequest;
import com.tagox.flow.service.AgendaProfissionalService;
import com.tagox.flow.service.AgendamentoService;
import com.tagox.flow.service.DisponibilidadePublicaService;
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

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DisponibilidadePublicaServiceTest {

    private static final ZoneId FUSO_CLINICA =
            ZoneId.of("America/Sao_Paulo");

    private static final LocalTime HORARIO_INICIO =
            LocalTime.of(9, 0);

    private static final LocalTime HORARIO_FIM =
            LocalTime.of(12, 0);

    private static final long DURACAO_CONSULTA_SEGUNDOS =
            50 * 60L;

    private static final String SLUG_PREFIXO = "disponibilidade";

    @Autowired
    private DisponibilidadePublicaService disponibilidadeService;

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private AgendaProfissionalService agendaService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;

    private Tenant tenant;
    private User profissional;
    private Patient paciente;
    private LocalDate dataConsulta;

    @BeforeEach
    void prepararDados() {
        garantirPapeisPadrao();

        tenant = criarTenant();

        profissional = criarProfissional();

        paciente = criarPaciente();

        dataConsulta = LocalDate.now(FUSO_CLINICA)
                .plusDays(7);

        cadastrarHorarioProfissional();
    }

    @Test
    void deveDisponibilizarHorariosRespeitandoIntervaloPadrao() {

        List<HorarioDisponivelResponse> horarios =
                consultarDisponibilidade();

        assertThat(horarios)
                .extracting(HorarioDisponivelResponse::inicio)
                .containsExactly(
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0)
                );

        assertThat(horarios)
                .extracting(HorarioDisponivelResponse::fim)
                .containsExactly(
                        LocalTime.of(9, 50),
                        LocalTime.of(10, 50),
                        LocalTime.of(11, 50)
                );
    }

    @Test
    void deveDisponibilizarHorariosSemIntervaloQuandoConfiguradoComoZero() {

        tenant.atualizarIntervaloEntreConsultasMinutos(0);

        List<HorarioDisponivelResponse> horarios =
                consultarDisponibilidade();

        assertThat(horarios)
                .extracting(HorarioDisponivelResponse::inicio)
                .containsExactly(
                        LocalTime.of(9, 0),
                        LocalTime.of(9, 50),
                        LocalTime.of(10, 40)
                );

        assertThat(horarios)
                .extracting(HorarioDisponivelResponse::fim)
                .containsExactly(
                        LocalTime.of(9, 50),
                        LocalTime.of(10, 40),
                        LocalTime.of(11, 30)
                );
    }

    @Test
    void naoDeveDisponibilizarHorarioSemRespeitarIntervaloAposConsultaExistente() {

        Instant inicioAgendamento = dataConsulta
                .atTime(9, 5)
                .atZone(FUSO_CLINICA)
                .toInstant();

        agendamentoService.criarAgendamento(
                tenant.getId(),
                profissional.getId(),
                paciente.getId(),
                inicioAgendamento,
                null
        );

        List<HorarioDisponivelResponse> horarios =
                consultarDisponibilidade();

        assertThat(horarios)
                .extracting(HorarioDisponivelResponse::inicio)
                .containsExactly(
                        LocalTime.of(11, 0)
                );
    }

    @Test
    void deveLiberarHorarioQuandoAgendamentoForCancelado() {

        Instant inicioAgendamento = dataConsulta
                .atTime(10, 0)
                .atZone(FUSO_CLINICA)
                .toInstant();

        Agendamento agendamento =
                agendamentoService.criarAgendamento(
                        tenant.getId(),
                        profissional.getId(),
                        paciente.getId(),
                        inicioAgendamento,
                        null
                );

        List<HorarioDisponivelResponse> horariosOcupados =
                consultarDisponibilidade();

        assertThat(horariosOcupados)
                .extracting(HorarioDisponivelResponse::inicio)
                .doesNotContain(
                        LocalTime.of(10, 0)
                );

        agendamentoService.cancelar(
                tenant.getId(),
                agendamento.getId()
        );

        List<HorarioDisponivelResponse> horariosLiberados =
                consultarDisponibilidade();

        assertThat(horariosLiberados)
                .extracting(HorarioDisponivelResponse::inicio)
                .containsExactly(
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0)
                );
    }

    private List<HorarioDisponivelResponse> consultarDisponibilidade() {
        return disponibilidadeService.consultarDisponibilidade(
                tenant.getSlug(),
                profissional.getId(),
                dataConsulta
        );
    }

    private void cadastrarHorarioProfissional() {

        short diaSemana = (short) dataConsulta
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

    private Tenant criarTenant() {

        String identificador = UUID.randomUUID()
                .toString()
                .replace("-", "");

        String documento = "55"
                + identificador.substring(0, 12);

        Tenant novoTenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                documento,
                "Clinica Disponibilidade",
                "Clinica Disponibilidade",
                SLUG_PREFIXO + "-" + identificador,
                TenantStatus.TRIAL
        );

        return tenantRepository.save(novoTenant);
    }

    private User criarProfissional() {

        String identificador = UUID.randomUUID()
                .toString()
                .replace("-", "");

        CreateUserRequest request = new CreateUserRequest();

        request.setNome("Profissional Disponibilidade");

        request.setEmail(
                "profissional-" + identificador + "@teste.com"
        );

        request.setSenha("senha-segura");

        request.setRole(RoleType.PROFISSIONAL);

        return userService.criarUsuario(
                request,
                tenant.getId()
        );
    }

    private Patient criarPaciente() {

        Patient novoPaciente = new Patient(
                UUID.randomUUID(),
                tenant,
                "Paciente Disponibilidade",
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