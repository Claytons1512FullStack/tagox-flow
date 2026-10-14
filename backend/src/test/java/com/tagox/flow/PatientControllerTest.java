package com.tagox.flow;

import com.tagox.flow.domain.patient.Patient;
import com.tagox.flow.domain.patient.PatientRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserRoleRepository userRoleRepository;

    private Tenant tenant;
    private User admin;
    private String adminToken;

    @BeforeEach
    void setUp() {
        tenant = criarTenant();
        admin = criarUsuario(tenant, "admin-" + UUID.randomUUID() + "@teste.com");
        adminToken = gerarToken(admin, tenant);

        when(userRoleRepository.findRoleTypesByUsuarioId(admin.getId()))
                .thenReturn(List.of(RoleType.ADMIN));
    }

    @Test
    void adminDeveCadastrarPaciente() throws Exception {
        String request = """
                {
                    "nome": "  Maria   da Silva  ",
                    "cpf": "529.982.247-25",
                    "dataNascimento": "1990-05-20",
                    "telefone": "11999999999",
                    "email": "MARIA@EXEMPLO.COM"
                }
                """;

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tenantId").value(tenant.getId().toString()))
                .andExpect(jsonPath("$.nome").value("Maria da Silva"))
                .andExpect(jsonPath("$.cpf").value("52998224725"))
                .andExpect(jsonPath("$.email").value("maria@exemplo.com"));
    }

    @Test
    void deveListarSomentePacientesDoTenantAutenticado() throws Exception {
        Tenant outroTenant = criarTenant();

        patientRepository.save(criarPaciente(tenant, "Paciente da Empresa A",
                "52998224725"));

        patientRepository.save(criarPaciente(outroTenant, "Paciente da Empresa B",
                "11144477735"));

        mockMvc.perform(get("/api/patients")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Paciente da Empresa A"))
                .andExpect(jsonPath("$[0].tenantId").value(tenant.getId().toString()));
    }

    @Test
    void naoDevePermitirConsultarPacienteDeOutroTenant() throws Exception {
        Tenant outroTenant = criarTenant();

        Patient paciente = patientRepository.save(
                criarPaciente(outroTenant, "Paciente Privado", "11144477735"));

        mockMvc.perform(get("/api/patients/{id}", paciente.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void naoDevePermitirAtualizarPacienteDeOutroTenant() throws Exception {
        Tenant outroTenant = criarTenant();

        Patient paciente = patientRepository.save(
                criarPaciente(outroTenant, "Paciente Protegido", "11144477735"));

        String request = """
                {
                    "nome": "Tentativa de Alteracao",
                    "cpf": "529.982.247-25",
                    "dataNascimento": "1990-05-20"
                }
                """;

        mockMvc.perform(put("/api/patients/{id}", paciente.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());
    }

    @Test
    void naoDeveCadastrarCpfDuplicadoNoMesmoTenant() throws Exception {
        patientRepository.save(
                criarPaciente(tenant, "Paciente Existente", "52998224725"));

        String request = """
                {
                    "nome": "Outro Paciente",
                    "cpf": "529.982.247-25"
                }
                """;

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    void assistenteNaoDeveCadastrarPaciente() throws Exception {
        User assistente = criarUsuario(
                tenant, "assistente-" + UUID.randomUUID() + "@teste.com");

        when(userRoleRepository.findRoleTypesByUsuarioId(assistente.getId()))
                .thenReturn(List.of(RoleType.ASSISTENTE));

        String token = gerarToken(assistente, tenant);

        String request = """
                {
                    "nome": "Paciente de Teste",
                    "cpf": "529.982.247-25"
                }
                """;

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
    }

    private Tenant criarTenant() {
        String identificador = UUID.randomUUID().toString();
        String nome = "Empresa " + identificador;

        Tenant novoTenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                gerarCnpj(),
                nome,
                nome,
                "empresa-" + identificador,
                TenantStatus.ATIVO
        );

        return tenantRepository.save(novoTenant);
    }

    private User criarUsuario(Tenant tenant, String email) {
        User usuario = new User(
                UUID.randomUUID(),
                tenant,
                "Usuario de Teste",
                email,
                "hash-de-senha-de-teste",
                UserStatus.ATIVO
        );

        return userRepository.save(usuario);
    }

    private String gerarToken(User usuario, Tenant tenant) {
        return jwtService.gerarToken(usuario.getId(), tenant.getId());
    }

    private Patient criarPaciente(Tenant tenant, String nome, String cpf) {
        return new Patient(
                UUID.randomUUID(),
                tenant,
                nome,
                cpf,
                LocalDate.of(1990, 5, 20),
                "11999999999",
                "paciente-" + UUID.randomUUID() + "@teste.com"
        );
    }

    private String gerarCnpj() {
        StringBuilder base = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            base.append((int) (Math.random() * 10));
        }

        int primeiroDigito = calcularDigito(base.toString(),
                new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});

        base.append(primeiroDigito);

        int segundoDigito = calcularDigito(base.toString(),
                new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});

        base.append(segundoDigito);

        return base.toString();
    }

    private int calcularDigito(String numero, int[] pesos) {
        int soma = 0;

        for (int i = 0; i < pesos.length; i++) {
            soma += Character.getNumericValue(numero.charAt(i)) * pesos[i];
        }

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
