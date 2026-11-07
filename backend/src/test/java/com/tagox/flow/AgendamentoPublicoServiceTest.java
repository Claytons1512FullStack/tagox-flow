package com.tagox.flow;

import com.tagox.flow.domain.agenda.Agendamento;
import com.tagox.flow.domain.agenda.AgendamentoRepository;
import com.tagox.flow.domain.agenda.AgendaProfissionalRepository;
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
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.userrole.UserRole;
import com.tagox.flow.domain.userrole.UserRoleRepository;
import com.tagox.flow.dto.agenda.CriarAgendamentoPublicoRequest;
import com.tagox.flow.dto.user.CreateUserRequest;
import com.tagox.flow.exception.BusinessException;
import com.tagox.flow.exception.DuplicateResourceException;
import com.tagox.flow.service.AgendaProfissionalService;
import com.tagox.flow.service.AgendamentoPublicoService;
import com.tagox.flow.service.AgendamentoService;
import com.tagox.flow.service.UserService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AgendamentoPublicoServiceTest {

    private static final ZoneId FUSO_CLINICA =
            ZoneId.of("America/Sao_Paulo");

    private static final String CPF_VALIDO = "52998224725";

    private static final LocalTime HORARIO_INICIO =
            LocalTime.of(9, 0);

    private static final LocalTime HORARIO_FIM =
            LocalTime.of(12, 0);

    @Autowired
    private AgendamentoPublicoService agendamentoPublicoService;

    @MockitoSpyBean
    private AgendamentoService agendamentoService;

    @Autowired
    private AgendaProfissionalService agendaService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private AgendaProfissionalRepository agendaRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private Tenant tenant;
    private User profissional;
    private Patient pacienteExistente;
    private LocalDate dataConsulta;

    @BeforeEach
    void prepararDados() {
        garantirPapeisPadrao();

        tenant = criarTenant();

        profissional = criarProfissional();

        pacienteExistente = criarPacienteExistente();

        dataConsulta = LocalDate.now(FUSO_CLINICA)
                .plusDays(7);

        cadastrarHorarioProfissional();
    }

    @AfterEach
    void limparDadosPersistidosDoTeste() {

        if (tenant == null) {
            return;
        }

        UUID tenantId = tenant.getId();

        UUID profissionalId = profissional != null
                ? profissional.getId()
                : null;

        // 1. Agendamentos
        List<Agendamento> agendamentos =
                agendamentoRepository.findAll()
                        .stream()
                        .filter(agendamento ->
                                agendamento.getTenant().getId()
                                        .equals(tenantId))
                        .toList();

        agendamentoRepository.deleteAll(agendamentos);

        // 2. Agendas profissionais
        if (profissionalId != null) {
            agendaRepository
                    .findAllByTenant_IdAndProfissional_IdOrderByDiaSemanaAscHorarioInicioAsc(
                            tenantId,
                            profissionalId
                    )
                    .forEach(agendaRepository::delete);
        }

        // 3. Pacientes
        List<Patient> pacientes =
                patientRepository
                        .findAllByTenantIdOrderByNomeAsc(tenantId);

        patientRepository.deleteAll(pacientes);

        // 4. Vínculos entre usuários e papéis
        if (profissionalId != null) {
            List<UserRole> vinculos =
                    userRoleRepository.findAll()
                            .stream()
                            .filter(vinculo ->
                                    vinculo.getUsuario().getId()
                                            .equals(profissionalId))
                            .toList();

            userRoleRepository.deleteAll(vinculos);
        }

        // 5. Usuário profissional
        if (profissional != null) {
            userRepository.delete(profissional);
        }

        // 6. Tenant
        tenantRepository.delete(tenant);
    }

    @Test
    void deveCriarPacienteEAgendamentoEmSolicitacaoPublicaValida() {

        CriarAgendamentoPublicoRequest request =
                criarSolicitacao(
                        "Paciente Público Teste",
                        null,
                        LocalTime.of(9, 0)
                );

        Agendamento agendamento =
                agendamentoPublicoService.solicitarAgendamento(
                        tenant.getSlug(),
                        profissional.getId(),
                        request
                );

        assertThat(agendamento).isNotNull();

        assertThat(agendamento.getInicio())
                .isEqualTo(
                        dataConsulta.atTime(9, 0)
                                .atZone(FUSO_CLINICA)
                                .toInstant()
                );

        assertThat(patientRepository
                .findAllByTenantIdOrderByNomeAsc(tenant.getId()))
                .extracting(Patient::getNome)
                .contains("Paciente Público Teste");
    }

    @Test
    void naoDeveCriarPacienteQuandoHorarioNaoEstiverDisponivel() {

        Instant inicioExistente = dataConsulta
                .atTime(9, 0)
                .atZone(FUSO_CLINICA)
                .toInstant();

        agendamentoService.criarAgendamento(
                tenant.getId(),
                profissional.getId(),
                pacienteExistente.getId(),
                inicioExistente,
                null
        );

        CriarAgendamentoPublicoRequest request =
                criarSolicitacao(
                        "Paciente Horário Indisponível",
                        null,
                        LocalTime.of(9, 0)
                );

        assertThatThrownBy(() ->
                agendamentoPublicoService.solicitarAgendamento(
                        tenant.getSlug(),
                        profissional.getId(),
                        request
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não está mais disponível");

        assertThat(patientRepository
                .findAllByTenantIdOrderByNomeAsc(tenant.getId()))
                .extracting(Patient::getNome)
                .doesNotContain("Paciente Horário Indisponível");
    }

    @Test
    void naoDeveCriarAgendamentoQuandoCpfJaEstiverCadastrado() {

        Patient pacienteComCpf = new Patient(
                UUID.randomUUID(),
                tenant,
                "Paciente com CPF cadastrado",
                CPF_VALIDO,
                null,
                null,
                null
        );

        patientRepository.save(pacienteComCpf);

        CriarAgendamentoPublicoRequest request =
                criarSolicitacao(
                        "Novo Paciente com CPF duplicado",
                        CPF_VALIDO,
                        LocalTime.of(9, 0)
                );

        assertThatThrownBy(() ->
                agendamentoPublicoService.solicitarAgendamento(
                        tenant.getSlug(),
                        profissional.getId(),
                        request
                )
        )
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("CPF");

        assertThat(patientRepository
                .findAllByTenantIdOrderByNomeAsc(tenant.getId()))
                .extracting(Patient::getNome)
                .doesNotContain("Novo Paciente com CPF duplicado");
    }

    @Test
    void naoDeveAceitarHorarioQueDesrespeiteIntervaloEntreConsultas() {

        Instant inicioExistente = dataConsulta
                .atTime(9, 0)
                .atZone(FUSO_CLINICA)
                .toInstant();

        agendamentoService.criarAgendamento(
                tenant.getId(),
                profissional.getId(),
                pacienteExistente.getId(),
                inicioExistente,
                null
        );

        CriarAgendamentoPublicoRequest request =
                criarSolicitacao(
                        "Paciente Intervalo Insuficiente",
                        null,
                        LocalTime.of(9, 50)
                );

        assertThatThrownBy(() ->
                agendamentoPublicoService.solicitarAgendamento(
                        tenant.getSlug(),
                        profissional.getId(),
                        request
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não está mais disponível");

        assertThat(patientRepository
                .findAllByTenantIdOrderByNomeAsc(tenant.getId()))
                .extracting(Patient::getNome)
                .doesNotContain("Paciente Intervalo Insuficiente");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deveReverterCriacaoDoPacienteQuandoAgendamentoFalhar() {

        String nomePaciente = "Paciente Rollback Teste";

        doThrow(new BusinessException(
                "Falha simulada na criação do agendamento"
        ))
                .when(agendamentoService)
                .criarAgendamento(
                        any(UUID.class),
                        any(UUID.class),
                        any(UUID.class),
                        any(Instant.class),
                        any()
                );

        CriarAgendamentoPublicoRequest request =
                criarSolicitacao(
                        nomePaciente,
                        null,
                        LocalTime.of(9, 0)
                );

        assertThatThrownBy(() ->
                agendamentoPublicoService.solicitarAgendamento(
                        tenant.getSlug(),
                        profissional.getId(),
                        request
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Falha simulada");

        assertThat(patientRepository
                .findAllByTenantIdOrderByNomeAsc(tenant.getId()))
                .extracting(Patient::getNome)
                .doesNotContain(nomePaciente);
    }

    private CriarAgendamentoPublicoRequest criarSolicitacao(
            String nome,
            String cpf,
            LocalTime horario
    ) {
        CriarAgendamentoPublicoRequest request =
                new CriarAgendamentoPublicoRequest();

        request.setNome(nome);
        request.setCpf(cpf);
        request.setData(dataConsulta);
        request.setHorarioInicio(horario);
        request.setEmail(null);
        request.setTelefone(null);
        request.setObservacoes("Solicitação de teste");

        return request;
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
                "Clinica Agendamento Publico",
                "Clinica Agendamento Publico",
                "agendamento-publico-" + identificador,
                TenantStatus.TRIAL
        );

        return tenantRepository.save(novoTenant);
    }

    private User criarProfissional() {

        String identificador = UUID.randomUUID()
                .toString()
                .replace("-", "");

        CreateUserRequest request = new CreateUserRequest();

        request.setNome("Profissional Agendamento Público");

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

    private Patient criarPacienteExistente() {

        Patient paciente = new Patient(
                UUID.randomUUID(),
                tenant,
                "Paciente Existente",
                null,
                null,
                null,
                null
        );

        return patientRepository.save(paciente);
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