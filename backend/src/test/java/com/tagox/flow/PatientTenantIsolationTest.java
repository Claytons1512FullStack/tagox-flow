package com.tagox.flow;

import com.tagox.flow.domain.patient.Patient;
import com.tagox.flow.domain.patient.PatientRepository;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.tenant.TipoPessoa;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PatientTenantIsolationTest {


    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    void naoDevePermitirQueTenantDiferenteEncontrePaciente() {
        Tenant tenantA = criarTenant("Empresa A");
        Tenant tenantB = criarTenant("Empresa B");

        tenantRepository.save(tenantA);
        tenantRepository.save(tenantB);

        Patient paciente = new Patient(
                UUID.randomUUID(),
                tenantA,
                "Paciente da Empresa A",
                null,
                null,
                null,
                null
        );

        Patient pacienteSalvo = patientRepository.saveAndFlush(paciente);

        assertThat(
                patientRepository.findByIdAndTenantId(
                        pacienteSalvo.getId(),
                        tenantA.getId()
                )
        ).isPresent();

        assertThat(
                patientRepository.findByIdAndTenantId(
                        pacienteSalvo.getId(),
                        tenantB.getId()
                )
        ).isEmpty();
    }

    @Test
    void deveListarSomentePacientesDoTenantSolicitado() {
        Tenant tenantA = criarTenant("Empresa C");
        Tenant tenantB = criarTenant("Empresa D");

        tenantRepository.save(tenantA);
        tenantRepository.save(tenantB);

        patientRepository.saveAndFlush(new Patient(
                UUID.randomUUID(),
                tenantA,
                "Ana Paciente",
                null,
                null,
                null,
                null
        ));

        patientRepository.saveAndFlush(new Patient(
                UUID.randomUUID(),
                tenantB,
                "Bruno Paciente",
                null,
                null,
                null,
                null
        ));

        assertThat(
                patientRepository.findAllByTenantIdOrderByNomeAsc(
                        tenantA.getId()
                )
        )
                .hasSize(1)
                .allSatisfy(paciente ->
                        assertThat(paciente.getTenant().getId())
                                .isEqualTo(tenantA.getId())
                );

        assertThat(
                patientRepository.findAllByTenantIdOrderByNomeAsc(
                        tenantB.getId()
                )
        )
                .hasSize(1)
                .allSatisfy(paciente ->
                        assertThat(paciente.getTenant().getId())
                                .isEqualTo(tenantB.getId())
                );
    }

    private Tenant criarTenant(String nome) {
        String documento = gerarCnpjValido();

        String slug = nome.toLowerCase()
                .replace(" ", "-")
                + "-"
                + UUID.randomUUID().toString().substring(0, 8);

        return new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                documento,
                nome,
                nome,
                slug,
                TenantStatus.ATIVO
        );
    }

    private String gerarCnpjValido() {
        String base = UUID.randomUUID()
                .toString()
                .replaceAll("\\D", "");

        base = (base + "123456789012").substring(0, 12);

        int primeiroDigito = calcularDigitoCnpj(base);
        String parcial = base + primeiroDigito;

        int segundoDigito = calcularDigitoCnpj(parcial);

        return parcial + segundoDigito;
    }

    private int calcularDigitoCnpj(String valor) {
        int[] pesos = valor.length() == 12
                ? new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}
                : new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int soma = 0;

        for (int i = 0; i < valor.length(); i++) {
            soma += Character.getNumericValue(valor.charAt(i)) * pesos[i];
        }

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

}
